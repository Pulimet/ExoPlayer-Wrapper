package net.alexandroid.utils.exoplayerlibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.squareup.picasso.Picasso;

import net.alexandroid.shpref.MyLog;
import net.alexandroid.utils.exoplayerhelper.ExoAdListener;
import net.alexandroid.utils.exoplayerhelper.ExoPlayerHelper;
import net.alexandroid.utils.exoplayerhelper.ExoPlayerListener;
import net.alexandroid.utils.exoplayerlibrary.list.ListActivity;

public class MainActivity extends AppCompatActivity
        implements ExoPlayerListener, ExoAdListener, View.OnClickListener {
    public static final String VIDEO_URL = "http://ynethd-i.akamaihd.net/i/cdnwiz/1017/091017_yedioth_baby_fix_bP3D0Kgw_800.mp4/master.m3u8";
    public static final String VIDEO_URL2 = "http://ynethd-i.akamaihd.net/i/cdnwiz/1017/0710171807_SWISS_GIANTS_7jHXuKJW_800.mp4/master.m3u8";
    public static final String PPLUS_TAG_URL = "https://pubads.g.doubleclick.net/gampad/live/ads?sz=640x480&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&url=&correlator=&iu=/6870/ynet_mobile/pplus.preroll.apps&description_url=http://pplus.ynet.co.il/articles/0,7340,L-5020780,00.html&cust_params=dcPath%3D11211.PnaiPlus%26yncd%3D5020780%26videoPosition%3DPreroll%26autoplay%3Dtrue%26videoPosition_autoplay%3DPreroll_false%26AppVersion%3D1.0%26View%3D1";
    public static final String TEST_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";


    private SimpleExoPlayerView mExoPlayerView;
    private ExoPlayerHelper mExoPlayerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyLog.e("onActivityCreate");

        setButtons();

        mExoPlayerView = findViewById(R.id.exoPlayerView);

        mExoPlayerHelper = new ExoPlayerHelper.Builder(this, mExoPlayerView)
                .setCustomCacheSize(50, 2)
                .addMuteButton(false, true)
                .setUiControllersVisibility(true)
                .setRepeatModeOn(true)
                .setAutoPlayOn(true)
                .setVideoUrls(VIDEO_URL, VIDEO_URL2)
                //.setTagUrl(TEST_TAG_URL)
                .setExoPlayerEventsListener(this)
                //.setExoAdEventsListener(this)
                .addSavedInstanceState(savedInstanceState)
                .setThumbImageViewEnabled()
                .createAndPrepare();
    }

    private void setButtons() {
        findViewById(R.id.btnOpenList).setOnClickListener(this);
        findViewById(R.id.btnPlay).setOnClickListener(this);
        findViewById(R.id.btnPause).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnOpenList:
                startActivity(new Intent(MainActivity.this, ListActivity.class));
                break;
            case R.id.btnPlay:
                mExoPlayerHelper.playerPlay();
                break;
            case R.id.btnPause:
                mExoPlayerHelper.playerPause();
                break;

        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        MyLog.i("onSaveInstanceState");
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

    /**
     * ExoPlayerListener
     */

    @Override
    public void onThumbImageViewReady(ImageView imageView) {
        Picasso.with(this)
                .load(getString(R.string.fakeImageUrl))
                .placeholder(R.drawable.no_image_wide)
                .error(R.drawable.red_y_logo)
                .into(imageView);
    }

    @Override
    public void onLoadingStatusChanged(boolean isLoading, long bufferedPosition, int bufferedPercentage) {
/*
        MyLog.d("onLoadingStatusChanged, isLoading: " + isLoading +
                "   Buffered Position: " + bufferedPosition +
                "   Buffered Percentage: " + bufferedPercentage);
*/
    }

    @Override
    public void onPlayerPlaying(int currentWindowIndex) {
        MyLog.d("onPlayerPlaying, currentWindowIndex: " + currentWindowIndex);
    }

    @Override
    public void onPlayerPaused(int currentWindowIndex) {
        MyLog.d("onPlayerPaused, currentWindowIndex: " + currentWindowIndex);
    }

    @Override
    public void onPlayerBuffering(int currentWindowIndex) {
        MyLog.d("onPlayerBuffering, currentWindowIndex: " + currentWindowIndex);
    }

    @Override
    public void onPlayerStateEnded(int currentWindowIndex) {
        MyLog.d("onPlayerStateEnded, currentWindowIndex: " + currentWindowIndex);
    }

    @Override
    public void onPlayerStateIdle(int currentWindowIndex) {
        MyLog.d("onPlayerStateIdle, currentWindowIndex: " + currentWindowIndex);
    }

    @Override
    public void onPlayerError() {
        MyLog.e("onPlayerError");
    }

    @Override
    public void createExoPlayerCalled() {
        MyLog.d("createExoPlayerCalled");
    }

    @Override
    public void releaseExoPlayerCalled() {
        MyLog.d("releaseExoPlayerCalled");
    }

    @Override
    public void onVideoResumeDataLoaded(int window, long position, boolean isResumeWhenReady) {
        MyLog.d("window: " + window + "  position: " + position + " autoPlay: " + isResumeWhenReady);
    }

    @Override
    public void onVideoTapped() {
        MyLog.d("onVideoTapped");
    }

    @Override
    public void onTracksChanged(int currentWindowIndex, int nextWindowIndex, boolean isPlayBackStateReady) {
        MyLog.d("currentWindowIndex: " + currentWindowIndex + "  nextWindowIndex: " + nextWindowIndex + " isPlayBackStateReady: " + isPlayBackStateReady);
    }

    @Override
    public void onMuteStateChanged(boolean isMuted) {

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
