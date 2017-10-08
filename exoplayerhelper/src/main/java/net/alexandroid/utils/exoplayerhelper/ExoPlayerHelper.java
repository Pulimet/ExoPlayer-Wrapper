package net.alexandroid.utils.exoplayerhelper;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

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
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;


@SuppressWarnings("WeakerAccess")
public class ExoPlayerHelper implements View.OnClickListener,
        ExoPlayerControl,
        Player.EventListener,
        ImaAdsLoader.VideoAdPlayerCallback,
        ImaAdsMediaSource.AdsListener {

    // TODO 1. Compatible for integration at ynet main page
    // TODO 2. Compatible for integration at ynet slider activity

    public static final String APPLICATION_NAME = "ExoPlayerLibrary";
    public static final String PARAM_AUTO_PLAY = "PARAM_AUTO_PLAY";
    public static final String PARAM_WINDOW = "PARAM_WINDOW";
    public static final String PARAM_POSITION = "PARAM_POSITION";
    public static final String PARAM_IS_AD_WAS_SHOWN = "PARAM_IS_AD_WAS_SHOWN";

    private Context mContext;
    private Handler mHandler;

    private SimpleExoPlayerView mExoPlayerView;
    private SimpleExoPlayer mPlayer;
    private ImaAdsLoader mImaAdsLoader;
    private DataSource.Factory mDataSourceFactory;
    private final DefaultLoadControl mLoadControl;
    private final DefaultBandwidthMeter mBandwidthMeter;
    private MediaSource mMediaSource;
    private TrackSelector mTrackSelector;

    private ExoAdListener mExoAdListener;
    private ExoPlayerListener mExoPlayerListener;

    private ProgressBar mProgressBar;
    private ImageView mMuteBtn;
    private ImageView mThumbImage;

    private Uri[] mVideosUris;
    private String mTagUrl;
    private long mResumePosition = C.TIME_UNSET;
    private int mResumeWindow = C.INDEX_UNSET;
    private float mTempCurrentVolume;
    private boolean isVideoMuted;
    private boolean isAdMuted;
    private boolean isRepeatModeOn;
    private boolean isAutoPlayOn;
    private boolean isResumePlayWhenReady;
    private boolean isAdWasShown;
    private boolean isPlayerPrepared;
    private boolean isToPrepareOnResume = true;
    private boolean isAdThumbImageView;

    private ExoPlayerHelper(Context context) {
        mHandler = new Handler();
        mContext = context;

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
        if (v.getId() == R.id.muteBtn) {
            if (mPlayer.isPlayingAd()) {
                isVideoMuted = isAdMuted = !isAdMuted;
            } else {
                isAdMuted = isVideoMuted = !isVideoMuted;
            }
            ((ImageView) v).setImageResource(isVideoMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
            updateMutedStatus();
        }
    }

    private void setExoPlayerView(SimpleExoPlayerView exoPlayerView) {
        mExoPlayerView = exoPlayerView;
        addProgressBar();
    }

    private void addProgressBar() {
        FrameLayout frameLayout = mExoPlayerView.getOverlayFrameLayout();
        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleLarge);
        mProgressBar.setId(R.id.progressBar);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mProgressBar.setLayoutParams(params);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);
        frameLayout.addView(mProgressBar);
    }

    private void setThumbImageViewTrue() {
        isAdThumbImageView = true;
    }

    private void addThumbImageView() {
        if (mThumbImage != null) {
            return;
        }
        AspectRatioFrameLayout frameLayout = mExoPlayerView.findViewById(R.id.exo_content_frame);
        mThumbImage = new ImageView(mContext);
        mThumbImage.setId(R.id.thumbImg);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        mThumbImage.setLayoutParams(params);
        frameLayout.addView(mThumbImage);

        if (mExoPlayerListener != null) {
            mExoPlayerListener.onThumbImageViewReady(mThumbImage);
        }
    }

    private void removeThumbImageView() {
        if (mThumbImage != null) {
            AspectRatioFrameLayout frameLayout = mExoPlayerView.findViewById(R.id.exo_content_frame);
            frameLayout.removeView(mThumbImage);
            mThumbImage = null;
        }
    }

    // If you have a list of videos set isToPrepareOnResume to be false
    // to prevent auto prepare on activity onResume/onCreate
    private void setToPrepareOnResume(boolean toPrepareOnResume) {
        isToPrepareOnResume = toPrepareOnResume;
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

    @SuppressLint("RtlHardcoded")
    private void addMuteButton(boolean isAdMuted, boolean isVideoMuted) {
        this.isVideoMuted = isVideoMuted;
        this.isAdMuted = isAdMuted;

        FrameLayout frameLayout = mExoPlayerView.getOverlayFrameLayout();

        mMuteBtn = new ImageView(mContext);
        mMuteBtn.setId(R.id.muteBtn);
        mMuteBtn.setImageResource(this.isAdMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        mMuteBtn.setLayoutParams(params);

        mMuteBtn.setOnClickListener(this);

        frameLayout.addView(mMuteBtn);
    }

    private void updateMutedStatus() {
        boolean isMuted = (mPlayer.isPlayingAd() && isAdMuted) || (!mPlayer.isPlayingAd() && isVideoMuted);
        if (isMuted) {
            mPlayer.setVolume(0f);
        } else {
            mPlayer.setVolume(mTempCurrentVolume);
        }
        if (mMuteBtn != null) {
            mMuteBtn.setImageResource(isMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
        }
    }


    public void setExoPlayerEventsListener(ExoPlayerListener pExoPlayerListenerListener) {
        mExoPlayerListener = pExoPlayerListenerListener;
    }

    public void setExoAdListener(ExoAdListener exoAdListener) {
        mExoAdListener = exoAdListener;
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


    private void addSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isAdWasShown = savedInstanceState.getBoolean(PARAM_IS_AD_WAS_SHOWN, false);
            isResumePlayWhenReady = savedInstanceState.getBoolean(PARAM_AUTO_PLAY, true);
            mResumeWindow = savedInstanceState.getInt(PARAM_WINDOW, C.INDEX_UNSET);
            mResumePosition = savedInstanceState.getLong(PARAM_POSITION, C.TIME_UNSET);
        }
    }

    private void createExoPlayer(boolean isToPrepare) {
        if (mExoPlayerListener != null) {
            mExoPlayerListener.createExoPlayerCalled();
        }
        if (mPlayer != null) {
            return;
        }

        if (isAdThumbImageView) {
            addThumbImageView();
        }

        // TrackSelector that selects tracks provided by the MediaSource to be consumed by each of the available Renderer's.
        // A TrackSelector is injected when the exoPlayer is created.
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(mBandwidthMeter);
        mTrackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        //mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);

        //noinspection deprecation
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector, mLoadControl);

        mExoPlayerView.setPlayer(mPlayer);
        mExoPlayerView.setControllerShowTimeoutMs(1500);
        mExoPlayerView.setControllerHideOnTouch(false);

        mTempCurrentVolume = mPlayer.getVolume();

        mPlayer.setRepeatMode(isRepeatModeOn ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
        mPlayer.setPlayWhenReady(isAutoPlayOn);
        mPlayer.addListener(this);

        createMediaSource();

        if (isToPrepare) {
            prepareExoPlayer();
        }
    }


    private void prepareExoPlayer() {
        if (isPlayerPrepared) {
            return;
        }
        isPlayerPrepared = true;
        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            mPlayer.setPlayWhenReady(isResumePlayWhenReady);
            mPlayer.seekTo(mResumeWindow, mResumePosition);

            if (mExoPlayerListener != null) {
                mExoPlayerListener.onVideoResumeDataLoaded(mResumeWindow, mResumePosition, isResumePlayWhenReady);
            }
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
        if (mTagUrl == null || isAdWasShown) {
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
        isPlayerPrepared = false;

        if (mExoPlayerListener != null) {
            mExoPlayerListener.releaseExoPlayerCalled();
        }
        if (mPlayer != null) {
            updateResumePosition();
            removeThumbImageView();
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
        isResumePlayWhenReady = mPlayer.getPlayWhenReady();
        mResumeWindow = mPlayer.getCurrentWindowIndex();
        mResumePosition = Math.max(0, mPlayer.getContentPosition());
    }

    @SuppressWarnings("unused")
    private void clearResumePosition() {
        mResumeWindow = C.INDEX_UNSET;
        mResumePosition = C.TIME_UNSET;
    }

    // Player events, internal handle

    private void onPlayerBuffering() {
        setProgressVisible(true);
    }

    private void onPlayerPlaying() {
        setProgressVisible(false);
        removeThumbImageView();
        updateMutedStatus();
    }

    private void onPlayerPaused() {
        setProgressVisible(false);
        removeThumbImageView();
    }

    private void setProgressVisible(boolean visible) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void onAdEnded() {
        updateMutedStatus();
        isAdWasShown = true;
    }

    private void onAdUserClicked() {
        mImaAdsLoader.stopAd();
    }

    @SuppressWarnings("SameParameterValue")
    public static class Builder {

        private ExoPlayerHelper mExoPlayerHelper;

        public Builder(Context context) {
            mExoPlayerHelper = new ExoPlayerHelper(context);
        }

        public Builder setExoPlayerView(SimpleExoPlayerView exoPlayerView) {
            mExoPlayerHelper.setExoPlayerView(exoPlayerView);
            return this;
        }

        public Builder addMuteButton(boolean isAdMuted, boolean isVideoMuted) {
            mExoPlayerHelper.addMuteButton(isAdMuted, isVideoMuted);
            return this;
        }


        public Builder setUiControllersVisibility(boolean visibility) {
            mExoPlayerHelper.setUiControllersVisibility(visibility);
            return this;
        }

        public Builder setVideoUrls(String... urls) {
            mExoPlayerHelper.setVideoUrls(urls);
            return this;
        }

        public Builder setTagUrl(String tagUrl) {
            mExoPlayerHelper.setTagUrl(tagUrl);
            return this;
        }

        public Builder setRepeatModeOn(boolean isOn) {
            mExoPlayerHelper.setRepeatModeOn(isOn);
            return this;
        }

        public Builder setAutoPlayOn(boolean isAutoPlayOn) {
            mExoPlayerHelper.setAutoPlayOn(isAutoPlayOn);
            return this;
        }

        public Builder setExoPlayerEventsListener(ExoPlayerListener pExoPlayerListenerListener) {
            mExoPlayerHelper.setExoPlayerEventsListener(pExoPlayerListenerListener);
            return this;
        }

        public Builder setExoAdEventsListener(ExoAdListener pExoAdEventsListener) {
            mExoPlayerHelper.setExoAdListener(pExoAdEventsListener);
            return this;
        }

        public Builder addSavedInstanceState(Bundle pSavedInstanceState) {
            mExoPlayerHelper.addSavedInstanceState(pSavedInstanceState);
            return this;
        }

        public Builder setThumbImageViewTrue() {
            mExoPlayerHelper.setThumbImageViewTrue();
            return this;
        }

        public Builder setToPrepareOnResume(boolean toPrepareOnResume) {
            mExoPlayerHelper.setToPrepareOnResume(toPrepareOnResume);
            return this;
        }

        public ExoPlayerHelper create() {
            mExoPlayerHelper.createExoPlayer(false);
            return mExoPlayerHelper;
        }

        public ExoPlayerHelper createAndPrepare() {
            mExoPlayerHelper.createExoPlayer(true);
            return mExoPlayerHelper;
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
        if (mExoPlayerListener != null) {
            mExoPlayerListener.onLoadingStatusChanged(isLoading);
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (mExoPlayerListener == null) {
            return;
        }
        switch (playbackState) {
            case Player.STATE_READY:
                if (playWhenReady) {
                    onPlayerPlaying();
                    mExoPlayerListener.onPlayerPlaying();
                } else {
                    onPlayerPaused();
                    mExoPlayerListener.onPlayerPaused();
                }
                break;
            case Player.STATE_BUFFERING:
                onPlayerBuffering();
                mExoPlayerListener.onPlayerBuffering();
                break;
            case Player.STATE_ENDED:
                mExoPlayerListener.onPlayerStateEnded();
                break;
            case Player.STATE_IDLE:
                mExoPlayerListener.onPlayerStateIdle();
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (mExoPlayerListener != null) {
            mExoPlayerListener.onPlayerError();
        }
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
        if (mExoAdListener != null) {
            mExoAdListener.onAdPlay();
        }
    }

    @Override
    public void onVolumeChanged(int pI) {

    }

    @Override
    public void onPause() {
        if (mExoAdListener != null) {
            mExoAdListener.onAdPause();
        }
    }

    @Override
    public void onResume() {
        if (mExoAdListener != null) {
            mExoAdListener.onAdResume();
        }
    }

    @Override
    public void onEnded() {
        onAdEnded();
        if (mExoAdListener != null) {
            mExoAdListener.onAdEnded();
        }
    }

    @Override
    public void onError() {
        if (mExoAdListener != null) {
            mExoAdListener.onAdError();
        }
    }

    /**
     * ImaAdsMediaSource.AdsListener
     */
    @Override
    public void onAdLoadError(IOException error) {
        if (mExoAdListener != null) {
            mExoAdListener.onAdLoadError();
        }
    }

    @Override
    public void onAdClicked() {
        onAdUserClicked();
        if (mExoAdListener != null) {
            mExoAdListener.onAdClicked();
        }
    }

    @Override
    public void onAdTapped() {
        if (mExoAdListener != null) {
            mExoAdListener.onAdTapped();
        }
    }

    /**
     * ExoPlayerControl interface methods
     */
    @Override
    public void onInitPlayer() {
        createExoPlayer(true);
    }

    @Override
    public void onPreparePlayer() {
        prepareExoPlayer();
    }

    @Override
    public void onReleasePlayer() {
        releasePlayer();
    }

    @Override
    public void onPausePlayer() {
        mPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onPlayPlayer() {
        mPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(PARAM_IS_AD_WAS_SHOWN, !mPlayer.isPlayingAd());
        outState.putBoolean(PARAM_AUTO_PLAY, mPlayer.getPlayWhenReady());
        outState.putInt(PARAM_WINDOW, mPlayer.getCurrentWindowIndex());
        outState.putLong(PARAM_POSITION, mPlayer.getContentPosition());
    }

    @Override
    public void onActivityStart() {
        if (Util.SDK_INT > 23) {
            createExoPlayer(isToPrepareOnResume);
        }
    }

    @Override
    public void onActivityResume() {
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            createExoPlayer(isToPrepareOnResume);
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
