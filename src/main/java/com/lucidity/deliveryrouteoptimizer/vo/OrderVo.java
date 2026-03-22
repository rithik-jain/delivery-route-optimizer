package com.lucidity.deliveryrouteoptimizer.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Represents a single delivery order in the batch.
 *
 * <p>Each order ties together a restaurant, a consumer,
 * and the restaurant's average meal preparation time in minutes.</p>
 *
 * <p>The key constraint: you can't deliver food that hasn't been
 * cooked yet. So the delivery exec must visit the restaurant first,
 * potentially wait for the food, and only then head to the consumer.</p>
 *
 * @author Rithik Jain
 */
public class OrderVo {

    @NotNull(message = "Restaurant location is required")
    @Valid
    private LocationVo restaurantLocation;

    @NotNull(message = "Consumer location is required")
    @Valid
    private LocationVo consumerLocation;

    @NotNull(message = "Meal preparation time is required")
    @Positive(message = "Meal preparation time must be positive")
    private Double mealPreparationTimeInMinutes;

    public OrderVo() {}

    public OrderVo(LocationVo restaurantLocation, LocationVo consumerLocation,
                   double mealPreparationTimeInMinutes) {
        this.restaurantLocation = restaurantLocation;
        this.consumerLocation = consumerLocation;
        this.mealPreparationTimeInMinutes = mealPreparationTimeInMinutes;
    }

    public LocationVo getRestaurantLocation() {
        return restaurantLocation;
    }
    public void setRestaurantLocation(LocationVo restaurantLocation) {
        this.restaurantLocation = restaurantLocation;
    }

    public LocationVo getConsumerLocation() {
        return consumerLocation;
    }
    public void setConsumerLocation(LocationVo consumerLocation) {
        this.consumerLocation = consumerLocation;
    }

    public Double getMealPreparationTimeInMinutes() {
        return mealPreparationTimeInMinutes;
    }
    public void setMealPreparationTimeInMinutes(Double mealPreparationTimeInMinutes) {
        this.mealPreparationTimeInMinutes = mealPreparationTimeInMinutes;
    }

    @Override
    public String toString() {
        return "Order{restaurant=" + restaurantLocation
                + ", consumer=" + consumerLocation
                + ", prepTime=" + mealPreparationTimeInMinutes + "mins}";
    }
}
