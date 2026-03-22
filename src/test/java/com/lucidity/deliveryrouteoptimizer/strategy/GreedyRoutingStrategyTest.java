package com.lucidity.deliveryrouteoptimizer.strategy;

import com.lucidity.deliveryrouteoptimizer.distance.HaversineCalculator;
import com.lucidity.deliveryrouteoptimizer.helper.TestDataFactory;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryRequestVo;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryResponseVo;
import com.lucidity.deliveryrouteoptimizer.vo.RouteStepVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GreedyRoutingStrategy}.
 *
 * <p>The greedy strategy doesn't guarantee optimal results, so we
 * focus on structural correctness: precedence constraints hold,
 * all orders are covered, and it completes in reasonable time
 * even for larger batches.</p>
 *
 * @author Rithik Jain
 */
@DisplayName("Greedy (Heuristic) Routing Strategy")
class GreedyRoutingStrategyTest {

    private GreedyRoutingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new GreedyRoutingStrategy(new HaversineCalculator(), 20.0);
    }

    @Test
    @DisplayName("single order should produce 2 valid steps")
    void singleOrder() {
        DeliveryRequestVo request = TestDataFactory.singleOrderScenario();

        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertEquals(2, response.getRoute().size());
        assertPrecedenceConstraintHolds(response.getRoute());
    }

    @Test
    @DisplayName("two orders should maintain pickup-before-delivery constraint")
    void twoOrdersRespectPrecedence() {
        DeliveryRequestVo request = TestDataFactory.twoOrderScenario();

        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertEquals(4, response.getRoute().size());
        assertPrecedenceConstraintHolds(response.getRoute());
    }

    @Test
    @DisplayName("should handle 15 orders without blowing up")
    void handlesLargeBatches() {
        DeliveryRequestVo request = TestDataFactory.largeOrderScenario(15);

        long startMs = System.currentTimeMillis();
        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());
        long elapsedMs = System.currentTimeMillis() - startMs;

        assertEquals(30, response.getRoute().size(), "15 orders = 30 steps");
        assertEquals(15, response.getTotalOrders());
        assertPrecedenceConstraintHolds(response.getRoute());
        assertTrue(elapsedMs < 1000, "Greedy should complete in well under a second, took " + elapsedMs + " ms");
    }

    @Test
    @DisplayName("should handle 50 orders efficiently")
    void handles50Orders() {
        DeliveryRequestVo request = TestDataFactory.largeOrderScenario(50);

        long startMs = System.currentTimeMillis();
        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());
        long elapsedMs = System.currentTimeMillis() - startMs;

        assertEquals(100, response.getRoute().size());
        assertPrecedenceConstraintHolds(response.getRoute());
        assertTrue(elapsedMs < 2000, "50 orders should still be fast, took " + elapsedMs + " ms");
    }

    @Test
    @DisplayName("long prep time scenario should respect waiting")
    void respectsPrepTimeWaiting() {
        DeliveryRequestVo request = TestDataFactory.longPrepTimeScenario();

        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertTrue(response.getTotalTimeInMinutes() >= 60.0,
                "Should wait for the long-prep order");
        assertPrecedenceConstraintHolds(response.getRoute());
    }

    @Test
    @DisplayName("all orders should be covered exactly once")
    void allOrdersCovered() {
        DeliveryRequestVo request = TestDataFactory.threeOrderScenario();

        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        Set<Integer> pickedUp = new HashSet<>();
        Set<Integer> delivered = new HashSet<>();

        for (RouteStepVo step : response.getRoute()) {
            if (step.getAction().contains("PICKUP")) {
                pickedUp.add(step.getOrderNumber());
            } else {
                delivered.add(step.getOrderNumber());
            }
        }

        assertEquals(3, pickedUp.size());
        assertEquals(3, delivered.size());
        assertEquals(pickedUp, delivered, "Every picked-up order should be delivered");
    }

    private void assertPrecedenceConstraintHolds(List<RouteStepVo> route) {
        Set<Integer> pickedUp = new HashSet<>();
        for (RouteStepVo step : route) {
            if (step.getAction().contains("PICKUP")) {
                pickedUp.add(step.getOrderNumber());
            } else if (step.getAction().contains("DELIVER")) {
                assertTrue(pickedUp.contains(step.getOrderNumber()),
                        "Order " + step.getOrderNumber() + " delivered before pickup!");
            }
        }
    }
}
