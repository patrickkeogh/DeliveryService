package com.programming.kantech.deliveryservice.app.driver.views.ui;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.driver.views.activities.Activity_Main;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-27.
 */

public class ViewHolder_Order extends RecyclerView.ViewHolder {

    private final TextView tv_driver_order_name;
    private final TextView tv_driver_order_pickup_address;
    private final TextView tv_driver_order_delivery_address;
    private final TextView tv_driver_order_status;
    private final TextView tv_driver_order_distance;

    private final LinearLayout layout_user_order;
    private final LinearLayout layout_driver_order_inside;

    private ClickListener mClickListener;

    //Interface to send callbacks...
    public interface ClickListener {
        public void onItemClick(View view, int position);
    }

    public void setOnClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

    public ViewHolder_Order(View itemView) {
        super(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        });

        tv_driver_order_name = itemView.findViewById(R.id.tv_driver_order_name);
        tv_driver_order_pickup_address = itemView.findViewById(R.id.tv_driver_order_pickup_address);
        tv_driver_order_delivery_address = itemView.findViewById(R.id.tv_driver_order_delivery_address);
        tv_driver_order_status = itemView.findViewById(R.id.tv_driver_order_status);
        tv_driver_order_distance = itemView.findViewById(R.id.tv_driver_order_distance);

        layout_user_order = itemView.findViewById(R.id.layout_driver_order);
        layout_driver_order_inside = itemView.findViewById(R.id.layout_driver_order_inside);
    }

    public void setCustomerName(String name) {
        tv_driver_order_name.setText(name);
    }

    public void setDistance(String distance) {
        tv_driver_order_distance.setText(distance);
    }

    public void setPickupAddress(String address) {
        tv_driver_order_pickup_address.setText(address);

    }

    public void setDeliveryAddress(String address) {
        tv_driver_order_delivery_address.setText(address);
    }

    public void setOrderStatus(String status) {
        tv_driver_order_status.setText(status);
    }

    public void setBackgroundColor(Activity_Main activity, int colorId){
        layout_user_order.setBackgroundColor(ContextCompat.getColor(activity, colorId));
    }
    public void setSelectedColor(Activity_Main activity, int colorId){
        layout_driver_order_inside.setBackgroundColor(ContextCompat.getColor(activity, colorId));
    }


}
