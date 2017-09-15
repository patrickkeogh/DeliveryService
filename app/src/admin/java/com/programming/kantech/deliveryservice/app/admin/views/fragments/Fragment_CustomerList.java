package com.programming.kantech.deliveryservice.app.admin.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.admin.views.ui.ViewHolder_Customers;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Customer;
import com.programming.kantech.deliveryservice.app.utils.Constants;

/**
 * Created by patrick keogh on 2017-08-29.
 *
 */

public class Fragment_CustomerList extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Member variables
    private FirebaseRecyclerAdapter<Customer, ViewHolder_Customers> mFireAdapter;
    private RecyclerView mCustomerRecyclerView;
    private GoogleApiClient mClient;

    // Firebase references
    private DatabaseReference mCustomersRef;

    // Define a new interface onCustomerSelected that triggers a callback in the host activity
    CustomerClickListener mCallback;

    // onCustomerSelected interface, calls a method in the host activity named onCustomerSelected
    // onAddCustomerClicked interface, calls a method in the host activity named onAddCustomerClicked
    public interface CustomerClickListener {
        void onCustomerSelected(Customer customer);
        void onAddCustomerClicked();
    }

    // Mandatory empty constructor
    public Fragment_CustomerList() {

    }

    /**
     * Static factory method that
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static Fragment_CustomerList newInstance() {
        return new Fragment_CustomerList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Get the fragment layout for the driving list
        final View rootView = inflater.inflate(R.layout.fragment_customer_list, container, false);

        // Get a reference to the orders table
        mCustomersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_NODE_CUSTOMERS);

        // Get a reference to the Fab Button in the fragment_customer_list xml layout file
        FloatingActionButton mFab_add_customer = rootView.findViewById(R.id.fab_add_customer);
        mFab_add_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onAddCustomerClicked();
            }
        });

        // Get a reference to the RecyclerView in the fragment_customer_list xml layout file
        mCustomerRecyclerView = rootView.findViewById(R.id.rv_customer_list);

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mCustomerRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mCustomerRecyclerView.setHasFixedSize(false);

        mFireAdapter = new FirebaseRecyclerAdapter<Customer, ViewHolder_Customers>(
                Customer.class,
                R.layout.item_customer,
                ViewHolder_Customers.class,
                mCustomersRef) {

            @Override
            public void populateViewHolder(final ViewHolder_Customers holder, final Customer customer, int position) {
                Log.i(Constants.LOG_TAG, "populateViewHolder() called:" + customer.toString());

                //holder.itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight));

                holder.setName(customer.getCompany());


                PendingResult<PlaceBuffer> placeResult;

                placeResult = Places.GeoDataApi.getPlaceById(mClient, customer.getPlaceId());

                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {

                        Place myPlace = places.get(0);

                        Log.i(Constants.LOG_TAG, "PLACE:" + myPlace.getAddress());

                        holder.setAddress(myPlace.getAddress().toString());
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Notify the activity a customer has been selected
                        mCallback.onCustomerSelected(customer);
                    }
                });
            }

        };

        mCustomerRecyclerView.setAdapter(mFireAdapter);
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (Fragment_CustomerList.CustomerClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement CustomerClickListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFireAdapter != null) {
            mFireAdapter.cleanup();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();
        if(mClient == null){
            buildApiClient();
            mClient.connect();
        }else{
            if(!mClient.isConnected() ){
                mClient.connect();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mClient != null){
            mClient.disconnect();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClient = null;
    }

    private void buildApiClient() {
        Log.i(Constants.LOG_TAG, "buildApiClient() called");

        if(mClient == null){
            Log.i(Constants.LOG_TAG, "CREATE NEW GOOGLE CLIENT");

            // Build up the LocationServices API client
            // Uses the addApi method to request the LocationServices API
            // Also uses enableAutoManage to automatically know when to connect/suspend the client
            mClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }
}
