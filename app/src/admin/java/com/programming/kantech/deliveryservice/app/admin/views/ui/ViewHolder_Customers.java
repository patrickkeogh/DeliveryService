package com.programming.kantech.deliveryservice.app.admin.views.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;

/**
 * Created by patrick keogh on 2017-08-15.
 */

public class ViewHolder_Customers extends RecyclerView.ViewHolder {

    private final TextView tv_customer_name;
    private final TextView tv_customer_address;

    private ViewHolder_Customers.ClickListener mClickListener;

    //Interface to send callbacks...
    public interface ClickListener {
        public void onItemClick(View view, int position);
    }

    public void setOnClickListener(ViewHolder_Customers.ClickListener clickListener) {
        mClickListener = clickListener;
    }

    public ViewHolder_Customers(View itemView) {
        super(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        });

        tv_customer_name = itemView.findViewById(R.id.tv_customer_name);
        tv_customer_address = itemView.findViewById(R.id.tv_customer_address);
    }

    public void setName(String name) {
        tv_customer_name.setText(name);
    }

    public void setAddress(String address) {
        tv_customer_address.setText(address);
    }

}
