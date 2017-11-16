package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.AppUser;
import com.programming.kantech.deliveryservice.app.data.model.pojo.app.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;

import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by patrick keogh on 2017-10-03.
 * An activity to confirm to the user the order was successfully charged
 */

public class Activity_CheckoutSuccess extends AppCompatActivity {

    private Order mOrder;
    private AppUser mAppUser;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tv_user_checkout_success_message)
    TextView tv_user_checkout_success_message;

    @BindView(R.id.iv_user_checkout_home)
    ImageView iv_user_checkout_home;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_checkout_success);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_ORDER)) {
                mOrder = savedInstanceState.getParcelable(Constants.STATE_INFO_ORDER);
            }
            if (savedInstanceState.containsKey(Constants.STATE_INFO_USER)) {
                mAppUser = savedInstanceState.getParcelable(Constants.STATE_INFO_USER);
            }
        } else {
            mOrder = getIntent().getParcelableExtra(Constants.EXTRA_ORDER);
            mAppUser = getIntent().getParcelableExtra(Constants.EXTRA_USER);
        }

        if (mOrder == null || mAppUser == null) {
            throw new IllegalArgumentException("Must pass EXTRA_ORDER & EXTRA_USER");
        } else {

            //Log.i(Constants.LOG_TAG, "Amount before rounding:" + mOrder.getAmount());

            double amount = (double) (mOrder.getAmount());

            amount = amount / 100;

            //Log.i(Constants.LOG_TAG, "Amount after converting to double:" + amount);

            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
            currency.setMaximumFractionDigits(2);
            currency.setMinimumFractionDigits(2);

            String display_amount = currency.format(amount);


            String msg = display_amount + " was successfully charged to your credit card.";

            tv_user_checkout_success_message.setText(msg);

        }

        // Set the support action bar
        setSupportActionBar(mToolbar);

        // Set the action bar back button to look like an up button
        ActionBar mActionBar = this.getSupportActionBar();

        // Set the toolbar title
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle(R.string.activity_title_checkout_success);
        }
    }

    /**
     * Save the current state of this activity
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the driver in the instance state
        outState.putParcelable(Constants.STATE_INFO_USER, mAppUser);
        outState.putParcelable(Constants.STATE_INFO_ORDER, mOrder);
    }

    @OnClick(R.id.iv_user_checkout_home)
    public void returnHome() {

        Intent intent = new Intent(Activity_CheckoutSuccess.this, Activity_Main.class);
        intent.putExtra(Constants.EXTRA_USER, mAppUser);
        startActivity(intent);
        finish();
    }

}
