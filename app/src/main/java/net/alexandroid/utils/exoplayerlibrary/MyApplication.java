package net.alexandroid.utils.exoplayerlibrary;

import android.app.Application;

import net.alexandroid.utils.mylog.MyLog;

/**
 * Created on 10/4/2017.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.init(this);
        MyLog.setTag("ZAQ");
    }

}
