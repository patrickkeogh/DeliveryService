package com.programming.kantech.deliveryservice.app.data.model.pojo.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by patrick keogh on 2017-08-16.
 *
 */

public class Customer implements Parcelable{

    private String id;
    private String email;
    private String company;
    private String contact_name;
    private String contact_number;
    private String placeId;

    public Customer() {

    }

    public Customer(String id, String email, String company, String contact_name, String contact_number, String placeId) {
        this.id = id;
        this.email = email;
        this.company = company;
        this.contact_name = contact_name;
        this.contact_number = contact_number;
        this.placeId = placeId;
    }


    protected Customer(Parcel in) {
        id = in.readString();
        email = in.readString();
        company = in.readString();
        contact_name = in.readString();
        contact_number = in.readString();
        placeId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(company);
        dest.writeString(contact_name);
        dest.writeString(contact_number);
        dest.writeString(placeId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Customer> CREATOR = new Creator<Customer>() {
        @Override
        public Customer createFromParcel(Parcel in) {
            return new Customer(in);
        }

        @Override
        public Customer[] newArray(int size) {
            return new Customer[size];
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

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceId() {
        return placeId;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                ", contact_name='" + contact_name + '\'' +
                ", contact_number='" + contact_number + '\'' +
                ", placeId='" + placeId + '\'' +
                '}';
    }
}
