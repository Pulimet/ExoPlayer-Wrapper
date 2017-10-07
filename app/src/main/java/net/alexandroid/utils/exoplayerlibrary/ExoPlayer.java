package net.alexandroid.utils.exoplayerlibrary;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.ext.ima.ImaAdsMediaSource;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;


@SuppressWarnings("WeakerAccess")
public class ExoPlayer implements View.OnClickListener,
        ExoPlayerControl,
        Player.EventListener,
        ImaAdsLoader.VideoAdPlayerCallback,
        ImaAdsMediaSource.AdsListener {

    // TODO 3. Compatible for integration at ynet main page
    // TODO 4. Compatible for integration at ynet slider activity
    // TODO 5. updateMutedStatus(); onAdPlay and onVideoPlay
    // TODO 6. Loading progress animation

    public static final String APPLICATION_NAME = "ExoPlayerLibrary";

    private SimpleExoPlayerView mExoPlayerView;
    private SimpleExoPlayer mPlayer;
    private ImaAdsLoader mImaAdsLoader;
    private DataSource.Factory mDataSourceFactory;
    private final DefaultLoadControl mLoadControl;
    private final DefaultBandwidthMeter mBandwidthMeter;

    private MediaSource mMediaSource;

    private ExoPlayerEvents mExoPlayerEvents;
    private Context mContext;

    private Handler mHandler;
    private float mTempCurrentVolume;
    private boolean isVideoMuted;
    private boolean isAdMuted;
    private boolean isRepeatModeOn;
    private boolean isAutoPlayOn;
    private TrackSelector mTrackSelector;
    private long mResumePosition;
    private int mResumeWindow;
    private Uri[] mVideosUris;
    private String mTagUrl;

    private ExoPlayer(Context context) {
        mHandler = new Handler();
        mContext = context;

        clearResumePosition();

        // Measures bandwidth during playback. Can be null if not required.
        mBandwidthMeter = new DefaultBandwidthMeter();

        // Produces DataSource instances through which media data is loaded.
        mDataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, APPLICATION_NAME), mBandwidthMeter);


        // LoadControl that controls when the MediaSource buffers more media, and how much media is buffered.
        // LoadControl is injected when the player is created.
        mLoadControl = (new DefaultLoadControl(
                new DefaultAllocator(false, 2 * 1024 * 1024),
                1000, 3000, 3000, 3000));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.muteBtn:
                if (mPlayer.isPlayingAd()) {
                    isAdMuted = !isAdMuted;
                    ((ImageView) v).setImageResource(isAdMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
                } else {
                    isVideoMuted = !isVideoMuted;
                    ((ImageView) v).setImageResource(isVideoMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
                }
                updateMutedStatus();
                break;
        }
    }

    private void setExoPlayerView(SimpleExoPlayerView exoPlayerView) {
        mExoPlayerView = exoPlayerView;
    }

    private void setUiControllersVisibility(boolean visibility) {
        mExoPlayerView.setUseController(visibility);
    }

    private void setRepeatModeOn(boolean isRepeatModeOn) {
        this.isRepeatModeOn = isRepeatModeOn;
    }

    private void setAutoPlayOn(boolean isAutoPlayOn) {
        this.isAutoPlayOn = isAutoPlayOn;
    }

    private void addMuteButton(boolean isAdMuted, boolean isVideoMuted) {
        this.isVideoMuted = isVideoMuted;
        this.isAdMuted = isAdMuted;

        FrameLayout frameLayout = mExoPlayerView.getOverlayFrameLayout();

        ImageView muteBtn = new ImageView(mContext);
        muteBtn.setId(R.id.muteBtn);
        muteBtn.setImageResource(this.isVideoMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        muteBtn.setLayoutParams(params);

        muteBtn.setOnClickListener(this);

        frameLayout.addView(muteBtn);
    }

    private void updateMutedStatus() {
        if ((mPlayer.isPlayingAd() && isAdMuted) || (!mPlayer.isPlayingAd() && isVideoMuted)) {
            mPlayer.setVolume(0f);
        } else {
            mPlayer.setVolume(mTempCurrentVolume);
        }
    }

    private void setExoPlayerEventsListener(ExoPlayerEvents exoPlayerEventsListener) {
        mExoPlayerEvents = exoPlayerEventsListener;
    }


    private void setVideoUrls(String[] urls) {
        mVideosUris = new Uri[urls.length];
        for (int i = 0; i < urls.length; i++) {
            mVideosUris[i] = Uri.parse(urls[i]);
        }
    }

    private void setTagUrl(String tagUrl) {
        mTagUrl = tagUrl;
    }

    private void createExoPlayer() {
        // TrackSelector that selects tracks provided by the MediaSource to be consumed by each of the available Renderers.
        // A TrackSelector is injected when the exoPlayer is created.
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(mBandwidthMeter);
        mTrackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        //mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector, mLoadControl);

        mExoPlayerView.setPlayer(mPlayer);
        mExoPlayerView.setControllerShowTimeoutMs(1500);

        mTempCurrentVolume = mPlayer.getVolume();
        updateMutedStatus();

        mPlayer.setRepeatMode(isRepeatModeOn ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
        mPlayer.setPlayWhenReady(isAutoPlayOn);

        mPlayer.addListener(this);

        createMediaSource();

        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            mPlayer.seekTo(mResumeWindow, mResumePosition);
        }
        mPlayer.prepare(mMediaSource, !haveResumePosition, false);
    }

    private void createMediaSource() {
        // A MediaSource defines the media to be played, loads the media, and from which the loaded media can be read.
        // A MediaSource is injected via ExoPlayer.prepare at the start of playback.

        MediaSource[] mediaSources = new MediaSource[mVideosUris.length];
        for (int i = 0; i < mVideosUris.length; i++) {
            mediaSources[i] = new HlsMediaSource(mVideosUris[i], mDataSourceFactory, null, null);
        }

        mMediaSource = mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);

        addAdsToMediaSource();
    }


    private void addAdsToMediaSource() {
        if (mMediaSource == null) {
            throw new IllegalStateException("setVideoUrls must be invoked before setTagUrl (mMediaSource is null)");
        }
        if (mTagUrl == null) {
            return;
        }

        if (mImaAdsLoader == null) {
            mImaAdsLoader = new ImaAdsLoader(mContext, Uri.parse(mTagUrl));
            mImaAdsLoader.addCallback(this);
        }

        mMediaSource = new ImaAdsMediaSource(mMediaSource,
                mDataSourceFactory,
                mImaAdsLoader,
                mExoPlayerView.getOverlayFrameLayout(),
                mHandler,
                this);
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            updateResumePosition();
            mPlayer.release();
            mPlayer = null;
            mTrackSelector = null;
        }
    }

    private void releaseAdsLoader() {
        if (mImaAdsLoader != null) {
            mImaAdsLoader.release();
            mImaAdsLoader = null;
            mExoPlayerView.getOverlayFrameLayout().removeAllViews();
        }
    }

    private void updateResumePosition() {
        mResumeWindow = mPlayer.getCurrentWindowIndex();
        mResumePosition = Math.max(0, mPlayer.getContentPosition());
    }

    private void clearResumePosition() {
        mResumeWindow = C.INDEX_UNSET;
        mResumePosition = C.TIME_UNSET;
    }

    @SuppressWarnings("SameParameterValue")
    public static class Builder {

        private ExoPlayer mExoPlayer;

        public Builder(Context context) {
            mExoPlayer = new ExoPlayer(context);
        }

        public Builder setExoPlayerView(SimpleExoPlayerView exoPlayerView) {
            mExoPlayer.setExoPlayerView(exoPlayerView);
            return this;
        }

        public Builder addMuteButton(boolean isAdMuted, boolean isVideoMuted) {
            mExoPlayer.addMuteButton(isAdMuted, isVideoMuted);
            return this;
        }

        public Builder setUiControllersVisibility(boolean visibility) {
            mExoPlayer.setUiControllersVisibility(visibility);
            return this;
        }

        public Builder setVideoUrls(String... urls) {
            mExoPlayer.setVideoUrls(urls);
            return this;
        }

        public Builder setTagUrl(String tagUrl) {
            mExoPlayer.setTagUrl(tagUrl);
            return this;
        }

        public Builder setRepeatModeOn(boolean isOn) {
            mExoPlayer.setRepeatModeOn(isOn);
            return this;
        }

        public Builder setAutoPlayOn(boolean isAutoPlayOn) {
            mExoPlayer.setAutoPlayOn(isAutoPlayOn);
            return this;
        }

        public Builder setExoPlayerEventsListener(ExoPlayerEvents exoPlayerEventsListener) {
            mExoPlayer.setExoPlayerEventsListener(exoPlayerEventsListener);
            return this;
        }

        public ExoPlayer build() {
            mExoPlayer.createExoPlayer();
            return mExoPlayer;
        }


    }

    /**
     * ExoPlayer Player.EventListener
     */
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    /**
     * ImaAdsLoader.VideoAdPlayerCallback
     */

    @Override
    public void onPlay() {

    }

    @Override
    public void onVolumeChanged(int pI) {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onEnded() {

    }

    @Override
    public void onError() {

    }

    /**
     * ImaAdsMediaSource.AdsListener
     */
    @Override
    public void onAdLoadError(IOException error) {

    }

    @Override
    public void onAdClicked() {

    }

    @Override
    public void onAdTapped() {

    }

    /**
     * ExoPlayerControl interface methods
     */
    @Override
    public void onInitPlayer() {

    }

    @Override
    public void onReleasePlayer() {

    }

    @Override
    public void onPausePlayer() {

    }

    @Override
    public void onPlayPlayer() {

    }

    @Override
    public void onActivityStart() {
        if (Util.SDK_INT > 23) {
            createExoPlayer();
        }
    }

    @Override
    public void onActivityResume() {
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            createExoPlayer();
        }
    }

    @Override
    public void onActivityPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onActivityStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    public void onActivityDestroy() {
        releaseAdsLoader();
    }
}
