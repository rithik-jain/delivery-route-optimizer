package com.lucidity.deliveryrouteoptimizer.strategy;

import com.lucidity.deliveryrouteoptimizer.helper.TestDataFactory;
import com.lucidity.deliveryrouteoptimizer.distance.HaversineCalculator;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryRequestVo;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryResponseVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CompositeRoutingStrategy}.
 *
 * <p>Verifies that the composite correctly delegates to the exact
 * solver for small batches and the greedy heuristic for larger ones,
 * by asserting on the strategy name in the response.</p>
 *
 * @author Rithik Jain
 */

@DisplayName("Composite (Adaptive) Routing Strategy")
class CompositeRoutingStrategyTest {

    private CompositeRoutingStrategy compositeStrategy;

    private static final int THRESHOLD = 5;

    void setUp() {
        compositeStrategy = new CompositeRoutingStrategy(new HaversineCalculator(), THRESHOLD, 20.0);
    }

    @Test
    @DisplayName("small batch should use exact strategy")
    void usesExactForSmallBatches() {
        DeliveryRequestVo request = TestDataFactory.twoOrderScenario();

        DeliveryResponseVo result = compositeStrategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertNotNull(result);
        assertTrue(result.getStrategy().contains("Exact"));
    }

    @Test
    @DisplayName("large batch should use greedy strategy")
    void usesGreedyForLargeBatches() {
        DeliveryRequestVo request = TestDataFactory.largeOrderScenario(10);

        DeliveryResponseVo result = compositeStrategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertNotNull(result);
        assertTrue(result.getStrategy().contains("Greedy"));
    }

    @Test
    @DisplayName("batch at exact threshold boundary should use exact strategy")
    void usesExactAtBoundary() {
        DeliveryRequestVo request = TestDataFactory.largeOrderScenario(THRESHOLD);

        DeliveryResponseVo result = compositeStrategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertTrue(result.getStrategy().contains("Exact"));
    }

    @Test
    @DisplayName("strategy name should mention adaptive behaviour")
    void strategyNameIsDescriptive() {
        String name = compositeStrategy.getStrategyName();

        assertNotNull(name);
        assertTrue(name.contains("Adaptive"));
    }
}