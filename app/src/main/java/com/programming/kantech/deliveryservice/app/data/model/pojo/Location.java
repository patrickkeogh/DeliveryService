package com.programming.kantech.deliveryservice.app.data.model.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by patrick keogh on 2017-08-23.
 *
 */

public class Location implements Parcelable {

    private String id;
    private String custId;
    private String placeId;
    private Boolean isMainAddress = false;

    public Location() {

    }

    public Location(String id, String custId, String placeId, Boolean isMainAddress) {
        this.id = id;
        this.custId = custId;
        this.placeId = placeId;
        this.isMainAddress = isMainAddress;
    }

    protected Location(Parcel in) {
        id = in.readString();
        custId = in.readString();
        placeId = in.readString();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Boolean getMainAddress() {
        return isMainAddress;
    }

    public void setMainAddress(Boolean mainAddress) {
        isMainAddress = mainAddress;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id='" + id + '\'' +
                ", custId='" + custId + '\'' +
                ", placeId='" + placeId + '\'' +
                ", isMainAddress=" + isMainAddress +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(custId);
        parcel.writeString(placeId);
    }
}
