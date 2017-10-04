package net.alexandroid.utils.exoplayerlibrary;

import android.app.Application;

import net.alexandroid.shpref.MyLog;
import net.alexandroid.shpref.ShPref;

/**
 * Created on 10/4/2017.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ShPref.init(this, ShPref.APPLY);

        MyLog.showLogs(true);
        MyLog.setTag("ZAQ");
        MyLog.setPackageNameVisibility(false);
        MyLog.setThreadIdVisibility(false);
        MyLog.setIsTimeVisible(true);
        MyLog.setIsRemoveOverride(true);
    }

}
