package com.lucidity.deliveryrouteoptimizer.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * The incoming request payload for the delivery route optimization API.
 *
 * <p>Contains the delivery executive's current position and the
 * batch of orders they've just been assigned. This is the
 * "phone notification" Aman gets - here's where you are, here's
 * what you need to deliver.</p>
 *
 * @author Rithik Jain
 */
public class DeliveryRequestVo {

    @NotNull(message = "Delivery executive's current location is required")
    @Valid
    private LocationVo deliveryExecutiveLocation;

    @NotEmpty(message = "At least one order is required")
    @Valid
    private List<OrderVo> orderVos;

    public DeliveryRequestVo() {}

    public DeliveryRequestVo(LocationVo deliveryExecutiveLocation, List<OrderVo> orderVos) {
        this.deliveryExecutiveLocation = deliveryExecutiveLocation;
        this.orderVos = orderVos;
    }

    public LocationVo getDeliveryExecutiveLocation() {
        return deliveryExecutiveLocation;
    }
    public void setDeliveryExecutiveLocation(LocationVo deliveryExecutiveLocation) {
        this.deliveryExecutiveLocation = deliveryExecutiveLocation;
    }

    public List<OrderVo> getOrders() {
        return orderVos;
    }
    public void setOrders(List<OrderVo> orderVos) {
        this.orderVos = orderVos;
    }
}
