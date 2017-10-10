package com.programming.kantech.deliveryservice.app.driver.views.widget;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import java.util.ArrayList;

/**
 * Created by patri on 2017-10-10.
 */

public class AppWidget_ViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<Order> mOrderList = new ArrayList<>();
    private Context mContext = null;

    private DatabaseReference mOrdersRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    public AppWidget_ViewFactory(Context mContext) {
        this.mContext = mContext;
    }

    private void populateOrdersListView(){

        Log.i(Constants.LOG_TAG, "populateOrdersListView called");
        mOrdersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_ORDERS);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(Constants.LOG_TAG, "onDataChange called in populateOrdersListView()");

                if(dataSnapshot !=  null){
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        Order order = snapshot.getValue(Order.class);
                        Log.i(Constants.LOG_TAG, "Add order to list:" + order.toString());
                        mOrderList.add(order);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        populateOrdersListView();

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        Log.i(Constants.LOG_TAG, "Count:" + mOrderList.size());
        if (mOrderList == null) return 0;
        return mOrderList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.i(Constants.LOG_TAG, "getViewAt() called in WidgetRemoteViewsFactory");

        if (mOrderList == null || mOrderList.size() == 0) return null;

        // get the order for the requested position
        Order order = mOrderList.get(position);

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_order_list_item);
        views.setTextViewText(R.id.widget_tv_company, order.getCustomerName());



        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
