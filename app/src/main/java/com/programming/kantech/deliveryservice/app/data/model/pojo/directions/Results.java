package com.programming.kantech.deliveryservice.app.data.model.pojo.directions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.programming.kantech.deliveryservice.app.data.model.pojo.directions.Route;

import java.util.List;

/**
 * Created by patri on 2017-09-25.
 */

public class Results {

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
