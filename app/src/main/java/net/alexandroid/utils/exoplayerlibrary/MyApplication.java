package net.alexandroid.utils.exoplayerlibrary;

import android.app.Application;

import net.alexandroid.utils.mylog.MyLog;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.init(this, "ZAQ");
    }

}
