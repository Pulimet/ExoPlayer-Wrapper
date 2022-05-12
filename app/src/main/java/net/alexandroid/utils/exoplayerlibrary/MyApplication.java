package net.alexandroid.utils.exoplayerlibrary;

import androidx.multidex.MultiDexApplication;

import net.alexandroid.utils.mylog.MyLog;

public class MyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.init(this, "ZAQ");
    }
}
