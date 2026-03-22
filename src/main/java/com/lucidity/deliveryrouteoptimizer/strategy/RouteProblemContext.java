package com.lucidity.deliveryrouteoptimizer.strategy;

import com.lucidity.deliveryrouteoptimizer.distance.DistanceCalculator;
import com.lucidity.deliveryrouteoptimizer.vo.LocationVo;
import com.lucidity.deliveryrouteoptimizer.vo.OrderVo;

import java.util.List;

/**
 * Precomputed travel-time matrix and prep times for a delivery batch.
 * Built once, shared across strategies so Haversine isn't called repeatedly.
 *
 * <p>Location index convention:<br>
 * 0..n-1 = restaurants, n..2n-1 = consumers, 2n = exec start position.</p>
 *
 * @author Rithik Jain
 */
class RouteProblemContext {

    private final double[][] travelTimes;
    private final double[] prepTimes;
    private final int orderCount;
    private final int startLocIdx;

    private RouteProblemContext(double[][] travelTimes, double[] prepTimes,
                                int orderCount, int startLocIdx) {
        this.travelTimes = travelTimes;
        this.prepTimes = prepTimes;
        this.orderCount = orderCount;
        this.startLocIdx = startLocIdx;
    }

    /**
     * Builds the context by precomputing all pairwise travel times.
     *
     * @param start              exec's starting location
     * @param orders             the batch of orders
     * @param distanceCalculator distance calculation implementation
     * @param speedKmph          average delivery speed in km/hr
     * @return fully initialized context
     */
    static RouteProblemContext build(LocationVo start, List<OrderVo> orders,
                                            DistanceCalculator distanceCalculator,
                                            double speedKmph) {
        int n = orders.size();
        int totalLocations = 2 * n + 1;

        LocationVo[] locations = new LocationVo[totalLocations];
        for (int i = 0; i < n; i++) {
            locations[i] = orders.get(i).getRestaurantLocation();
            locations[n + i] = orders.get(i).getConsumerLocation();
        }
        locations[2 * n] = start;

        double[][] times = new double[totalLocations][totalLocations];
        for (int i = 0; i < totalLocations; i++) {
            for (int j = i + 1; j < totalLocations; j++) {
                double t = distanceCalculator.calculateTravelTimeInMinutes(
                        locations[i], locations[j], speedKmph);
                times[i][j] = t;
                times[j][i] = t;
            }
        }

        double[] prepTimes = orders.stream()
                .mapToDouble(OrderVo::getMealPreparationTimeInMinutes)
                .toArray();

        return new RouteProblemContext(times, prepTimes, n, 2 * n);
    }

    /** @return precomputed pairwise travel time matrix */
    double[][] getTravelTimes() {
        return travelTimes;
    }

    /** @return restaurant prep times indexed by order */
    double[] getPrepTimes() {
        return prepTimes;
    }

    /** @return number of orders in the batch */
    int getOrderCount() {
        return orderCount;
    }

    /** @return index of the exec's start location in the matrix */
    int getStartLocIdx() {
        return startLocIdx;
    }

    /** @return total stops to visit (2 * orderCount) */
    int getTotalStops() {
        return 2 * orderCount;
    }

    /**
     * Returns the consumer location index for a given order.
     *
     * @param orderIdx zero-based order index
     * @return the index in the travel time matrix
     */
    int consumerIdx(int orderIdx) {
        return orderCount + orderIdx;
    }
}