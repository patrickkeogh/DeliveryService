package com.programming.kantech.deliveryservice.app.admin.views.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;

/**
 * Created by patrick keogh on 2017-08-27.
 *
 */

public class ViewHolder_Order extends RecyclerView.ViewHolder {

    private final TextView tv_admin_order_customer;
    private final TextView tv_admin_order_date;
    private final TextView tv_admin_order_pickup_address;
    private final TextView tv_admin_order_delivery_address;
    private final TextView tv_admin_order_status;

    private final LinearLayout layout_admin_order;
    private final LinearLayout layout_selected;

    private ClickListener mClickListener;

    //Interface to send callbacks...
    public interface ClickListener {
        void onItemClick(View view, int position);
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

        tv_admin_order_customer = itemView.findViewById(R.id.tv_admin_order_customer);
        tv_admin_order_date = itemView.findViewById(R.id.tv_admin_order_date);
        tv_admin_order_pickup_address = itemView.findViewById(R.id.tv_admin_order_pickup_address);
        tv_admin_order_delivery_address = itemView.findViewById(R.id.tv_admin_order_delivery_address);
        tv_admin_order_status = itemView.findViewById(R.id.tv_admin_order_status);
        //tv_admin_order_distance = itemView.findViewById(R.id.tv_admin_order_distance);

        layout_selected = itemView.findViewById(R.id.layout_selected);

        layout_admin_order = itemView.findViewById(R.id.layout_admin_order);
    }

    public void setCustomerName(String name) {
        tv_admin_order_customer.setText(name);
    }

    public void setOrderDate(String date) {
        tv_admin_order_date.setText(date);
    }

//    public void setDistance(String distance) {
//        tv_admin_order_distance.setText(distance);
//    }

    public void setPickupAddress(String address) {
        tv_admin_order_pickup_address.setText(address);
    }

    public void setDeliveryAddress(String address) {
        tv_admin_order_delivery_address.setText(address);
    }

    public void setOrderStatus(String status) {
        tv_admin_order_status.setText(status);
    }

    public void setBackgroundColor(Context context, int colorId){

        layout_admin_order.setBackgroundColor(ContextCompat.getColor(context, colorId));
    }

    public void setSelectedColor(Context context, int colorId){

        layout_selected.setBackgroundColor(ContextCompat.getColor(context, colorId));
    }


}
