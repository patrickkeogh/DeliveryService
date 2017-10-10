package com.programming.kantech.deliveryservice.app.data.retrofit;

import com.programming.kantech.deliveryservice.app.data.model.pogo.directions.Example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by patrick keogh on 2017-09-25.
 * An Interface to query for the distance between 2 places, given 2 place id's
 */

public interface ApiInterface {

    // Parameters used
    String PARAM_ORIGIN = "origin";
    String PARAM_DESTINATION = "destination";


    // Paths for the server
    String ROOT_PATH = "json";

    @GET(ROOT_PATH)
    Call<Example> getDistance(@Query(PARAM_ORIGIN) String origin_place,
                              @Query(PARAM_DESTINATION) String destination_place);


}
