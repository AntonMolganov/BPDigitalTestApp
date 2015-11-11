package com.bpdigital.testapp;

import com.vk.sdk.VKSdk;

/**
 * Created by Anton on 07.11.2015.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
