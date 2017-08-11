package com.benjamjin.familyhub;

import android.app.Application;

/**
 * Created by benjamin on 09/08/2017.
 */

public class MyApplication extends Application {

    private static MyApplication mApplication;
    private static VocaliserService mVocaliser;

    @Override
    public void onCreate() {
        super.onCreate();

        mVocaliser = new VocaliserService(this);
        mVocaliser.setEnableVocalisation(true);

        mApplication = this;
    }

    protected void vocaliseText(String text) {
        mVocaliser.doVocalise(text);
    }

    public static MyApplication getInstance(){
        return mApplication;
    }
}


