package com.programming.kantech.deliveryservice.app.driver.views.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.driver.views.activities.Activity_Main;

/**
 * Created by patrick keogh on 2017-08-27.
 */

public class ViewHolder_OrderHistory extends RecyclerView.ViewHolder {

    private final TextView tv_user_order_customer;
    private final TextView tv_user_order_delivery_address;
    private final TextView tv_user_order_pickup_address;
    private final TextView tv_user_order_date;
    private final TextView tv_user_order_status;
    private final LinearLayout layout_user_order;

    private ClickListener mClickListener;

    private Context mContext;

    //Interface to send callbacks...
    public interface ClickListener {
        public void onShowMapClick(View view, int position);
    }

    public void setOnMapClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

    public ViewHolder_OrderHistory(View itemView) {
        super(itemView);

//        itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mClickListener.onItemClick(v, getAdapterPosition());
//            }
//        });

        tv_user_order_customer = itemView.findViewById(R.id.tv_user_order_customer);
        tv_user_order_date = itemView.findViewById(R.id.tv_user_order_date);
        tv_user_order_pickup_address = itemView.findViewById(R.id.tv_user_order_pickup_address);
        tv_user_order_delivery_address = itemView.findViewById(R.id.tv_user_order_delivery_address);
        tv_user_order_status = itemView.findViewById(R.id.tv_user_order_status);
        layout_user_order = itemView.findViewById(R.id.layout_user_order);

    }

    public void setCustomerName(String name) {
        tv_user_order_customer.setText(name);
    }

    public void setPickupAddress(String address) {
        tv_user_order_pickup_address.setText(address);
    }

    public void setDeliveryAddress(String address) {
        tv_user_order_delivery_address.setText(address);
    }

    public void setOrderDate(String date) {
        tv_user_order_date.setText(date);
    }

    public void setOrderStatus(String status) {
        tv_user_order_status.setText(status);
    }

    public void setBackgroundColor(Context context, int colorId){
        layout_user_order.setBackgroundColor(ContextCompat.getColor(context, colorId));
    }


}
