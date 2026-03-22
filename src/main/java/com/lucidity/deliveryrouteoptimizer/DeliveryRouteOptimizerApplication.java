package com.lucidity.deliveryrouteoptimizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Delivery Route Optimizer application.
 *
 * <p>This service helps delivery executives figure out the fastest way
 * to complete a batch of N orders - accounting for restaurant prep times,
 * real-world distances (via Haversine), and the constraint that food
 * must be picked up before it can be delivered</p>
 *
 * @author Rithik Jain
 */
@SpringBootApplication
public class DeliveryRouteOptimizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryRouteOptimizerApplication.class, args);
    }
}
