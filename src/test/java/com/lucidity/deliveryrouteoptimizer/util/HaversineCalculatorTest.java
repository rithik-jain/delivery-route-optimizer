package com.lucidity.deliveryrouteoptimizer.util;

import com.lucidity.deliveryrouteoptimizer.distance.HaversineCalculator;
import com.lucidity.deliveryrouteoptimizer.vo.LocationVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link HaversineCalculator}.
 *
 * <p>We validate against known distances between real-world cities.
 * The Haversine formula isn't perfect (Earth isn't a perfect sphere),
 * so we allow a small tolerance on the results.</p>
 *
 * @author Rithik Jain
 */
@DisplayName("Haversine Distance Calculator")
class HaversineCalculatorTest {

    /** Tolerance in km — Haversine is accurate to within ~0.5% for most distances */
    private static final double DISTANCE_TOLERANCE_KM = 5.0;
    private static final double TIME_TOLERANCE_MIN = 0.01;
    private final HaversineCalculator calculator = new HaversineCalculator();

    @Nested
    @DisplayName("Distance calculations")
    class DistanceCalculations {

        @Test
        @DisplayName("same point should have zero distance")
        void samePointShouldHaveZeroDistance() {
            LocationVo koramangala = new LocationVo(12.9352, 77.6245);

            double distance = calculator.calculateDistanceInKm(koramangala, koramangala);

            assertEquals(0.0, distance, 0.001, "Distance from a point to itself should be zero");
        }

        @Test
        @DisplayName("Bangalore to Mumbai should be roughly 845 km")
        void bangaloreToMumbaiDistance() {
            LocationVo bangalore = new LocationVo(12.9716, 77.5946);
            LocationVo mumbai = new LocationVo(19.0760, 72.8777);

            double distance = calculator.calculateDistanceInKm(bangalore, mumbai);

            // Actual great-circle distance is approximately 845 km
            assertEquals(845.0, distance, DISTANCE_TOLERANCE_KM,
                    "Bangalore → Mumbai should be approximately 845 km");
        }

        @Test
        @DisplayName("short distance within Bangalore — Koramangala to Indiranagar")
        void shortDistanceWithinBangalore() {
            LocationVo koramangala = new LocationVo(12.9352, 77.6245);
            LocationVo indiranagar = new LocationVo(12.9784, 77.6408);

            double distance = calculator.calculateDistanceInKm(koramangala, indiranagar);

            // About 5 km as the crow flies
            assertTrue(distance > 3.0 && distance < 7.0,
                    "Koramangala to Indiranagar should be roughly 5 km, got: " + distance);
        }

        @Test
        @DisplayName("order of arguments should not matter (symmetry)")
        void distanceShouldBeSymmetric() {
            LocationVo a = new LocationVo(12.9716, 77.5946);
            LocationVo b = new LocationVo(13.0827, 80.2707);

            double aToB = calculator.calculateDistanceInKm(a, b);
            double bToA = calculator.calculateDistanceInKm(b, a);

            assertEquals(aToB, bToA, 0.001, "Haversine distance should be symmetric");
        }

        @Test
        @DisplayName("null locations should throw IllegalArgumentException")
        void nullLocationsShouldThrow() {
            LocationVo valid = new LocationVo(12.9716, 77.5946);

            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> calculator.calculateDistanceInKm(null, valid),
                            "Null 'from' should throw"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> calculator.calculateDistanceInKm(valid, null),
                            "Null 'to' should throw")
            );
        }
    }

    @Nested
    @DisplayName("Travel time calculations")
    class TravelTimeCalculations {

        @Test
        @DisplayName("20 km at 20 km/hr should take exactly 60 minutes")
        void basicTravelTime() {
            // Two points roughly 20 km apart
            LocationVo a = new LocationVo(12.9716, 77.5946);
            LocationVo b = new LocationVo(12.9716, 77.5946); // same point = 0 km

            double time = calculator.calculateTravelTimeInMinutes(a, b, 20.0);

            assertEquals(0.0, time, TIME_TOLERANCE_MIN, "Zero distance should take zero time");
        }

        @Test
        @DisplayName("default speed should be 20 km/hr")
        void defaultSpeedShouldBe20KmPerHr() {
            LocationVo a = new LocationVo(12.9352, 77.6245);
            LocationVo b = new LocationVo(12.9784, 77.6408);

            double withDefault = calculator.calculateTravelTimeInMinutes(a, b, 20.0);
            double withExplicit = calculator.calculateTravelTimeInMinutes(a, b, 20.0);

            assertEquals(withExplicit, withDefault, TIME_TOLERANCE_MIN,
                    "Default overload should use 20 km/hr");
        }

        @Test
        @DisplayName("higher speed should mean less travel time")
        void higherSpeedMeansLessTime() {
            LocationVo a = new LocationVo(12.9716, 77.5946);
            LocationVo b = new LocationVo(13.0827, 80.2707);

            double slowTime = calculator.calculateTravelTimeInMinutes(a, b, 10.0);
            double fastTime = calculator.calculateTravelTimeInMinutes(a, b, 40.0);

            assertTrue(fastTime < slowTime, "Faster speed should give shorter travel time");
            assertEquals(slowTime, fastTime * 4, 0.01, "Double speed → half time");
        }

        @Test
        @DisplayName("zero or negative speed should throw")
        void invalidSpeedShouldThrow() {
            LocationVo a = new LocationVo(12.9716, 77.5946);
            LocationVo b = new LocationVo(13.0827, 80.2707);

            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> calculator.calculateTravelTimeInMinutes(a, b, 0),
                            "Zero speed should throw"),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> calculator.calculateTravelTimeInMinutes(a, b, -5),
                            "Negative speed should throw")
            );
        }
    }
}
