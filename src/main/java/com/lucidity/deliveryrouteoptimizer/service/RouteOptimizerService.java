package com.lucidity.deliveryrouteoptimizer.service;

import com.lucidity.deliveryrouteoptimizer.vo.DeliveryRequestVo;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryResponseVo;
import com.lucidity.deliveryrouteoptimizer.strategy.RoutingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Handles delivery route optimization requests. Delegates the actual
 * computation to the injected {@link RoutingStrategy}.
 *
 * @author Rithik Jain
 */
@Service
public class RouteOptimizerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteOptimizerService.class);

    private final RoutingStrategy routingStrategy;

    /**
     * @param routingStrategy the active routing strategy
     */
    public RouteOptimizerService(RoutingStrategy routingStrategy) {
        this.routingStrategy = routingStrategy;
        LOGGER.info("RouteOptimizerService initialized with strategy: {}", routingStrategy.getStrategyName());
    }

    /**
     * Optimizes the delivery route for the given request.
     *
     * @param request the delivery batch details
     * @return the optimized delivery plan
     */
    public DeliveryResponseVo optimizeRoute(DeliveryRequestVo request) {
        LOGGER.info("Received optimization request with {} orders", request.getOrders().size());

        long startMs = System.currentTimeMillis();

        DeliveryResponseVo response = routingStrategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(),
                request.getOrders()
        );

        long elapsedMs = System.currentTimeMillis() - startMs;
        LOGGER.info("Route optimization completed in {} ms", elapsedMs);

        return response;
    }
}