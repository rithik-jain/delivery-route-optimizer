package com.lucidity.deliveryrouteoptimizer.service;

import com.lucidity.deliveryrouteoptimizer.helper.TestDataFactory;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryRequestVo;
import com.lucidity.deliveryrouteoptimizer.vo.DeliveryResponseVo;
import com.lucidity.deliveryrouteoptimizer.strategy.RoutingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link RouteOptimizerService}.
 *
 * <p>The service layer is thin on purpose — it delegates to the strategy.
 * These tests mainly verify that delegation happens correctly and that
 * the service doesn't silently swallow errors.</p>
 *
 * @author Rithik Jain
 */
@DisplayName("Route Optimizer Service")
@ExtendWith(MockitoExtension.class)
class RouteOptimizerServiceTest {

    @Mock
    private RoutingStrategy routingStrategy;

    private RouteOptimizerService service;

    @BeforeEach
    void setUp() {
        when(routingStrategy.getStrategyName()).thenReturn("MockStrategy");
        service = new RouteOptimizerService(routingStrategy);
    }

    @Test
    @DisplayName("should delegate to routing strategy and return its response")
    void delegatesToStrategy() {
        DeliveryRequestVo request = TestDataFactory.twoOrderScenario();
        DeliveryResponseVo expectedResponse = new DeliveryResponseVo(
                List.of(), 45.0, 12.0, 2, "MockStrategy");
        when(routingStrategy.findOptimalRoute(any(), anyList())).thenReturn(expectedResponse);

        DeliveryResponseVo actualResponse = service.optimizeRoute(request);

        verify(routingStrategy).findOptimalRoute(
                request.getDeliveryExecutiveLocation(),
                request.getOrders());
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("should propagate exceptions from the strategy")
    void propagatesExceptions() {
        DeliveryRequestVo request = TestDataFactory.singleOrderScenario();
        when(routingStrategy.findOptimalRoute(any(), anyList()))
                .thenThrow(new RuntimeException("Algorithm failed"));

        assertThrows(RuntimeException.class, () -> service.optimizeRoute(request));
    }
}
