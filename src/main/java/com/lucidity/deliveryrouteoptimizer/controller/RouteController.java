package com.lucidity.deliveryrouteoptimizer.controller;

import com.lucidity.deliveryrouteoptimizer.vo.DeliveryRequestVo;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryResponseVo;
import com.lucidity.deliveryrouteoptimizer.service.RouteOptimizerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST endpoint for finding the optimal delivery route.
 *
 * <p>Single endpoint : take a batch of orders and the delivery exec's location,
 * return the fastest sequence to deliver everything.</p>
 *
 * @author Rithik Jain
 */
@RestController
@RequestMapping("/api/v1/routes")
@Tag(name = "Route Optimization", description = "Finds the fastest delivery sequence for a batch of orders")
public class RouteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteController.class);

    private final RouteOptimizerService routeOptimizerService;

    public RouteController(RouteOptimizerService routeOptimizerService) {
        this.routeOptimizerService = routeOptimizerService;
    }

    /**
     * Computes the optimal delivery route for a batch of orders.
     *
     * <p>Send in the delivery executive's current position and a list of
     * orders (each with restaurant location, consumer location, and meal
     * prep time). Get back the fastest sequence of pickups and deliveries.</p>
     *
     * @param request the delivery batch details
     * @return the optimized route with total time and distance
     */
    @Operation(
            summary = "Optimize delivery route",
            description = "Computes the fastest pickup-and-delivery sequence for a batch of orders"
    )
    @PostMapping("/optimize")
    public ResponseEntity<DeliveryResponseVo> optimizeRoute(@Valid @RequestBody DeliveryRequestVo request) {
        LOGGER.info("POST /optimize — {} orders from location {}",
                request.getOrders().size(), request.getDeliveryExecutiveLocation());

        DeliveryResponseVo response = routeOptimizerService.optimizeRoute(request);

        LOGGER.info("Route optimized — total time: {} min, distance: {} km, strategy: {}",
                response.getTotalTimeInMinutes(), response.getTotalDistanceInKm(), response.getStrategy());

        return ResponseEntity.ok(response);
    }

    /**
     * Quick health check — is the service up?
     *
     * @return a simple status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        LOGGER.debug("Health check endpoint hit");
        return ResponseEntity.ok("Delivery Route Optimizer is up and running!");
    }
}
