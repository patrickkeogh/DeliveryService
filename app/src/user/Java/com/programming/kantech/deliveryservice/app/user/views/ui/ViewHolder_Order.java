package com.programming.kantech.deliveryservice.app.user.views.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.user.views.activities.Activity_MyOrders;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-27.
 * A view holder for Firebase Adaapter
 */

public class ViewHolder_Order extends RecyclerView.ViewHolder {

    private final TextView tv_user_order_delivery_address;
    private final TextView tv_user_order_pickup_address;
    private final TextView tv_user_order_date;
    private final TextView tv_user_order_status;
    private final LinearLayout layout_user_order;

    public Button btn_order_show_map;
    public Button btn_order_show_details;

    private ClickListener mClickListener;

    //Interface to send callbacks...
    public interface ClickListener {
        public void onShowMapClick(View view, int position);
    }

    public void setOnMapClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

    public ViewHolder_Order(View itemView) {
        super(itemView);

        tv_user_order_date = itemView.findViewById(R.id.tv_user_order_date);
        tv_user_order_pickup_address = itemView.findViewById(R.id.tv_user_order_pickup_address);
        tv_user_order_delivery_address = itemView.findViewById(R.id.tv_user_order_delivery_address);
        tv_user_order_status = itemView.findViewById(R.id.tv_user_order_status);
        layout_user_order = itemView.findViewById(R.id.layout_user_order);

        btn_order_show_map = itemView.findViewById(R.id.btn_order_show_map);
        btn_order_show_details = itemView.findViewById(R.id.btn_order_show_details);


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

    public void setBackgroundColor(Activity_MyOrders activity, int colorId){
        layout_user_order.setBackgroundColor(ContextCompat.getColor(activity, colorId));
    }

}
