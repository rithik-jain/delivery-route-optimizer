package com.lucidity.deliveryrouteoptimizer.strategy;

import com.lucidity.deliveryrouteoptimizer.vo.DeliveryResponseVo;
import com.lucidity.deliveryrouteoptimizer.vo.LocationVo;
import com.lucidity.deliveryrouteoptimizer.vo.OrderVo;

import java.util.List;

/**
 * Contract for any route optimization algorithm.
 *
 * <p>Implement this to plug in a new solver (genetic algorithm, external
 * API call, etc.) without touching any other code.</p>
 *
 * @author Rithik Jain
 */
public interface RoutingStrategy {

    /**
     * Finds the optimal delivery route for the given orders.
     *
     * @param startLocation exec's current position
     * @param orders        the batch of orders to deliver
     * @return optimized delivery route
     */
    DeliveryResponseVo findOptimalRoute(LocationVo startLocation, List<OrderVo> orders);

    /**
     * Human-readable name for this strategy (used in logs and responses).
     *
     * @return the strategy name
     */
    String getStrategyName();
}