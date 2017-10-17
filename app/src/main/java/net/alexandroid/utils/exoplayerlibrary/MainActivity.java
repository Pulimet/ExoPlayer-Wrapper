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

    public static final String SAMPLE_1 = "http://cdn-fms.rbs.com.br/vod/hls_sample1_manifest.m3u8";
    public static final String SAMPLE_2 = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8";
    public static final String SAMPLE_3 = "http://content.jwplatform.com/manifests/vM7nH0Kl.m3u8";
    public static final String SAMPLE_4 = "http://184.72.239.149/vod/smil:BigBuckBunny.smil/playlist.m3u8";
    public static final String SAMPLE_5 = "http://www.streambox.fr/playlists/test_001/stream.m3u8";
    public static final String SAMPLE_6 = "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8";
    public static final String SAMPLE_7 = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
    public static final String SAMPLE_8 = " http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8";



    public static final String THUMB_IMG_URL = "https://i0.wp.com/androidlibs.net/alexandroid/wp-content/uploads/2013/11/ava.jpg";


    private SimpleExoPlayerView mExoPlayerView;
    private ExoPlayerHelper mExoPlayerHelper;

    private void log() {
        MyLog.e("Duration: " + (mExoPlayerHelper.getDuration() / 1000) + "s.   "
                + "Current: " + (mExoPlayerHelper.getCurrentPosition() / 1000) + "s.    "
                + "Left: " + ((mExoPlayerHelper.getDuration() - mExoPlayerHelper.getCurrentPosition()) / 1000) + "s.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyLog.e("onActivityCreate");

        setButtons();

        mExoPlayerView = findViewById(R.id.exoPlayerView);

        mExoPlayerHelper = new ExoPlayerHelper.Builder(this, mExoPlayerView)
                //.enableCache(10)
                .addMuteButton(false, true)
                .setUiControllersVisibility(true)
                .setRepeatModeOn(true)
                .setAutoPlayOn(true)
                .setVideoUrls(SAMPLE_1, SAMPLE_2, SAMPLE_3, SAMPLE_4, SAMPLE_5, SAMPLE_6, SAMPLE_7, SAMPLE_8)
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
        findViewById(R.id.btnNext).setOnClickListener(this);
        findViewById(R.id.btnPrev).setOnClickListener(this);
        findViewById(R.id.btnBlock).setOnClickListener(this);
        findViewById(R.id.btnUnBlock).setOnClickListener(this);
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
            case R.id.btnNext:
                mExoPlayerHelper.playerNext();
                break;
            case R.id.btnPrev:
                mExoPlayerHelper.playerPrevious();
                break;
            case R.id.btnBlock:
                mExoPlayerHelper.playerBlock();
                break;
            case R.id.btnUnBlock:
                mExoPlayerHelper.playerUnBlock();
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
                .load(THUMB_IMG_URL)
                .placeholder(R.drawable.place_holder)
                .error(R.drawable.error_image)
                .into(imageView);
    }

    @Override
    public void onLoadingStatusChanged(boolean isLoading, long bufferedPosition, int bufferedPercentage) {
        MyLog.w("onLoadingStatusChanged, isLoading: " + isLoading +
                "   Buffered Position: " + bufferedPosition +
                "   Buffered Percentage: " + bufferedPercentage);
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
    public void onPlayerError(String errorString) {
        MyLog.e("onPlayerError");
    }

    @Override
    public void createExoPlayerCalled(boolean isToPrepare) {
        MyLog.d("createExoPlayerCalled, isToPrepare: " + isToPrepare);
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
    public void onPlayBtnTap() {
        MyLog.d("onPlayBtnTap");
    }

    @Override
    public void onPauseBtnTap() {
        MyLog.d("onPauseBtnTap");
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
