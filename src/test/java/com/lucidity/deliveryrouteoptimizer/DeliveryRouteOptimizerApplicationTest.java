package com.lucidity.deliveryrouteoptimizer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test — verifies the Spring context loads without errors.
 *
 * @author Rithik Jain
 */
@SpringBootTest
@DisplayName("Application Context")
class DeliveryRouteOptimizerApplicationTest {

    @Test
    @DisplayName("context should load successfully")
    void contextLoads() {
        // If we get here without an exception, the context is wired up correctly.
    }
}
