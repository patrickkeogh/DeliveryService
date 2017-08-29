package com.programming.kantech.deliveryservice.app.data.model.pojo;

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

    public Customer() {

    }

    public Customer(String id, String email, String company, String contact_name, String contact_number) {
        this.id = id;
        this.email = email;
        this.company = company;
        this.contact_name = contact_name;
        this.contact_number = contact_number;
    }

    protected Customer(Parcel in) {
        id = in.readString();
        email = in.readString();
        company = in.readString();
        contact_name = in.readString();
        contact_number = in.readString();
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
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                ", contact_name='" + contact_name + '\'' +
                ", contact_number='" + contact_number + '\'' +
                '}';
    }
}
