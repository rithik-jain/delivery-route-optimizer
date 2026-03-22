package com.lucidity.deliveryrouteoptimizer.vo;

/**
 * One step in the optimized delivery route.
 *
 * <p>Each step tells the delivery executive: go to this location,
 * here's what you're doing there (picking up or dropping off),
 * and which order it belongs to.</p>
 *
 * @author Rithik Jain
 */
public class RouteStepVo {

    private int stepNumber;
    private LocationVo location;
    private String action;
    private int orderNumber;

    public RouteStepVo() {}

    public RouteStepVo(int stepNumber, LocationVo location, String action, int orderNumber) {
        this.stepNumber = stepNumber;
        this.location = location;
        this.action = action;
        this.orderNumber = orderNumber;
    }

    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

    public LocationVo getLocation() { return location; }
    public void setLocation(LocationVo location) { this.location = location; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public int getOrderNumber() { return orderNumber; }
    public void setOrderNumber(int orderNumber) { this.orderNumber = orderNumber; }

    @Override
    public String toString() {
        return "Step " + stepNumber + ": " + action + " for Order " + orderNumber + " at " + location;
    }
}
