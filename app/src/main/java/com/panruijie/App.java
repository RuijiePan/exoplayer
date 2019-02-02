package com.panruijie;

import android.app.Application;
import android.content.Context;

/**
 * Created by panruijie on 2019/2/2.
 **/
public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
