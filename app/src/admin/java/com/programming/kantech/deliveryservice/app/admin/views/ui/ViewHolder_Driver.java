package com.programming.kantech.deliveryservice.app.admin.views.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.programming.kantech.deliveryservice.app.R;

/**
 * Created by patrick keogh on 2017-08-15.
 *
 */

public class ViewHolder_Driver extends RecyclerView.ViewHolder {

    private final TextView tv_driver_name;
    private final TextView tv_driver_id;
    public final ImageView iv_driver_photo;

    private ViewHolder_Driver.ClickListener mClickListener;

    //Interface to send callbacks...
    public interface ClickListener {
        public void onItemClick(View view, int position);
    }

    public void setOnClickListener(ViewHolder_Driver.ClickListener clickListener) {
        mClickListener = clickListener;
    }

    public ViewHolder_Driver(View itemView) {
        super(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        });

        tv_driver_name = itemView.findViewById(R.id.tv_driver_name);
        tv_driver_id = itemView.findViewById(R.id.tv_driver_id);
        iv_driver_photo = itemView.findViewById(R.id.iv_admin_driver_photo);
    }

    public void setName(String name) {
        tv_driver_name.setText(name);
    }

    public void setPhoto(String url, Context context) {
        Glide.with(context).load(url)
                .error(R.drawable.ic_menu_drive)
                .placeholder(R.drawable.ic_attach_money)
                .dontAnimate()
                .into(iv_driver_photo);
    }


    public void setId(String id) {
        tv_driver_id.setText(id);
    }

}
