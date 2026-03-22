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
 * Tests for {@link PermutationRoutingStrategy}.
 *
 * <p>Since this is the exact solver, we can make strong assertions
 * about the structure and validity of the result - every order must
 * be picked up before being delivered, all orders must appear, and
 * the time/distance must be positive.</p>
 *
 * @author Rithik Jain
 */
@DisplayName("Permutation (Exact) Routing Strategy")
class PermutationRoutingStrategyTest {

    private PermutationRoutingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PermutationRoutingStrategy(new HaversineCalculator(), 20.0);
    }

    @Test
    @DisplayName("single order should produce exactly 2 steps: pickup then delivery")
    void singleOrderProducesTwoSteps() {
        DeliveryRequestVo request = TestDataFactory.singleOrderScenario();

        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertEquals(2, response.getRoute().size(), "Should have exactly 2 steps");
        assertTrue(response.getRoute().get(0).getAction().contains("PICKUP"),
                "First step should be a pickup");
        assertTrue(response.getRoute().get(1).getAction().contains("DELIVER"),
                "Second step should be a delivery");
    }

    @Test
    @DisplayName("two orders should produce 4 steps with valid precedence")
    void twoOrdersProduceFourSteps() {
        DeliveryRequestVo request = TestDataFactory.twoOrderScenario();

        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertEquals(4, response.getRoute().size(), "Should have 4 steps for 2 orders");
        assertPrecedenceConstraintHolds(response.getRoute());
    }

    @Test
    @DisplayName("three orders should still produce correct results")
    void threeOrdersHandledCorrectly() {
        DeliveryRequestVo request = TestDataFactory.threeOrderScenario();

        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertEquals(6, response.getRoute().size(), "Should have 6 steps for 3 orders");
        assertEquals(3, response.getTotalOrders());
        assertPrecedenceConstraintHolds(response.getRoute());
    }

    @Test
    @DisplayName("total time and distance should be positive")
    void timeAndDistanceShouldBePositive() {
        DeliveryRequestVo request = TestDataFactory.twoOrderScenario();

        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertTrue(response.getTotalTimeInMinutes() > 0, "Total time should be positive");
        assertTrue(response.getTotalDistanceInKm() > 0, "Total distance should be positive");
    }

    @Test
    @DisplayName("long prep time should not break the algorithm")
    void handlesLongPrepTime() {
        DeliveryRequestVo request = TestDataFactory.longPrepTimeScenario();

        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        assertTrue(response.getTotalTimeInMinutes() >= 60.0,
                "Total time should account for the long prep time");
        assertPrecedenceConstraintHolds(response.getRoute());
    }

    @Test
    @DisplayName("strategy name should be descriptive")
    void strategyNameIsDescriptive() {
        assertNotNull(strategy.getStrategyName());
        assertFalse(strategy.getStrategyName().isBlank());
    }

    @Test
    @DisplayName("all orders should appear in the route exactly once for pickup and once for delivery")
    void allOrdersCoveredExactlyOnce() {
        DeliveryRequestVo request = TestDataFactory.threeOrderScenario();

        DeliveryResponseVo response = strategy.findOptimalRoute(
                request.getDeliveryExecutiveLocation(), request.getOrders());

        Set<Integer> pickups = new HashSet<>();
        Set<Integer> deliveries = new HashSet<>();

        for (RouteStepVo step : response.getRoute()) {
            String key = String.valueOf(step.getOrderNumber());
            if (step.getAction().contains("PICKUP")) {
                assertTrue(pickups.add(step.getOrderNumber()),
                        "Order " + key + " should only be picked up once");
            } else {
                assertTrue(deliveries.add(step.getOrderNumber()),
                        "Order " + key + " should only be delivered once");
            }
        }

        assertEquals(3, pickups.size(), "All 3 orders should have a pickup");
        assertEquals(3, deliveries.size(), "All 3 orders should have a delivery");
    }

    /**
     * Checks that every consumer delivery happens AFTER the corresponding restaurant pickup.
     */
    private void assertPrecedenceConstraintHolds(List<RouteStepVo> route) {
        Set<Integer> pickedUp = new HashSet<>();

        for (RouteStepVo step : route) {
            if (step.getAction().contains("PICKUP")) {
                pickedUp.add(step.getOrderNumber());
            } else if (step.getAction().contains("DELIVER")) {
                assertTrue(pickedUp.contains(step.getOrderNumber()),
                        "Order " + step.getOrderNumber()
                                + " was delivered before being picked up! (Step " + step.getStepNumber() + ")");
            }
        }
    }
}
