package net.alexandroid.utils.exoplayerlibrary;


import android.content.Context;
import android.net.Uri;
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

    // TODO 1. onPause/onResume & onStop/onStart while ad/content playing
    // TODO 2. onScreen rotation
    // TODO 3. Compatible for integration at ynet main page
    // TODO 4. Compatible for integration at ynet slider activity

    public static final String APPLICATION_NAME = "ExoPlayerLibrary";

    private SimpleExoPlayerView mExoPlayerView;
    private SimpleExoPlayer mPlayer;
    private ImaAdsLoader mImaAdsLoader;
    private DataSource.Factory mDataSourceFactory;
    private final DefaultLoadControl mLoadControl;
    private final DefaultBandwidthMeter mBandwidthMeter;

    private MediaSource mMediaSource;

    private ExoPlayerListener mExoPlayerListener;
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
    private boolean isResumePlayWhenReady;
    private Uri[] mVideosUris;
    private String mTagUrl;
    private ExoAdListener mExoAdListener;
    private ImageView mMuteBtn;
    private ProgressBar mProgressBar;

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
                    isVideoMuted = isAdMuted = !isAdMuted;
                } else {
                    isAdMuted = isVideoMuted = !isVideoMuted;
                }
                ((ImageView) v).setImageResource(isVideoMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
                updateMutedStatus();
                break;
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
        frameLayout.addView(mProgressBar);
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

    private void setExoPlayerEventsListener(ExoPlayerListener pExoPlayerListenerListener) {
        mExoPlayerListener = pExoPlayerListenerListener;
    }

    private void setExoAdListener(ExoAdListener exoAdListener) {
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

    private void createExoPlayer() {
        if (mExoPlayerListener != null) {
            mExoPlayerListener.createExoPlayerCalled();
        }
        if (mPlayer != null) {
            return;
        }
        // TrackSelector that selects tracks provided by the MediaSource to be consumed by each of the available Renderers.
        // A TrackSelector is injected when the exoPlayer is created.
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(mBandwidthMeter);
        mTrackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        //mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector, mLoadControl);

        mExoPlayerView.setPlayer(mPlayer);
        mExoPlayerView.setControllerShowTimeoutMs(1500);

        mTempCurrentVolume = mPlayer.getVolume();

        mPlayer.setRepeatMode(isRepeatModeOn ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
        mPlayer.setPlayWhenReady(isAutoPlayOn);

        mPlayer.addListener(this);

        createMediaSource();

        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            mPlayer.setPlayWhenReady(isResumePlayWhenReady);
            mPlayer.seekTo(mResumeWindow, mResumePosition);

            if(mExoPlayerListener != null) {
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
        if (mExoPlayerListener != null) {
            mExoPlayerListener.releaseExoPlayerCalled();
        }
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
        isResumePlayWhenReady = mPlayer.getPlayWhenReady();
        mResumeWindow = mPlayer.getCurrentWindowIndex();
        mResumePosition = Math.max(0, mPlayer.getContentPosition());
    }

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
        updateMutedStatus();
    }

    private void onPlayerPaused() {
        setProgressVisible(false);
    }

    private void setProgressVisible(boolean visible) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void onAdEnded() {
        updateMutedStatus();
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


        public Builder setExoPlayerEventsListener(ExoPlayerListener pExoPlayerListenerListener) {
            mExoPlayer.setExoPlayerEventsListener(pExoPlayerListenerListener);
            return this;
        }

        public Builder setExoAdEventsListener(ExoAdListener pExoAdEventsListener) {
            mExoPlayer.setExoAdListener(pExoAdEventsListener);
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
        mExoAdListener.onAdPlay();
    }

    @Override
    public void onVolumeChanged(int pI) {

    }

    @Override
    public void onPause() {
        mExoAdListener.onAdPause();
    }

    @Override
    public void onResume() {
        mExoAdListener.onAdResume();
    }

    @Override
    public void onEnded() {
        onAdEnded();
        mExoAdListener.onAdEnded();
    }

    @Override
    public void onError() {
        mExoAdListener.onAdError();
    }

    /**
     * ImaAdsMediaSource.AdsListener
     */
    @Override
    public void onAdLoadError(IOException error) {
        mExoAdListener.onAdLoadError();
    }

    @Override
    public void onAdClicked() {
        mExoAdListener.onAdClicked();
    }

    @Override
    public void onAdTapped() {
        mExoAdListener.onAdTapped();
    }

    /**
     * ExoPlayerControl interface methods
     */
    @Override
    public void onInitPlayer() {
        createExoPlayer();
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
