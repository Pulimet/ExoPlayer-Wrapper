package net.alexandroid.utils.exoplayerlibrary;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ui.PlayerView;

import net.alexandroid.utils.exoplayerhelper.ExoPlayerHelper;
import net.alexandroid.utils.mylog.MyLog;

public class SimplePlayerActivity extends AppCompatActivity {
    public static final String SAMPLE_1 = "http://cdn-fms.rbs.com.br/vod/hls_sample1_manifest.m3u8";
    public static final String TEST_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";

    private ExoPlayerHelper mExoPlayerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_player);

        PlayerView exoPlayerView = findViewById(R.id.exoPlayerView);

        mExoPlayerHelper = new ExoPlayerHelper.Builder(this, exoPlayerView)
                .setUiControllersVisibility(true)
                .setAutoPlayOn(true)
                .setVideoUrls(SAMPLE_1)
                .setTagUrl(TEST_TAG_URL)
                .addSavedInstanceState(savedInstanceState)
                .createAndPrepare();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mExoPlayerHelper.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyLog.i("onActivityStart");
        mExoPlayerHelper.onActivityStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLog.i("onActivityResume");
        mExoPlayerHelper.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyLog.i("onActivityPause");
        mExoPlayerHelper.onActivityPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyLog.i("onActivityStop");
        mExoPlayerHelper.onActivityStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.e("onActivityDestroy");
        mExoPlayerHelper.onActivityDestroy();
    }

}
