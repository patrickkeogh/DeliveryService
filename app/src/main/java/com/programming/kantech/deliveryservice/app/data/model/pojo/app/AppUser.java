package com.programming.kantech.deliveryservice.app.data.model.pojo.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by patri on 2017-09-21.
 */

public class AppUser implements Parcelable {

    private String id;
    private String email;
    private String company;
    private String contact_name;
    private String contact_number;
    private String placeId;
    private String device;

    public AppUser() {

    }

    public AppUser(String id, String email, String company, String contact_name, String contact_number, String placeId, String device) {
        this.id = id;
        this.email = email;
        this.company = company;
        this.contact_name = contact_name;
        this.contact_number = contact_number;
        this.placeId = placeId;
        this.device = device;


    }

    protected AppUser(Parcel in) {
        id = in.readString();
        email = in.readString();
        company = in.readString();
        contact_name = in.readString();
        contact_number = in.readString();
        placeId = in.readString();
        device = in.readString();
    }

    public static final Creator<AppUser> CREATOR = new Creator<AppUser>() {
        @Override
        public AppUser createFromParcel(Parcel in) {
            return new AppUser(in);
        }

        @Override
        public AppUser[] newArray(int size) {
            return new AppUser[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(email);
        parcel.writeString(company);
        parcel.writeString(contact_name);
        parcel.writeString(contact_number);
        parcel.writeString(placeId);
        parcel.writeString(device);
    }
}
