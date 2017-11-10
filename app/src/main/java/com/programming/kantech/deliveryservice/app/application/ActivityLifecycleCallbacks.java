package com.programming.kantech.deliveryservice.app.application;

/**
 * Created by patri on 2017-11-10.
 */

public interface ActivityLifecycleCallbacks {

    void onActivityCreated(android.app.Activity activity, android.os.Bundle bundle);

    void onActivityStarted(android.app.Activity activity);

    void onActivityResumed(android.app.Activity activity);

    void onActivityPaused(android.app.Activity activity);

    void onActivityStopped(android.app.Activity activity);

    void onActivitySaveInstanceState(android.app.Activity activity, android.os.Bundle bundle);

    void onActivityDestroyed(android.app.Activity activity);
}
