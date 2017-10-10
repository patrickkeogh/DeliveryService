package com.programming.kantech.deliveryservice.app.data.model.pogo.directions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.programming.kantech.deliveryservice.app.data.model.pogo.directions.Leg;

import java.util.List;

/**
 * Created by patri on 2017-09-25.
 */

public class Route {

    @SerializedName("legs")
    @Expose
    private List<Leg> legs = null;

    public List<Leg> getLegs() {
        return legs;
    }

    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }


}
