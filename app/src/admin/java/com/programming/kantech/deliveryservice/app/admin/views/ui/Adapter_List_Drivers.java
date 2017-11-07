package com.programming.kantech.deliveryservice.app.admin.views.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Driver;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patri on 2017-08-13.
 *
 */

public class Adapter_List_Drivers extends RecyclerView.Adapter<Adapter_List_Drivers.ViewHolder_Details> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private List<Driver> mDrivers = new ArrayList<>();

    final private OnClickHandler mClickHandler;

    public void update(List<Driver> drivers) {
        mDrivers.clear();
        for (Driver driver: drivers) {
            mDrivers.add(driver);
        }
        notifyDataSetChanged();
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface OnClickHandler {
        void onClick(Driver driver);
    }

    /**
     * Creates an Adapter_Details_Steps.
     *
     * @param context      Used to talk to the UI and app resources
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public Adapter_List_Drivers(@NonNull Context context, OnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;

    }

    @Override
    public ViewHolder_Details onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.i(Constants.LOG_TAG, "onCreateViewHolder() called in Adapter_List_Drivers");
        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_driver, viewGroup, false);

        view.setFocusable(true);

        return new ViewHolder_Details(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder_Details holder, int position) {

        Driver driver = mDrivers.get(position);

        Log.i(Constants.LOG_TAG, "Adapter Driver:" + driver.getDisplayName());

        holder.tv_driver_name.setText(driver.getDisplayName());

    }

    @Override
    public int getItemCount() {
        //Log.i(Constants.LOG_TAG, "getItemCount() called");
        if (mDrivers == null) return 0;
        return mDrivers.size();
    }

    class ViewHolder_Details extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView tv_driver_name;

        public ViewHolder_Details(View view) {
            super(view);
            view.setOnClickListener(this);

            tv_driver_name = view.findViewById(R.id.tv_driver_name);
        }

        @Override
        public void onClick(View view) {
            Driver driver = mDrivers.get(getAdapterPosition());
            mClickHandler.onClick(driver);

        }
    }



}
