package com.programming.kantech.deliveryservice.app.driver.views.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;

/**
 * Created by patrick keogh on 2017-08-27.
 * A view holder to show the details of an order
 */

public class ViewHolder_OrderHistory extends RecyclerView.ViewHolder {

    private final TextView tv_driver_order_customer;
    private final TextView tv_driver_order_date;
    private final TextView tv_driver_order_pickup_address;
    private final TextView tv_driver_order_delivery_address;
    private final TextView tv_driver_order_status;
    private final LinearLayout layout_driver_order;
    private final LinearLayout layout_selected;

    public ViewHolder_OrderHistory(View itemView) {
        super(itemView);

        tv_driver_order_customer = itemView.findViewById(R.id.tv_driver_order_customer);
        tv_driver_order_date = itemView.findViewById(R.id.tv_driver_order_date);
        tv_driver_order_pickup_address = itemView.findViewById(R.id.tv_driver_order_pickup_address);
        tv_driver_order_delivery_address = itemView.findViewById(R.id.tv_driver_order_delivery_address);
        tv_driver_order_status = itemView.findViewById(R.id.tv_driver_order_status);
        layout_driver_order = itemView.findViewById(R.id.layout_driver_order);
        layout_selected = itemView.findViewById(R.id.layout_selected);

    }

    public void setCustomerName(String name) {
        tv_driver_order_customer.setText(name);
    }

    public void setPickupAddress(String address) {
        tv_driver_order_pickup_address.setText(address);
    }

    public void setDeliveryAddress(String address) {
        tv_driver_order_delivery_address.setText(address);
    }

    public void setOrderDate(String date) {
        tv_driver_order_date.setText(date);
    }

    public void setOrderStatus(String status) {
        tv_driver_order_status.setText(status);
    }

    public void setBackgroundColor(Context context, int colorId){
        layout_driver_order.setBackgroundColor(ContextCompat.getColor(context, colorId));
    }

    public void setSelectedColor(Context context, int colorId){
        layout_selected.setBackgroundColor(ContextCompat.getColor(context, colorId));
    }


}
