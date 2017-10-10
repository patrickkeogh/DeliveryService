package com.programming.kantech.deliveryservice.app.driver.services;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.programming.kantech.deliveryservice.app.driver.views.widget.AppWidget_ViewFactory;

/**
 * Created by patri on 2017-10-10.
 */

public class Service_DriverWidget extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new AppWidget_ViewFactory(this.getApplicationContext());
    }


}
