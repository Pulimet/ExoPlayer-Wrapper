package net.alexandroid.utils.exoplayerhelper;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;


@SuppressWarnings("WeakerAccess")
public class ExoPlayerHelper implements
        View.OnClickListener,
        ExoPlayerControl,
        Player.EventListener,
        ImaAdsLoader.VideoAdPlayerCallback,
        ImaAdsMediaSource.AdsListener {

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
    private DefaultLoadControl mLoadControl;
    private DefaultBandwidthMeter mBandwidthMeter;
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
    private boolean isThumbImageViewEnabled;

    private ExoPlayerHelper(Context context, SimpleExoPlayerView exoPlayerView) {
        mHandler = new Handler();
        mContext = context;
        mExoPlayerView = exoPlayerView;

        addProgressBar();
        setOverlayClickable();

        init();
    }

    private void init() {
        // Measures bandwidth during playback. Can be null if not required.
        mBandwidthMeter = new DefaultBandwidthMeter();

        // Produces DataSource instances through which media data is loaded.
        mDataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, mContext.getString(R.string.app_name)), mBandwidthMeter);


        // LoadControl that controls when the MediaSource buffers more media, and how much media is buffered.
        // LoadControl is injected when the player is created.
        mLoadControl = (new DefaultLoadControl(
                new DefaultAllocator(false, 2 * 1024 * 1024),
                1000, 3000, 3000, 3000));
    }

    // Player creation and release
    private void setVideoUrls(String[] urls) {
        mVideosUris = new Uri[urls.length];
        for (int i = 0; i < urls.length; i++) {
            mVideosUris[i] = Uri.parse(urls[i]);
        }
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

    private void setOverlayClickable() {
        mExoPlayerView.findViewById(R.id.exo_content_frame).setOnClickListener(this);
    }

    private void setProgressVisible(boolean visible) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
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

    private void setUiControllersVisibility(boolean visibility) {
        mExoPlayerView.setUseController(visibility);
    }

    @SuppressLint("RtlHardcoded")
    private void addMuteButton(boolean isAdMuted, boolean isVideoMuted) {
        this.isVideoMuted = isVideoMuted;
        this.isAdMuted = isAdMuted;

        FrameLayout frameLayout = mExoPlayerView.getOverlayFrameLayout();

        mMuteBtn = new ImageView(mContext);
        mMuteBtn.setId(R.id.muteBtn);
        mMuteBtn.setImageResource(this.isVideoMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
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

    private void enableCache(int maxCacheSizeMb) {
        LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(maxCacheSizeMb * 1024 * 1024);
        File file = new File(mContext.getCacheDir().getParentFile(), "media_vod");
        Log.e("ZAQ", "enableCache, file: " + file.getAbsolutePath());
        SimpleCache simpleCache = new SimpleCache(file, evictor);
        mDataSourceFactory = new CacheDataSourceFactory(simpleCache, mDataSourceFactory);
    }

    @Override
    public void onClick(View v) {
        // Mute button click
        if (v.getId() == R.id.muteBtn) {
            if (mPlayer.isPlayingAd()) {
                isVideoMuted = isAdMuted = !isAdMuted;
            } else {
                isAdMuted = isVideoMuted = !isVideoMuted;
            }
            ((ImageView) v).setImageResource(isVideoMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
            updateMutedStatus();
            if (mExoPlayerListener != null) {
                mExoPlayerListener.onMuteStateChanged(isVideoMuted);
            }
        }

        // On video tap
        if (mExoPlayerListener != null && v.getId() == R.id.exo_content_frame) {
            mExoPlayerListener.onVideoTapped();

/*            //TODO Test
            Log.e("ZAQ", "Duration: " + (mPlayer.getDuration() / 1000) + "s.");
            Log.e("ZAQ", "Current: " + (mPlayer.getContentPosition() / 1000) + "s.");
            Log.e("ZAQ", "Left: " + ((mPlayer.getDuration() - mPlayer.getContentPosition()) / 1000) + "s.");*/
        }
    }

    // Resume position saving
    private void addSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isAdWasShown = savedInstanceState.getBoolean(PARAM_IS_AD_WAS_SHOWN, false);
            isResumePlayWhenReady = savedInstanceState.getBoolean(PARAM_AUTO_PLAY, true);
            mResumeWindow = savedInstanceState.getInt(PARAM_WINDOW, C.INDEX_UNSET);
            mResumePosition = savedInstanceState.getLong(PARAM_POSITION, C.TIME_UNSET);
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

    private int getNextWindowIndex() {
        return mPlayer.getCurrentTimeline().getNextWindowIndex(mPlayer.getCurrentWindowIndex(), mPlayer.getRepeatMode());
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

    private void onAdEnded() {
        updateMutedStatus();
        isAdWasShown = true;
    }

    private void onAdUserClicked() {
        mImaAdsLoader.stopAd();
        isAdWasShown = true;
    }

    @SuppressWarnings("SameParameterValue")
    public static class Builder {

        private ExoPlayerHelper mExoPlayerHelper;

        public Builder(Context context, SimpleExoPlayerView exoPlayerView) {
            mExoPlayerHelper = new ExoPlayerHelper(context, exoPlayerView);
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
            mExoPlayerHelper.mTagUrl = tagUrl;
            return this;
        }

        public Builder setRepeatModeOn(boolean isOn) {
            mExoPlayerHelper.isRepeatModeOn = isOn;
            return this;
        }

        public Builder setAutoPlayOn(boolean isAutoPlayOn) {
            mExoPlayerHelper.isAutoPlayOn = isAutoPlayOn;
            return this;
        }

        public Builder setExoPlayerEventsListener(ExoPlayerListener exoPlayerListener) {
            mExoPlayerHelper.setExoPlayerEventsListener(exoPlayerListener);
            return this;
        }

        public Builder setExoAdEventsListener(ExoAdListener exoAdListener) {
            mExoPlayerHelper.setExoAdListener(exoAdListener);
            return this;
        }

        public Builder addSavedInstanceState(Bundle savedInstanceState) {
            mExoPlayerHelper.addSavedInstanceState(savedInstanceState);
            return this;
        }

        public Builder setThumbImageViewEnabled() {
            mExoPlayerHelper.isThumbImageViewEnabled = true;
            return this;
        }

        public Builder enableCache(int maxCacheSizeMb) {
            mExoPlayerHelper.enableCache(maxCacheSizeMb);
            return this;
        }

        /**
         * If you have a list of videos set isToPrepareOnResume to be false
         * to prevent auto prepare on activity onResume/onCreate
         */
        public Builder setToPrepareOnResume(boolean toPrepareOnResume) {
            mExoPlayerHelper.isToPrepareOnResume = toPrepareOnResume;
            return this;
        }

        /**
         * Probably you will feel a need to use that method when you need to show pre-roll ad
         * and you not interested in auto play. That method allows to separate player creation
         * from calling prepare()
         * Note: To play ad/content you ned to call preparePlayer()
         *
         * @return ExoPlayerHelper instance
         */
        public ExoPlayerHelper create() {
            mExoPlayerHelper.createPlayer(false);
            return mExoPlayerHelper;
        }

        /**
         * Note: If you added tagUrl ad would start playing automatic even if you had set setAutoPlayOn(false)
         *
         * @return ExoPlayerHelper instance
         */
        public ExoPlayerHelper createAndPrepare() {
            mExoPlayerHelper.createPlayer(true);
            return mExoPlayerHelper;
        }

    }


    /**
     * ExoPlayer Player.EventListener methods
     */
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        if (mExoPlayerListener != null) {
            mExoPlayerListener.onTracksChanged(
                    mPlayer.getCurrentWindowIndex(),
                    getNextWindowIndex(),
                    mPlayer.getPlaybackState() == Player.STATE_READY);
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (mExoPlayerListener != null) {
            mExoPlayerListener.onLoadingStatusChanged(isLoading, mPlayer.getBufferedPosition(), mPlayer.getBufferedPercentage());
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
                    mExoPlayerListener.onPlayerPlaying(mPlayer.getCurrentWindowIndex());
                } else {
                    onPlayerPaused();
                    mExoPlayerListener.onPlayerPaused(mPlayer.getCurrentWindowIndex());
                }
                break;
            case Player.STATE_BUFFERING:
                onPlayerBuffering();
                mExoPlayerListener.onPlayerBuffering(mPlayer.getCurrentWindowIndex());
                break;
            case Player.STATE_ENDED:
                mExoPlayerListener.onPlayerStateEnded(mPlayer.getCurrentWindowIndex());
                break;
            case Player.STATE_IDLE:
                mExoPlayerListener.onPlayerStateIdle(mPlayer.getCurrentWindowIndex());
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

        Log.e("ExoPlayerHelper", "Player.onPlayerError: " + error.type);
        switch (error.type) {
            case ExoPlaybackException.TYPE_SOURCE:
                IOException ioException = error.getSourceException();
                Log.e("ExoPlayerHelper", ioException.getMessage());
                break;
            case ExoPlaybackException.TYPE_RENDERER:
                Exception exception = error.getRendererException();
                Log.e("ExoPlayerHelper", exception.getMessage());
                break;
            case ExoPlaybackException.TYPE_UNEXPECTED:
                RuntimeException runtimeException = error.getUnexpectedException();
                Log.e("ExoPlayerHelper", runtimeException.getMessage());
                break;
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
    public void setExoPlayerEventsListener(ExoPlayerListener pExoPlayerListenerListener) {
        mExoPlayerListener = pExoPlayerListenerListener;
    }

    @Override
    public void setExoAdListener(ExoAdListener exoAdListener) {
        mExoAdListener = exoAdListener;
    }

    @Override
    public void createPlayer(boolean isToPrepare) {
        if (mExoPlayerListener != null) {
            mExoPlayerListener.createExoPlayerCalled();
        }
        if (mPlayer != null) {
            return;
        }

        if (isThumbImageViewEnabled) {
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
            preparePlayer();
        }
    }

    @Override
    public void preparePlayer() {
        if (mPlayer == null || isPlayerPrepared) {
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

    @Override
    public void releasePlayer() {
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

    @Override
    public void releaseAdsLoader() {
        if (mImaAdsLoader != null) {
            mImaAdsLoader.release();
            mImaAdsLoader = null;
            mExoPlayerView.getOverlayFrameLayout().removeAllViews();
        }
    }

    @Override
    public void playerPause() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void playerPlay() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void seekTo(int windowIndex, long positionMs) {
        if (mPlayer != null) {
            mPlayer.seekTo(windowIndex, positionMs);
        }
    }

    @Override
    public boolean isPlayerCreated() {
        return mPlayer != null;
    }

    @Override
    public boolean isPlayerPrepared() {
        return isPlayerPrepared;
    }

    @Override
    public boolean isPlayerVideoMuted() {
        return isVideoMuted;
    }

    @Override
    public int getCurrentWindowIndex() {
        if (mPlayer != null) {
            return mPlayer.getCurrentWindowIndex();
        } else {
            return 0;
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        } else {
            return 0;
        }
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
            createPlayer(isToPrepareOnResume);
        }
    }

    @Override
    public void onActivityResume() {
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            createPlayer(isToPrepareOnResume);
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
