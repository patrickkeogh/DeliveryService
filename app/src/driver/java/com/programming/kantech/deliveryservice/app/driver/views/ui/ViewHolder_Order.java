package com.programming.kantech.deliveryservice.app.driver.views.ui;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-27.
 */

public class ViewHolder_Order extends RecyclerView.ViewHolder {

    private final TextView tv_order_customer_name;
    private final TextView tv_order_pickup_address;
    private final TextView tv_order_delivery_address;
    private final TextView tv_order_date;
    private final TextView tv_order_status;

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

        tv_order_customer_name = itemView.findViewById(R.id.tv_customer_name);
        tv_order_pickup_address = itemView.findViewById(R.id.tv_order_pickup_address);
        tv_order_delivery_address = itemView.findViewById(R.id.tv_order_delivery_address);
        tv_order_date = itemView.findViewById(R.id.tv_order_date);
        tv_order_status = itemView.findViewById(R.id.tv_order_status);
    }

    public void setCustomerName(String name) {
        tv_order_customer_name.setText(name);
    }

    public void setPickupAddress(String address) {
        tv_order_pickup_address.setText(address);
    }

    public void setDeliveryAddress(String address) {
        tv_order_delivery_address.setText(address);
    }

    public void setOrderDate(String date) {
        tv_order_date.setText(date);
    }

    public void setOrderStatus(String status) {
        tv_order_status.setText(status);
    }


}
