package com.programming.kantech.deliveryservice.app.data.retrofit;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by patrick keogh on 2017-09-25.
 *
 */

public class ApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(@NonNull Chain chain) throws IOException {

                            Request request = chain.request();
                            HttpUrl url = request.url()
                                    .newBuilder()
                                    .addQueryParameter("key", Constants.KEY_GOOGLE)
                                    .build();

                            request = request
                                    .newBuilder()
                                    .url(url)
                                    .build();

                            return chain.proceed(request);
                        }
                    }).build();


            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_PATH_DIRECTIONS_API)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}

