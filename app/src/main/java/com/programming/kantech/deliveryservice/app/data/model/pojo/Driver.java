package com.programming.kantech.deliveryservice.app.data.model.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by patrick keogh on 2017-08-09.
 *
 */

public class Driver implements Parcelable {

    private String uid;
    private String displayName;
    private String email;
    private String device;
    private Boolean driverApproved;

    public Driver() {

    }

    public Driver(String uid, String displayName, String email, Boolean driverApproved, String device) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.driverApproved = driverApproved;
        this.device = device;
    }

    protected Driver(Parcel in) {
        uid = in.readString();
        displayName = in.readString();
        email = in.readString();
    }

    public static final Creator<Driver> CREATOR = new Creator<Driver>() {
        @Override
        public Driver createFromParcel(Parcel in) {
            return new Driver(in);
        }

        @Override
        public Driver[] newArray(int size) {
            return new Driver[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getDriverApproved() {
        return driverApproved;
    }

    public void setDriverApproved(Boolean driverApproved) {
        this.driverApproved = driverApproved;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "uid='" + uid + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", driverApproved=" + driverApproved +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(displayName);
        parcel.writeString(email);
    }
}
