package com.lucidity.deliveryrouteoptimizer.vo;

import java.util.List;

/**
 * The API response - the optimized delivery plan.
 *
 * <p>Tells the delivery executive exactly what sequence to follow,
 * how long the whole thing will take, and how far they'll travel.
 * Basically the answer to "what's the fastest way to get all this done?"</p>
 *
 * @author Rithik Jain
 */
public class DeliveryResponseVo {

    private List<RouteStepVo> route;
    private double totalTimeInMinutes;
    private double totalDistanceInKm;
    private int totalOrders;
    private String strategy;

    public DeliveryResponseVo() {}

    public DeliveryResponseVo(List<RouteStepVo> route, double totalTimeInMinutes,
                              double totalDistanceInKm, int totalOrders, String strategy) {
        this.route = route;
        this.totalTimeInMinutes = totalTimeInMinutes;
        this.totalDistanceInKm = totalDistanceInKm;
        this.totalOrders = totalOrders;
        this.strategy = strategy;
    }

    public List<RouteStepVo> getRoute() {
        return route;
    }
    public void setRoute(List<RouteStepVo> route) {
        this.route = route;
    }

    public double getTotalTimeInMinutes() {
        return totalTimeInMinutes;
    }
    public void setTotalTimeInMinutes(double totalTimeInMinutes) {
        this.totalTimeInMinutes = totalTimeInMinutes;
    }

    public double getTotalDistanceInKm() {
        return totalDistanceInKm;
    }
    public void setTotalDistanceInKm(double totalDistanceInKm) {
        this.totalDistanceInKm = totalDistanceInKm;
    }

    public int getTotalOrders() {
        return totalOrders;
    }
    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public String getStrategy() {
        return strategy;
    }
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
