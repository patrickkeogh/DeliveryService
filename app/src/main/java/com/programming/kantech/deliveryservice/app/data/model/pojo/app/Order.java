package com.programming.kantech.deliveryservice.app.data.model.pojo.app;

import android.os.Parcel;
import android.os.Parcelable;

import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-25.
 *
 */

public class Order implements Parcelable{

    private String id;
    private String deliveryLocationId;
    private String pickupLocationId;
    private String customerId;

    private String customerName;
    private String customerContact;
    private String customerPhone;

    private String driverId;
    private String driverName;
    private String type;
    private String status = Constants.ORDER_STATUS_BOOKED;
    private boolean inProgress;
    private String inProgressDriverId;
    private String inProgressDateDriverId;
    private String sortDateDriverId;
    private String queryDateDriverId;
    private long pickupDate;
    private String distance_text;
    private int distance;
    private int amount;

    public Order() {

    }

    public Order(String id, String deliveryLocationId, String pickupLocationId,
                 String customerId, String customerName, String driverId,
                 String type, String status, boolean inProgress,
                 String inProgressDriverId, String inProgressDateDriverId,
                 long pickupDate, String distance_text, int distance, int amount,
                 String driver, String sortDateDriver, String dateDriverId) {
        this.id = id;
        this.deliveryLocationId = deliveryLocationId;
        this.pickupLocationId = pickupLocationId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.driverId = driverId;
        this.type = type;
        this.status = status;
        this.inProgress = inProgress;
        this.inProgressDriverId = inProgressDriverId;
        this.inProgressDateDriverId = inProgressDateDriverId;
        this.pickupDate = pickupDate;
        this.distance_text = distance_text;
        this.distance = distance;
        this.amount = amount;
        this.driverName = driver;
        this.sortDateDriverId = sortDateDriver;
        this.queryDateDriverId = dateDriverId;
    }

    protected Order(Parcel in) {
        id = in.readString();
        deliveryLocationId = in.readString();
        pickupLocationId = in.readString();
        customerId = in.readString();
        customerName = in.readString();
        customerContact = in.readString();
        customerPhone = in.readString();
        driverId = in.readString();
        driverName = in.readString();
        type = in.readString();
        status = in.readString();
        inProgress = in.readByte() != 0;
        inProgressDriverId = in.readString();
        inProgressDateDriverId = in.readString();
        pickupDate = in.readLong();
        distance_text = in.readString();
        distance = in.readInt();
        amount = in.readInt();
        sortDateDriverId = in.readString();
        queryDateDriverId = in.readString();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(deliveryLocationId);
        dest.writeString(pickupLocationId);
        dest.writeString(customerId);
        dest.writeString(customerName);
        dest.writeString(customerContact);
        dest.writeString(customerPhone);
        dest.writeString(driverId);
        dest.writeString(driverName);
        dest.writeString(type);
        dest.writeString(status);
        dest.writeByte((byte) (inProgress ? 1 : 0));
        dest.writeString(inProgressDriverId);
        dest.writeString(inProgressDateDriverId);
        dest.writeLong(pickupDate);
        dest.writeString(distance_text);
        dest.writeInt(distance);
        dest.writeInt(amount);
        dest.writeString(sortDateDriverId);
        dest.writeString(queryDateDriverId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

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

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
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

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public String getInProgressDriverId() {
        return inProgressDriverId;
    }

    public void setInProgressDriverId(String inProgressDriverId) {
        this.inProgressDriverId = inProgressDriverId;
    }

    public String getInProgressDateDriverId() {
        return inProgressDateDriverId;
    }

    public void setInProgressDateDriverId(String inProgressDateDriverId) {
        this.inProgressDateDriverId = inProgressDateDriverId;
    }

    public long getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(long pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getDistance_text() {
        return distance_text;
    }

    public void setDistance_text(String distance_text) {
        this.distance_text = distance_text;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getSortDateDriverId() {
        return sortDateDriverId;
    }

    public void setSortDateDriverId(String sortDateDriverId) {
        this.sortDateDriverId = sortDateDriverId;
    }

    public String getQueryDateDriverId() {
        return queryDateDriverId;
    }

    public void setQueryDateDriverId(String queryDateDriverId) {
        this.queryDateDriverId = queryDateDriverId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", deliveryLocationId='" + deliveryLocationId + '\'' +
                ", pickupLocationId='" + pickupLocationId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerContact='" + customerContact + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", driverId='" + driverId + '\'' +
                ", driverName='" + driverName + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", inProgress=" + inProgress +
                ", inProgressDriverId='" + inProgressDriverId + '\'' +
                ", inProgressDateDriverId='" + inProgressDateDriverId + '\'' +
                ", sortDateDriverId='" + sortDateDriverId + '\'' +
                ", queryDateDriverId='" + queryDateDriverId + '\'' +
                ", pickupDate=" + pickupDate +
                ", distance_text='" + distance_text + '\'' +
                ", distance=" + distance +
                ", amount=" + amount +
                '}';
    }
}
