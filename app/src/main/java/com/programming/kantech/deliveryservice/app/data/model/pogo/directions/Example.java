package com.programming.kantech.deliveryservice.app.data.model.pogo.directions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by patri on 2017-09-25.
 */

public class Example {

    @SerializedName("routes")
    @Expose
    private List<Route> routes = null;

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }



}
