package com.lucidity.deliveryrouteoptimizer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucidity.deliveryrouteoptimizer.helper.TestDataFactory;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryRequestVo;
import com.lucidity.deliveryrouteoptimizer.vo.LocationVo;
import com.lucidity.deliveryrouteoptimizer.vo.OrderVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link RouteController}.
 *
 * <p>These boot the full Spring context and hit the actual HTTP endpoints.
 * We verify the happy path, input validation, and error handling end-to-end.</p>
 *
 * @author Rithik Jain
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Route Controller (Integration)")
class RouteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/v1/routes/health")
    class HealthCheck {

        @Test
        @DisplayName("should return 200 with status message")
        void healthCheckReturns200() throws Exception {
            mockMvc.perform(get("/api/v1/routes/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("up and running")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/routes/optimize")
    class OptimizeRoute {

        @Test
        @DisplayName("happy path — two orders should return a valid route")
        void happyPathTwoOrders() throws Exception {
            DeliveryRequestVo request = TestDataFactory.twoOrderScenario();

            mockMvc.perform(post("/api/v1/routes/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.route", hasSize(4)))
                    .andExpect(jsonPath("$.totalTimeInMinutes", greaterThan(0.0)))
                    .andExpect(jsonPath("$.totalDistanceInKm", greaterThan(0.0)))
                    .andExpect(jsonPath("$.totalOrders").value(2))
                    .andExpect(jsonPath("$.strategy").isNotEmpty());
        }

        @Test
        @DisplayName("single order should work fine")
        void singleOrder() throws Exception {
            DeliveryRequestVo request = TestDataFactory.singleOrderScenario();

            mockMvc.perform(post("/api/v1/routes/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.route", hasSize(2)))
                    .andExpect(jsonPath("$.route[0].action", containsString("PICKUP")))
                    .andExpect(jsonPath("$.route[1].action", containsString("DELIVER")));
        }

        @Test
        @DisplayName("three orders should return 6 steps")
        void threeOrders() throws Exception {
            DeliveryRequestVo request = TestDataFactory.threeOrderScenario();

            mockMvc.perform(post("/api/v1/routes/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.route", hasSize(6)))
                    .andExpect(jsonPath("$.totalOrders").value(3));
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("missing delivery executive location should return 400")
        void missingExecutiveLocation() throws Exception {
            DeliveryRequestVo request = new DeliveryRequestVo(
                    null,
                    List.of(new OrderVo(TestDataFactory.INDIRANAGAR, TestDataFactory.HSR_LAYOUT, 10.0))
            );

            mockMvc.perform(post("/api/v1/routes/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.messages", not(empty())));
        }

        @Test
        @DisplayName("empty orders list should return 400")
        void emptyOrders() throws Exception {
            DeliveryRequestVo request = new DeliveryRequestVo(
                    TestDataFactory.KORAMANGALA,
                    Collections.emptyList()
            );

            mockMvc.perform(post("/api/v1/routes/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.messages", not(empty())));
        }

        @Test
        @DisplayName("invalid latitude should return 400")
        void invalidLatitude() throws Exception {
            DeliveryRequestVo request = new DeliveryRequestVo(
                    new LocationVo(999.0, 77.5946), // latitude out of range
                    List.of(new OrderVo(TestDataFactory.INDIRANAGAR, TestDataFactory.HSR_LAYOUT, 10.0))
            );

            mockMvc.perform(post("/api/v1/routes/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("negative prep time should return 400")
        void negativePrepTime() throws Exception {
            DeliveryRequestVo request = new DeliveryRequestVo(
                    TestDataFactory.KORAMANGALA,
                    List.of(new OrderVo(TestDataFactory.INDIRANAGAR, TestDataFactory.HSR_LAYOUT, -5.0))
            );

            mockMvc.perform(post("/api/v1/routes/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("malformed JSON should return 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/v1/routes/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{this is not json"))
                    .andExpect(status().isBadRequest());
        }
    }
}
