package com.programming.kantech.deliveryservice.app.admin.views.ui;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-15.
 *
 */

public class ViewHolder_Locations extends RecyclerView.ViewHolder {

    private final TextView tv_location_name;
    private final TextView tv_location_address;

    private ViewHolder_Locations.ClickListener mClickListener;

    //Interface to send callbacks...
    public interface ClickListener {
        public void onItemClick(View view, int position);
    }

    public void setOnClickListener(ViewHolder_Locations.ClickListener clickListener) {
        mClickListener = clickListener;
    }

    public ViewHolder_Locations(View itemView) {
        super(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        });

        tv_location_name = itemView.findViewById(R.id.tv_location_name);


        tv_location_address = itemView.findViewById(R.id.tv_location_address);
    }

    public void setName(String name) {
        Log.i(Constants.LOG_TAG, "setname:" + name);
        tv_location_name.setText(name);
    }

    public void setAddress(String address) {
        tv_location_address.setText(address);
    }

}
