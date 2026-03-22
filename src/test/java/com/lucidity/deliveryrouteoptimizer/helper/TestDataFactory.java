package com.lucidity.deliveryrouteoptimizer.helper;

import com.lucidity.deliveryrouteoptimizer.vo.DeliveryRequestVo;
import com.lucidity.deliveryrouteoptimizer.vo.LocationVo;
import com.lucidity.deliveryrouteoptimizer.vo.OrderVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for building test data. Keeps the actual test methods clean and
 * focused on assertions rather than setup boilerplate.
 *
 * <p>All locations are in Bangalore — the assignment mentions Koramangala,
 * so we're keeping things geographically consistent.</p>
 *
 * @author Rithik Jain
 */
public final class TestDataFactory {

    // --- Bangalore landmarks as test locations ---

    /** Aman's starting position — Koramangala, as mentioned in the problem statement */
    public static final LocationVo KORAMANGALA = new LocationVo(12.9352, 77.6245);

    /** Indiranagar — popular restaurant hub */
    public static final LocationVo INDIRANAGAR = new LocationVo(12.9784, 77.6408);

    /** HSR Layout */
    public static final LocationVo HSR_LAYOUT = new LocationVo(12.9116, 77.6474);

    /** BTM Layout */
    public static final LocationVo BTM_LAYOUT = new LocationVo(12.9166, 77.6101);

    /** Jayanagar */
    public static final LocationVo JAYANAGAR = new LocationVo(12.9299, 77.5838);

    /** Whitefield — far east */
    public static final LocationVo WHITEFIELD = new LocationVo(12.9698, 77.7500);

    /** Electronic City — far south */
    public static final LocationVo ELECTRONIC_CITY = new LocationVo(12.8399, 77.6770);

    /** MG Road */
    public static final LocationVo MG_ROAD = new LocationVo(12.9756, 77.6065);

    /** Marathahalli */
    public static final LocationVo MARATHAHALLI = new LocationVo(12.9591, 77.6974);

    /** JP Nagar */
    public static final LocationVo JP_NAGAR = new LocationVo(12.9063, 77.5857);

    private TestDataFactory() {
        // utility class
    }

    /**
     * Builds a simple 2-order scenario — the classic case from the problem statement.
     *
     * <p>Order 1: Restaurant in Indiranagar, deliver to HSR Layout, 15 min prep</p>
     * <p>Order 2: Restaurant in BTM Layout, deliver to Jayanagar, 25 min prep</p>
     */
    public static DeliveryRequestVo twoOrderScenario() {
        List<OrderVo> orderVos = List.of(
                new OrderVo(INDIRANAGAR, HSR_LAYOUT, 15.0),
                new OrderVo(BTM_LAYOUT, JAYANAGAR, 25.0)
        );
        return new DeliveryRequestVo(KORAMANGALA, orderVos);
    }

    /**
     * Single order — simplest possible case.
     */
    public static DeliveryRequestVo singleOrderScenario() {
        OrderVo orderVo = new OrderVo(INDIRANAGAR, HSR_LAYOUT, 10.0);
        return new DeliveryRequestVo(KORAMANGALA, List.of(orderVo));
    }

    /**
     * Three orders — still small enough for exact solving, but tests more permutations.
     */
    public static DeliveryRequestVo threeOrderScenario() {
        List<OrderVo> orderVos = List.of(
                new OrderVo(INDIRANAGAR, HSR_LAYOUT, 15.0),
                new OrderVo(BTM_LAYOUT, JAYANAGAR, 20.0),
                new OrderVo(MG_ROAD, ELECTRONIC_CITY, 10.0)
        );
        return new DeliveryRequestVo(KORAMANGALA, orderVos);
    }

    /**
     * Creates a batch of N orders with varying prep times and scattered locations.
     * Useful for stress-testing the greedy strategy.
     */
    public static DeliveryRequestVo largeOrderScenario(int n) {
        LocationVo[] restaurants = {
                INDIRANAGAR, BTM_LAYOUT, MG_ROAD, WHITEFIELD, MARATHAHALLI,
                JAYANAGAR, JP_NAGAR, ELECTRONIC_CITY, HSR_LAYOUT, KORAMANGALA
        };
        LocationVo[] consumers = {
                HSR_LAYOUT, JAYANAGAR, ELECTRONIC_CITY, KORAMANGALA, JP_NAGAR,
                MG_ROAD, WHITEFIELD, INDIRANAGAR, BTM_LAYOUT, MARATHAHALLI
        };

        List<OrderVo> orderVos = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            LocationVo restaurant = restaurants[i % restaurants.length];
            LocationVo consumer = consumers[i % consumers.length];
            // Vary prep times between 5 and 30 minutes
            double prepTime = 5.0 + (i * 3.7 % 25);
            orderVos.add(new OrderVo(restaurant, consumer, prepTime));
        }

        return new DeliveryRequestVo(KORAMANGALA, orderVos);
    }

    /**
     * An order where the restaurant has a very long prep time — tests that
     * the algorithm handles waiting correctly.
     */
    public static DeliveryRequestVo longPrepTimeScenario() {
        List<OrderVo> orderVos = List.of(
                new OrderVo(INDIRANAGAR, HSR_LAYOUT, 60.0),  // 1 hour prep!
                new OrderVo(BTM_LAYOUT, JAYANAGAR, 5.0)       // ready almost instantly
        );
        return new DeliveryRequestVo(KORAMANGALA, orderVos);
    }
}
