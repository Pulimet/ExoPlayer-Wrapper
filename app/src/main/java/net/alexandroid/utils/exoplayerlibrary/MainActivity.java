package net.alexandroid.utils.exoplayerlibrary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import net.alexandroid.shpref.MyLog;

public class MainActivity extends AppCompatActivity
        implements ExoPlayerListener, ExoAdListener {
    public static final String VIDEO_URL = "http://ynethd-i.akamaihd.net/i/cdnwiz/0917/190917_maya_dagan_laisha_Qnw9LprZ_800.mp4/master.m3u8";
    public static final String PPLUS_TAG_URL = "https://pubads.g.doubleclick.net/gampad/live/ads?sz=640x480&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&url=&correlator=&iu=/6870/ynet_mobile/pplus.preroll.apps&description_url=http://pplus.ynet.co.il/articles/0,7340,L-5020780,00.html&cust_params=dcPath%3D11211.PnaiPlus%26yncd%3D5020780%26videoPosition%3DPreroll%26autoplay%3Dtrue%26videoPosition_autoplay%3DPreroll_false%26AppVersion%3D1.0%26View%3D1";
    public static final String TEST_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";


    private SimpleExoPlayerView mExoPlayerView;
    private ExoPlayer mExoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mExoPlayerView = findViewById(R.id.exoPlayerView);

        mExoPlayer = new ExoPlayer.Builder(this)
                .setExoPlayerView(mExoPlayerView)
                .addMuteButton(false, false)
                .setUiControllersVisibility(true)
                .setRepeatModeOn(true)
                .setAutoPlayOn(true)
                .setVideoUrls(VIDEO_URL)
                .setTagUrl(TEST_TAG_URL)
                .setExoPlayerEventsListener(this)
                .setExoAdEventsListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mExoPlayer.onActivityStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mExoPlayer.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mExoPlayer.onActivityPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mExoPlayer.onActivityStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExoPlayer.onActivityDestroy();
    }

    /**
     * ExoPlayerListener
     */

    @Override
    public void onLoadingStatusChanged(boolean isLoading) {
        //MyLog.d("Loading: " + isLoading);
    }

    @Override
    public void onPlayerPlaying() {
        MyLog.d("onPlayerPlaying");
    }

    @Override
    public void onPlayerPaused() {
        MyLog.d("onPlayerPaused");
    }

    @Override
    public void onPlayerBuffering() {
        MyLog.d("onPlayerBuffering");
    }

    @Override
    public void onPlayerStateEnded() {
        MyLog.d("onPlayerStateEnded");
    }

    @Override
    public void onPlayerStateIdle() {
        MyLog.d("onPlayerStateIdle");
    }

    @Override
    public void onPlayerError() {
        MyLog.e("onPlayerError");
    }


    /**
     * ExoAdListener
     */
    @Override
    public void onAdPlay() {
        MyLog.d("onAdPlay");
    }

    @Override
    public void onAdPause() {
        MyLog.d("onAdPause");
    }

    @Override
    public void onAdResume() {
        MyLog.d("onAdResume");
    }

    @Override
    public void onAdEnded() {
        MyLog.d("onAdEnded");
    }

    @Override
    public void onAdError() {
        MyLog.d("onAdError");
    }

    @Override
    public void onAdLoadError() {
        MyLog.d("onAdLoadError");
    }

    @Override
    public void onAdClicked() {
        MyLog.d("onAdClicked");
    }

    @Override
    public void onAdTapped() {
        MyLog.d("onAdTapped");
    }
}
