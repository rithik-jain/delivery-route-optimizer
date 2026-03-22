package com.lucidity.deliveryrouteoptimizer.distance;

import com.lucidity.deliveryrouteoptimizer.vo.LocationVo;
import org.springframework.stereotype.Component;

/**
 * Haversine implementation of {@link DistanceCalculator}.
 *
 * <p>Computes the great-circle ("as the crow flies") distance between
 * two points on a sphere. Not perfect for city roads, but it's what
 * the assignment specifies and a solid default.</p>
 *
 * @author Rithik Jain
 */
@Component
public class HaversineCalculator implements DistanceCalculator {

    /** Earth's mean radius in kilometres. */
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public double calculateDistanceInKm(LocationVo from, LocationVo to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Locations must not be null");
        }

        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double latDiff = Math.toRadians(to.getLatitude() - from.getLatitude());
        double lonDiff = Math.toRadians(to.getLongitude() - from.getLongitude());

        double a = Math.pow(Math.sin(latDiff / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(lonDiff / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        return EARTH_RADIUS_KM * c;
    }

    @Override
    public double calculateTravelTimeInMinutes(LocationVo from, LocationVo to, double speedKmph) {
        if (speedKmph <= 0) {
            throw new IllegalArgumentException("Speed must be positive, got: " + speedKmph);
        }
        double distanceKm = calculateDistanceInKm(from, to);
        return (distanceKm / speedKmph) * 60.0;
    }
}