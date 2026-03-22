package com.lucidity.deliveryrouteoptimizer.util;

/**
 * Helper methods for delivery time calculations that are shared
 * across different routing strategies.
 *
 * <p>Keeps the strategies focused on route-finding logic instead of
 * repeating the same arithmetic in multiple places.</p>
 *
 * @author Rithik Jain
 */
public final class DeliveryTimeHelper {

    private DeliveryTimeHelper() {
        // utility class — no instances needed
    }

    /**
     * Rounds a value to two decimal places — used for clean API responses.
     *
     * @param value the raw value
     * @return the value rounded to 2 decimal places
     */
    public static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}