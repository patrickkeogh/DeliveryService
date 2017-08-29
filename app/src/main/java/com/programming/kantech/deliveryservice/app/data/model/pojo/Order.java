package com.programming.kantech.deliveryservice.app.data.model.pojo;

import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-25.
 *
 */

public class Order {

    private String id;
    private String deliveryLocationId;
    private String pickupLocationId;
    private String customerId;
    private String customerName;
    private String type;
    private String status = Constants.ORDER_STATUS_BOOKED;
    private long pickupDate;

    public Order() {

    }

    public Order(String id, String deliveryLocationId, String pickupLocationId, String customerId, String customerName, String type, String status, long pickupDate) {
        this.id = id;
        this.deliveryLocationId = deliveryLocationId;
        this.pickupLocationId = pickupLocationId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.type = type;
        this.status = status;
        this.pickupDate = pickupDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeliveryLocationId() {
        return deliveryLocationId;
    }

    public void setDeliveryLocationId(String deliveryLocationId) {
        this.deliveryLocationId = deliveryLocationId;
    }

    public String getPickupLocationId() {
        return pickupLocationId;
    }

    public void setPickupLocationId(String pickupLocationId) {
        this.pickupLocationId = pickupLocationId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(long pickupDate) {
        this.pickupDate = pickupDate;
    }
}
