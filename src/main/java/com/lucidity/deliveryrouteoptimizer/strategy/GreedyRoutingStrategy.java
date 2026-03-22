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
 * A greedy "smart nearest-neighbour" heuristic for larger order batches.
 *
 * <p>At each step, evaluates all legal next moves and picks the one with
 * the lowest effective cost — a blend of travel time and idle waiting.
 * Runs in O(N²), won't always find THE best route, but finds a good one fast.</p>
 *
 * <p>Pickups and deliveries are freely interleaved. A consumer is only
 * eligible once their restaurant has been visited.</p>
 *
 * @author Rithik Jain
 */
public class GreedyRoutingStrategy implements RoutingStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreedyRoutingStrategy.class);

    /** Penalty weight for idle time — discourages waiting at restaurants. */
    private static final double WAIT_PENALTY_WEIGHT = 0.8;

    /** Slight bonus for deliveries — avoids holding food too long. */
    private static final double DELIVERY_COST_FACTOR = 0.95;

    private final DistanceCalculator distanceCalculator;
    private final double averageSpeedKmph;

    /**
     * @param distanceCalculator distance calculation implementation
     * @param averageSpeedKmph   average delivery speed in km/hr
     */
    public GreedyRoutingStrategy(DistanceCalculator distanceCalculator,
                                 double averageSpeedKmph) {
        this.distanceCalculator = distanceCalculator;
        this.averageSpeedKmph = averageSpeedKmph;
    }

    @Override
    public String getStrategyName() {
        return "Smart Greedy Nearest-Neighbour (Heuristic)";
    }

    /**
     * {@inheritDoc}
     *
     * @param startLocation exec's current position
     * @param orders        batch of orders to deliver
     * @return the heuristic-optimized route
     */
    @Override
    public DeliveryResponseVo findOptimalRoute(LocationVo startLocation, List<OrderVo> orders) {
        int n = orders.size();
        LOGGER.info("Running greedy heuristic for {} orders from {}", n, startLocation);

        RouteProblemContext context = RouteProblemContext.build(
                startLocation, orders, distanceCalculator, averageSpeedKmph);

        boolean[] restaurantVisited = new boolean[n];
        boolean[] consumerVisited = new boolean[n];
        int currentLoc = context.getStartLocIdx();
        double currentTime = 0.0;

        List<RouteStepVo> steps = new ArrayList<>();

        for (int s = 0; s < context.getTotalStops(); s++) {
            BestCandidate candidate = findBestNextStop(
                    context, restaurantVisited, consumerVisited, currentLoc, currentTime);

            applyCandidate(candidate, restaurantVisited, consumerVisited);
            currentTime = candidate.arrivalTime;
            currentLoc = candidate.locationIdx;

            steps.add(buildStep(s + 1, candidate, orders));
        }

        double totalDistance = calculateTotalDistance(startLocation, steps);

        LOGGER.info("Greedy route computed — total time: {} min, distance: {} km",
                String.format(AppConstants.DECIMAL_FORMAT_TWO, currentTime), String.format(AppConstants.DECIMAL_FORMAT_TWO, totalDistance));

        return new DeliveryResponseVo(
                steps,
                DeliveryTimeHelper.roundToTwoDecimals(currentTime),
                DeliveryTimeHelper.roundToTwoDecimals(totalDistance),
                n,
                getStrategyName()
        );
    }

    /**
     * Scans all legal next stops and picks the cheapest one.
     *
     * @param context            precomputed problem data
     * @param restaurantVisited  which restaurants have been visited
     * @param consumerVisited    which consumers have been delivered to
     * @param currentLoc         current position index in travel matrix
     * @param currentTime        time elapsed so far
     * @return the best candidate stop
     */
    private BestCandidate findBestNextStop(RouteProblemContext context,
                                           boolean[] restaurantVisited,
                                           boolean[] consumerVisited,
                                           int currentLoc, double currentTime) {
        double bestCost = Double.MAX_VALUE;
        BestCandidate best = null;
        int n = context.getOrderCount();

        for (int i = 0; i < n; i++) {
            // Try pickup from restaurant i
            if (!restaurantVisited[i]) {
                double travelTime = context.getTravelTimes()[currentLoc][i];
                double arrival = currentTime + travelTime;
                double prepTime = context.getPrepTimes()[i];
                double waitTime = Math.max(0, prepTime - arrival);
                double departure = Math.max(arrival, prepTime);
                double cost = (departure - currentTime) + (waitTime * WAIT_PENALTY_WEIGHT);

                if (cost < bestCost) {
                    bestCost = cost;
                    best = new BestCandidate(i, i, true, departure);
                }
            }

            // Try delivery to consumer i
            if (restaurantVisited[i] && !consumerVisited[i]) {
                double travelTime = context.getTravelTimes()[currentLoc][context.consumerIdx(i)];
                double arrival = currentTime + travelTime;
                double cost = travelTime * DELIVERY_COST_FACTOR;

                if (cost < bestCost) {
                    bestCost = cost;
                    best = new BestCandidate(i, context.consumerIdx(i), false, arrival);
                }
            }
        }

        return best;
    }

    /**
     * Marks the chosen candidate as visited.
     *
     * @param candidate          the chosen next stop
     * @param restaurantVisited  restaurant visit tracker
     * @param consumerVisited    consumer visit tracker
     */
    private void applyCandidate(BestCandidate candidate, boolean[] restaurantVisited,
                                boolean[] consumerVisited) {
        if (candidate.isPickup) {
            restaurantVisited[candidate.orderIdx] = true;
        } else {
            consumerVisited[candidate.orderIdx] = true;
        }
    }

    /**
     * Builds a route step from the chosen candidate.
     *
     * @param stepNumber 1-based step number
     * @param candidate  the chosen stop
     * @param orders     original order list
     * @return formatted route step
     */
    private RouteStepVo buildStep(int stepNumber, BestCandidate candidate, List<OrderVo> orders) {
        OrderVo order = orders.get(candidate.orderIdx);
        LocationVo location = candidate.isPickup
                ? order.getRestaurantLocation()
                : order.getConsumerLocation();
        String action = candidate.isPickup ? "PICKUP from Restaurant" : "DELIVER to Consumer";
        return new RouteStepVo(stepNumber, location, action, candidate.orderIdx + 1);
    }

    /**
     * Calculates the total distance across all steps in the route.
     *
     * @param startLocation exec's starting position
     * @param steps         the built route steps
     * @return total distance in km
     */
    private double calculateTotalDistance(LocationVo startLocation, List<RouteStepVo> steps) {
        double totalDistance = 0.0;
        LocationVo prev = startLocation;
        for (RouteStepVo step : steps) {
            totalDistance += distanceCalculator.calculateDistanceInKm(prev, step.getLocation());
            prev = step.getLocation();
        }
        return totalDistance;
    }

    /**
     * Holds the evaluation result for a candidate next stop.
     */
    private record BestCandidate(int orderIdx, int locationIdx, boolean isPickup, double arrivalTime) {}
}