package com.lucidity.deliveryrouteoptimizer.distance;

import com.lucidity.deliveryrouteoptimizer.vo.LocationVo;

/**
 * Contract for calculating distances between geographic locations.
 *
 * <p>Allows swapping Haversine for road-distance APIs (Google Maps,
 * OSRM, etc.) without touching any strategy code.</p>
 *
 * @author Rithik Jain
 */
public interface DistanceCalculator {

    /**
     * Calculates the distance between two locations in kilometres.
     *
     * @param from the starting location
     * @param to   the destination location
     * @return the distance in kilometres
     */
    double calculateDistanceInKm(LocationVo from, LocationVo to);

    /**
     * Calculates the travel time between two locations.
     *
     * @param from      the starting location
     * @param to        the destination location
     * @param speedKmph the average speed in km/hr
     * @return the travel time in minutes
     */
    double calculateTravelTimeInMinutes(LocationVo from, LocationVo to, double speedKmph);
}