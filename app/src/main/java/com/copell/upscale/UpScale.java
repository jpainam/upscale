package com.copell.upscale;

import android.content.Context;

import androidx.multidex.MultiDexApplication;


public class UpScale extends MultiDexApplication {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return appContext;
    }
}
