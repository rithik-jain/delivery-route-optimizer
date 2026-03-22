package com.lucidity.deliveryrouteoptimizer.strategy;

import com.lucidity.deliveryrouteoptimizer.constant.AppConstants;
import com.lucidity.deliveryrouteoptimizer.distance.DistanceCalculator;
import com.lucidity.deliveryrouteoptimizer.util.DeliveryTimeHelper;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryResponseVo;
import com.lucidity.deliveryrouteoptimizer.vo.LocationVo;
import com.lucidity.deliveryrouteoptimizer.vo.OrderVo;
import com.lucidity.deliveryrouteoptimizer.vo.RouteStepVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the globally optimal route by evaluating every valid delivery sequence.
 *
 * <p>For N orders we have 2N stops. This recursively tries every valid
 * permutation (pickup before delivery) and keeps the fastest. Pruning
 * and a greedy upper-bound seed keep it practical for batches up to ~7.</p>
 *
 * <p>Pickups and deliveries are freely interleaved - the solver does NOT
 * assume "collect all first, then deliver all". At every step both
 * unvisited restaurants and deliverable consumers are candidates.</p>
 *
 * @author Rithik Jain
 */
public class PermutationRoutingStrategy implements RoutingStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermutationRoutingStrategy.class);

    /** Buffer multiplier for greedy upper bound - gives exact search room to improve. */
    private static final double GREEDY_BOUND_BUFFER = 1.05;

    private final DistanceCalculator distanceCalculator;
    private final double averageSpeedKmph;

    /**
     * @param distanceCalculator distance calculation implementation
     * @param averageSpeedKmph   average delivery speed in km/hr
     */
    public PermutationRoutingStrategy(DistanceCalculator distanceCalculator,
                                      double averageSpeedKmph) {
        this.distanceCalculator = distanceCalculator;
        this.averageSpeedKmph = averageSpeedKmph;
    }

    @Override
    public String getStrategyName() {
        return "Permutation Search (Exact)";
    }

    /**
     * {@inheritDoc}
     *
     * @param startLocation exec's current position
     * @param orders        batch of orders to deliver
     * @return the optimal route
     */
    @Override
    public DeliveryResponseVo findOptimalRoute(LocationVo startLocation, List<OrderVo> orders) {
        int n = orders.size();
        LOGGER.info("Running exact permutation search for {} orders from {}", n, startLocation);

        RouteProblemContext context = RouteProblemContext.build(
                startLocation, orders, distanceCalculator, averageSpeedKmph);

        SearchState state = new SearchState(n);
        BestResult best = new BestResult(context.getTotalStops(), estimateGreedyBound(context));
        LOGGER.debug("Greedy upper bound for pruning: {} min", String.format(AppConstants.DECIMAL_FORMAT_TWO, best.time));
        solve(context, state, best);
        return buildResponse(startLocation, orders, best, context);
    }

    /**
     * Recursive entry point. Tries all valid next stops, prunes aggressively.
     *
     * @param context precomputed problem data
     * @param state   mutable recursion state (backtracked after each branch)
     * @param best    best solution found so far
     */
    private void solve(RouteProblemContext context, SearchState state, BestResult best) {
        if (state.currentTime >= best.time) {
            return;
        }
        if (state.depth == context.getTotalStops()) {
            recordIfBetter(state, best);
            return;
        }

        if (estimateLowerBound(context, state) >= best.time) {
            return;
        }
        int currentLoc = (state.depth == 0)
                ? context.getStartLocIdx()
                : state.currentSequence[state.depth - 1];

        tryRestaurantVisits(context, state, best, currentLoc);
        tryConsumerVisits(context, state, best, currentLoc);
    }

    /**
     * Tries visiting each unvisited restaurant as the next stop.
     *
     * @param context    precomputed problem data
     * @param state      current search state
     * @param best       best known result
     * @param currentLoc index of current location in the travel matrix
     */
    private void tryRestaurantVisits(RouteProblemContext context, SearchState state,
                                     BestResult best, int currentLoc) {
        for (int i = 0; i < context.getOrderCount(); i++) {
            if (state.restaurantVisited[i]) continue;

            double arrival = state.currentTime + context.getTravelTimes()[currentLoc][i];
            double departure = Math.max(arrival, context.getPrepTimes()[i]);

            if (departure >= best.time) continue;

            state.restaurantVisited[i] = true;
            state.currentSequence[state.depth] = i;
            state.depth++;
            double savedTime = state.currentTime;
            state.currentTime = departure;

            solve(context, state, best);

            state.currentTime = savedTime;
            state.depth--;
            state.restaurantVisited[i] = false;
        }
    }

    /**
     * Tries delivering to each consumer whose food has been picked up.
     *
     * @param context    precomputed problem data
     * @param state      current search state
     * @param best       best known result
     * @param currentLoc index of current location in the travel matrix
     */
    private void tryConsumerVisits(RouteProblemContext context, SearchState state,
                                   BestResult best, int currentLoc) {
        for (int i = 0; i < context.getOrderCount(); i++) {
            if (!state.restaurantVisited[i] || state.consumerVisited[i]) continue;

            double arrival = state.currentTime + context.getTravelTimes()[currentLoc][context.consumerIdx(i)];

            if (arrival >= best.time) continue;

            state.consumerVisited[i] = true;
            state.currentSequence[state.depth] = context.consumerIdx(i);
            state.depth++;
            double savedTime = state.currentTime;
            state.currentTime = arrival;

            solve(context, state, best);

            state.currentTime = savedTime;
            state.depth--;
            state.consumerVisited[i] = false;
        }
    }

    /**
     * Updates the best result if the current completed route is faster.
     *
     * @param state current search state (at a leaf node)
     * @param best  best known result to update
     */
    private void recordIfBetter(SearchState state, BestResult best) {
        if (state.currentTime < best.time) {
            best.time = state.currentTime;
            System.arraycopy(state.currentSequence, 0, best.sequence, 0, state.currentSequence.length);
        }
    }

    /**
     * Quick lower bound - if remaining prep times alone would exceed best, prune.
     *
     * @param context precomputed problem data
     * @param state   current search state
     * @return minimum possible completion time from this state
     */
    private double estimateLowerBound(RouteProblemContext context, SearchState state) {
        double lowerBound = state.currentTime;
        for (int i = 0; i < context.getOrderCount(); i++) {
            if (!state.restaurantVisited[i]) {
                lowerBound = Math.max(lowerBound, context.getPrepTimes()[i]);
            }
        }
        return lowerBound;
    }

    /**
     * Runs a fast greedy pass to get an initial upper bound. Makes pruning
     * kick in much earlier in the exact search.
     *
     * @param context precomputed problem data
     * @return estimated total time with a small buffer
     */
    private double estimateGreedyBound(RouteProblemContext context) {
        int n = context.getOrderCount();
        boolean[] rVisited = new boolean[n];
        boolean[] cVisited = new boolean[n];
        int currentLoc = context.getStartLocIdx();
        double time = 0.0;

        for (int step = 0; step < context.getTotalStops(); step++) {
            double bestStepTime = Double.MAX_VALUE;
            int bestNextLoc = -1;
            boolean bestIsRestaurant = false;
            int bestOrderIdx = -1;

            for (int i = 0; i < n; i++) {
                if (!rVisited[i]) {
                    double arrival = time + context.getTravelTimes()[currentLoc][i];
                    double departure = Math.max(arrival, context.getPrepTimes()[i]);
                    if (departure < bestStepTime) {
                        bestStepTime = departure;
                        bestNextLoc = i;
                        bestIsRestaurant = true;
                        bestOrderIdx = i;
                    }
                }
                if (rVisited[i] && !cVisited[i]) {
                    double arrival = time + context.getTravelTimes()[currentLoc][context.consumerIdx(i)];
                    if (arrival < bestStepTime) {
                        bestStepTime = arrival;
                        bestNextLoc = context.consumerIdx(i);
                        bestIsRestaurant = false;
                        bestOrderIdx = i;
                    }
                }
            }

            if (bestIsRestaurant) {
                rVisited[bestOrderIdx] = true;
            } else {
                cVisited[bestOrderIdx] = true;
            }
            time = bestStepTime;
            currentLoc = bestNextLoc;
        }

        return time * GREEDY_BOUND_BUFFER; // small buffer so exact search can still improve
    }

    /**
     * Converts the raw index-based solution into a human-readable response.
     *
     * @param startLocation exec's starting position
     * @param orders        original order list
     * @param best          best result from the search
     * @param context       precomputed problem data
     * @return formatted response VO
     */
    private DeliveryResponseVo buildResponse(LocationVo startLocation, List<OrderVo> orders,
                                             BestResult best, RouteProblemContext context) {
        List<RouteStepVo> steps = new ArrayList<>();
        double totalDistance = 0.0;
        int prevLoc = context.getStartLocIdx();
        int n = context.getOrderCount();

        for (int depth = 0; depth < context.getTotalStops(); depth++) {
            int locIdx = best.sequence[depth];
            boolean isRestaurant = locIdx < n;
            int orderIdx = isRestaurant ? locIdx : locIdx - n;
            OrderVo order = orders.get(orderIdx);

            LocationVo location = isRestaurant
                    ? order.getRestaurantLocation()
                    : order.getConsumerLocation();

            String action = isRestaurant ?  AppConstants.PICKUP_FROM_RESTAURANT : AppConstants.DELIVER_TO_CONSUMER;

            LocationVo prevLocation = resolveLocation(prevLoc, startLocation, orders, n);
            totalDistance += distanceCalculator.calculateDistanceInKm(prevLocation, location);

            steps.add(new RouteStepVo(depth + 1, location, action, order.getOrderNumber()));
            prevLoc = locIdx;
        }

        LOGGER.info("Optimal route found - total time: {} min, distance: {} km",
                String.format(AppConstants.DECIMAL_FORMAT_TWO, best.time), String.format(AppConstants.DECIMAL_FORMAT_TWO, totalDistance));

        return new DeliveryResponseVo(
                steps,
                DeliveryTimeHelper.roundToTwoDecimals(best.time),
                DeliveryTimeHelper.roundToTwoDecimals(totalDistance),
                n,
                getStrategyName()
        );
    }

    /**
     * Maps a location index back to the actual {@link LocationVo}.
     *
     * @param locIdx index in the travel time matrix
     * @param start  exec's start location
     * @param orders order list
     * @param n      number of orders
     * @return the corresponding location
     */
    private LocationVo resolveLocation(int locIdx, LocationVo start, List<OrderVo> orders, int n) {
        if (locIdx == 2 * n) {
            return start;
        }
        if (locIdx < n) {
            return orders.get(locIdx).getRestaurantLocation();
        }
        return orders.get(locIdx - n).getConsumerLocation();
    }

    /**
     * Mutable recursion state - passed through and backtracked at each branch.
     */
    private static class SearchState {
        final boolean[] restaurantVisited;
        final boolean[] consumerVisited;
        final int[] currentSequence;
        int depth;
        double currentTime;

        SearchState(int orderCount) {
            this.restaurantVisited = new boolean[orderCount];
            this.consumerVisited = new boolean[orderCount];
            this.currentSequence = new int[2 * orderCount];
            this.depth = 0;
            this.currentTime = 0.0;
        }
    }

    /**
     * Tracks the best (fastest) solution found across all branches.
     */
    private static class BestResult {
        double time;
        final int[] sequence;

        BestResult(int totalStops, double initialBound) {
            this.time = initialBound;
            this.sequence = new int[totalStops];
        }
    }
}