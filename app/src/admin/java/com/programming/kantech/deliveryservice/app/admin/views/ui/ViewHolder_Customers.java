package com.programming.kantech.deliveryservice.app.admin.views.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;

/**
 * Created by patrick keogh on 2017-08-15.
 *
 */

public class ViewHolder_Customers extends RecyclerView.ViewHolder {

    private final TextView tv_admin_customer_name;
    private final TextView tv_admin_customer_address;
    private final LinearLayout layout_admin_customer_inside;

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

        tv_admin_customer_name = itemView.findViewById(R.id.tv_admin_customer_name);
        tv_admin_customer_address = itemView.findViewById(R.id.tv_admin_customer_address);
        layout_admin_customer_inside = itemView.findViewById(R.id.layout_admin_customer_inside);
    }

    public void setName(String name) {
        tv_admin_customer_name.setText(name);
    }

    public void setAddress(String address) {
        tv_admin_customer_address.setText(address);

    }

    public void setSelectedColor(Context context, int colorId) {
        layout_admin_customer_inside.setBackgroundColor(ContextCompat.getColor(context, colorId));
    }

}
