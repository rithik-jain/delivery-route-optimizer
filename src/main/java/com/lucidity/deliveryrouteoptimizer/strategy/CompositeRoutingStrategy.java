package com.lucidity.deliveryrouteoptimizer.strategy;

import com.lucidity.deliveryrouteoptimizer.distance.DistanceCalculator;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryResponseVo;
import com.lucidity.deliveryrouteoptimizer.vo.LocationVo;
import com.lucidity.deliveryrouteoptimizer.vo.OrderVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Automatically picks the right algorithm based on batch size.
 *
 * <p>Delegates to the exact solver for small batches (guaranteed optimal)
 * and falls back to the greedy heuristic for larger ones (fast, near-optimal).
 * The threshold and speed are configured in {@code application.yml}.</p>
 *
 * @author Rithik Jain
 */
@Component
public class CompositeRoutingStrategy implements RoutingStrategy {

    private static final Logger log = LoggerFactory.getLogger(CompositeRoutingStrategy.class);

    private final RoutingStrategy exactStrategy;
    private final RoutingStrategy greedyStrategy;
    private final int exactThreshold;

    /**
     * @param distanceCalculator distance calculation implementation
     * @param exactThreshold     max orders for exact solver (from YAML)
     * @param averageSpeedKmph   average delivery speed in km/hr (from YAML)
     */
    public CompositeRoutingStrategy(
            DistanceCalculator distanceCalculator,
            @Value("${app.routing.exact-threshold:7}") int exactThreshold,
            @Value("${app.routing.average-speed-kmph:20.0}") double averageSpeedKmph) {
        this.exactThreshold = exactThreshold;
        this.exactStrategy = new PermutationRoutingStrategy(distanceCalculator, averageSpeedKmph);
        this.greedyStrategy = new GreedyRoutingStrategy(distanceCalculator, averageSpeedKmph);

        log.info("CompositeRoutingStrategy initialized — speed: {} km/hr, exact for <={} orders, greedy otherwise",
                averageSpeedKmph, exactThreshold);
    }

    @Override
    public String getStrategyName() {
        return "Adaptive (Exact <=" + exactThreshold + ", Greedy otherwise)";
    }

    /**
     * {@inheritDoc}
     *
     * @param startLocation exec's current position
     * @param orders        batch of orders to deliver
     * @return the optimized route from whichever strategy is chosen
     */
    @Override
    public DeliveryResponseVo findOptimalRoute(LocationVo startLocation, List<OrderVo> orders) {
        int batchSize = orders.size();

        if (batchSize <= exactThreshold) {
            log.info("Batch size {} <= threshold {}. Using exact solver.", batchSize, exactThreshold);
            return exactStrategy.findOptimalRoute(startLocation, orders);
        } else {
            log.info("Batch size {} > threshold {}. Switching to greedy.", batchSize, exactThreshold);
            return greedyStrategy.findOptimalRoute(startLocation, orders);
        }
    }
}