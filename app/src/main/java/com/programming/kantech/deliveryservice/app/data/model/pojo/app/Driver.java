package com.programming.kantech.deliveryservice.app.data.model.pojo.app;

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
    private Boolean photoApproved;
    private String photoUrl;
    private String thumbUrl;

    public Driver() {

    }

    public Driver(String uid, String displayName, String email,
                  String device, Boolean driverApproved, Boolean photoApproved, String photoUrl, String thumbUrl) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.device = device;
        this.driverApproved = driverApproved;
        this.photoApproved = photoApproved;
        this.photoUrl = photoUrl;
        this.thumbUrl = thumbUrl;
    }

    protected Driver(Parcel in) {
        uid = in.readString();
        displayName = in.readString();
        email = in.readString();
        device = in.readString();
        photoUrl = in.readString();
        thumbUrl = in.readString();
        driverApproved = (Boolean) in.readValue( null );
        photoApproved = (Boolean) in.readValue( null );
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

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Boolean getDriverApproved() {
        return driverApproved;
    }

    public void setDriverApproved(Boolean driverApproved) {
        this.driverApproved = driverApproved;
    }

    public Boolean getPhotoApproved() {
        return photoApproved;
    }

    public void setPhotoApproved(Boolean photoApproved) {
        this.photoApproved = photoApproved;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
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
        parcel.writeString(device);
        parcel.writeString(photoUrl);
        parcel.writeString(thumbUrl);
        parcel.writeValue(driverApproved);
        parcel.writeValue(photoApproved);
    }

    @Override
    public String toString() {
        return "Driver{" +
                "uid='" + uid + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", device='" + device + '\'' +
                ", driverApproved=" + driverApproved +
                ", photoApproved=" + photoApproved +
                ", photoUrl='" + photoUrl + '\'' +
                ", thumbUrl='" + thumbUrl + '\'' +
                '}';
    }
}
