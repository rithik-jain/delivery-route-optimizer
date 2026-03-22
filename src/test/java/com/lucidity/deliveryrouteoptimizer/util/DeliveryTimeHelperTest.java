package com.lucidity.deliveryrouteoptimizer.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DeliveryTimeHelper}.
 *
 * @author Rithik Jain
 */
@DisplayName("Delivery Time Helper")
class DeliveryTimeHelperTest {

    @Test
    @DisplayName("should round to two decimal places")
    void roundsToTwoDecimals() {
        assertEquals(12.35, DeliveryTimeHelper.roundToTwoDecimals(12.3456));
        assertEquals(0.0, DeliveryTimeHelper.roundToTwoDecimals(0.0));
        assertEquals(100.0, DeliveryTimeHelper.roundToTwoDecimals(100.0));
        assertEquals(7.78, DeliveryTimeHelper.roundToTwoDecimals(7.776));
    }

    @Test
    @DisplayName("should handle negative values")
    void handlesNegativeValues() {
        assertEquals(-3.14, DeliveryTimeHelper.roundToTwoDecimals(-3.14159));
    }

    @Test
    @DisplayName("should not change values already at two decimals")
    void noChangeForAlreadyRounded() {
        assertEquals(5.55, DeliveryTimeHelper.roundToTwoDecimals(5.55));
    }
}
