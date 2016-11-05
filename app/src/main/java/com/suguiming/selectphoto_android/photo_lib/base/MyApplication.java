package com.suguiming.selectphoto_android.photo_lib.base;

import android.app.Application;


/**
 * Created by suguiming on 16/9/26.
 */
public class MyApplication extends Application {

    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }

    public static synchronized MyApplication getInstance() {
        return myApplication;
    }


}
