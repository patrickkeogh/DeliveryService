package com.programming.kantech.deliveryservice.app.admin.views.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

/**
 * Created by patri on 2017-10-13.
 */

public class HPLinearLayoutManager extends LinearLayoutManager {

    public HPLinearLayoutManager(Context context) {
        super(context);
    }

    public HPLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public HPLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Magic here
     */
    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}
