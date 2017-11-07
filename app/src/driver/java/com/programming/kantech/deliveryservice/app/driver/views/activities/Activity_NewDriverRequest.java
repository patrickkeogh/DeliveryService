package com.programming.kantech.deliveryservice.app.driver.views.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Activity_NewDriverRequest extends AppCompatActivity {

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mProgressBar;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.appbar_layout)
    AppBarLayout mAppbarLayout;

    @BindView(R.id.tv_request_title)
    TextView mTitle;

    @BindView(R.id.tv_request_message)
    TextView mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_driver_request);

        ButterKnife.bind(this);

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("New Driver Request");
        }

        mProgressBar.setVisibility(View.VISIBLE);

        sendNewDriverRequest();


    }

    @Override
    public void onBackPressed() {
        Log.i(Constants.LOG_TAG, "onBackPressed() in Driver has been called");
        finish();
    }

    private void sendNewDriverRequest() {

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(Constants.LOG_TAG, "stop progress bar");
                //Do something after 10 secs
                mProgressBar.setVisibility(View.GONE);
                mTitle.setText(R.string.request_new_driver_title_completed);
                mMessage.setText(R.string.request_new_driver_message_completed);
            }
        }, 10000);

    }
}
