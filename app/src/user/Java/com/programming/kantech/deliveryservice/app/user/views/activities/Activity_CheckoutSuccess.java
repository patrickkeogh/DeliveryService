package com.programming.kantech.deliveryservice.app.user.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.programming.kantech.deliveryservice.app.R;
import com.programming.kantech.deliveryservice.app.data.model.pojo.Order;
import com.programming.kantech.deliveryservice.app.utils.Constants;
import com.programming.kantech.deliveryservice.app.utils.Utils_General;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by patri on 2017-10-03.
 */

public class Activity_CheckoutSuccess extends AppCompatActivity {

    private Order mOrder;
    private ActionBar mActionBar;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.tv_user_checkout_success_message)
    TextView tv_user_checkout_success_message;

    @InjectView(R.id.iv_user_checkout_home)
    ImageView iv_user_checkout_home;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_checkout_success);

        Log.i(Constants.LOG_TAG, "onCreate() in User activity_checkout_success");

        ButterKnife.inject(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Constants.STATE_INFO_ORDER)) {
                mOrder = savedInstanceState.getParcelable(Constants.STATE_INFO_ORDER);
            }
        } else {
            mOrder = getIntent().getParcelableExtra(Constants.EXTRA_ORDER);
        }

        if (mOrder == null) {
            throw new IllegalArgumentException("Must pass EXTRA_ORDER");
        } else {

            Log.i(Constants.LOG_TAG, "Amount before rounding:" + mOrder.getAmount());

            double amount = (double) (mOrder.getAmount());

            amount = amount / 100;

            Log.i(Constants.LOG_TAG, "Amount after converting to double:" + amount);

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
        mActionBar = this.getSupportActionBar();

        // Set the toolbar title
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setTitle(R.string.activity_title_checkout_success);
        }
    }

    @OnClick(R.id.iv_user_checkout_home)
    public void returnHome() {

        Intent intent = new Intent(Activity_CheckoutSuccess.this, Activity_Main.class);
        startActivity(intent);
    }

}