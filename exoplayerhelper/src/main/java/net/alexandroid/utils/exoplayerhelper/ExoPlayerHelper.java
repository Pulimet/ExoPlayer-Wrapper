package net.alexandroid.utils.exoplayerhelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.TracksInfo;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AdOverlayInfo;
import com.google.android.exoplayer2.ui.AdViewProvider;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class ExoPlayerHelper implements
        View.OnClickListener,
        View.OnTouchListener,
        ExoPlayerControl,
        ExoPlayerStatus,
        Player.Listener,
        VideoAdPlayer.VideoAdPlayerCallback,
        AdEvent.AdEventListener {

    public static final String PARAM_AUTO_PLAY = "PARAM_AUTO_PLAY";
    public static final String PARAM_WINDOW = "PARAM_WINDOW";
    public static final String PARAM_POSITION = "PARAM_POSITION";
    public static final String PARAM_IS_AD_WAS_SHOWN = "PARAM_IS_AD_WAS_SHOWN";

    private final Context mContext;

    private final StyledPlayerView mExoPlayerView;
    private ExoPlayer mPlayer;
    private ImaAdsLoader mImaAdsLoader;
    private DataSource.Factory mDataSourceFactory;
    private DefaultLoadControl mLoadControl;
    private MediaSource mMediaSource;

    private ExoAdListener mExoAdListener;
    private ExoPlayerListener mExoPlayerListener;
    private ExoThumbListener mExoThumbListener;

    private ProgressBar mProgressBar;
    private ImageView mBtnMute;
    private ImageView mBtnFullScreen;
    private ImageView mThumbImage;

    private Uri[] mVideosUris;
    private ArrayList<String> mSubTitlesUrls;
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
    private boolean isLiveStreamSupportEnabled;
    private final LinearLayout mBottomProgress;
    private UUID adIdentifier;


    private ExoPlayerHelper(Context context, StyledPlayerView exoPlayerView) {
        if (context == null) {
            throw new IllegalArgumentException("ExoPlayerHelper constructor - Context can't be null");
        }

        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("ExoPlayerHelper constructor - Context must be an instance of Activity");
        }

        if (exoPlayerView == null) {
            throw new IllegalArgumentException("ExoPlayerHelper constructor - SimpleExoPlayerView can't be null");
        }

        mContext = context;
        mExoPlayerView = exoPlayerView;

        mBottomProgress = mExoPlayerView.findViewById(R.id.bottom_progress);

        setVideoClickable();
        setControllerListener();
        init();
    }

    private void addProgressBar(int color) {
        final FrameLayout frameLayout = mExoPlayerView.getOverlayFrameLayout();
        if (frameLayout == null) {
            return;
        }
        mProgressBar = frameLayout.findViewById(R.id.progressBar);
        if (mProgressBar != null) {
            return;
        }
        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleLarge);
        mProgressBar.setId(R.id.progressBar);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        mProgressBar.setLayoutParams(params);
        mProgressBar.setIndeterminate(true);
        mProgressBar.getIndeterminateDrawable().setColorFilter(
                color == 0 ? Color.RED : color,
                android.graphics.PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.GONE);
        frameLayout.addView(mProgressBar);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setVideoClickable() {
        mExoPlayerView.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setControllerListener() {
        mExoPlayerView.findViewById(R.id.exo_play).setOnTouchListener(this);
        mExoPlayerView.findViewById(R.id.exo_pause).setOnTouchListener(this);

        mBtnMute = mExoPlayerView.findViewById(R.id.btnMute);
        mBtnFullScreen = mExoPlayerView.findViewById(R.id.btnFullScreen);
        mBtnMute.setOnTouchListener(this);
        mBtnFullScreen.setOnTouchListener(this);
    }


    private void init() {
        // Measures bandwidth during playback. Can be null if not required.
        final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(
                mContext
        ).build();

        // Produces DataSource instances through which media data is loaded.
        mDataSourceFactory = new DefaultDataSource.Factory(
                mContext, new DefaultHttpDataSource.Factory()
                .setUserAgent(Util.getUserAgent(mContext, mContext.getString(R.string.app_name)))
                .setTransferListener(bandwidthMeter)
        );


        // LoadControl that controls when the MediaSource buffers more media, and how much media is buffered.
        // LoadControl is injected when the player is created.
        //removed deprecated DefaultLoadControl creation method
        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
        builder.setAllocator(new DefaultAllocator(true, 2 * 1024 * 1024));
        builder.setBufferDurationsMs(5000, 5000, 5000, 5000);
        builder.setPrioritizeTimeOverSizeThresholds(true);
        mLoadControl = builder.build();
    }

    // Player creation and release
    private void setVideoUrls(String[] urls) {
        adIdentifier = UUID.randomUUID();
        mVideosUris = new Uri[urls.length];
        for (int i = 0; i < urls.length; i++) {
            mVideosUris[i] = Uri.parse(urls[i]);
        }
    }

    private void setSubtitlesUrls(ArrayList<String> list) {
        mSubTitlesUrls = list;
    }

    private void createMediaSource() {
        // A MediaSource defines the media to be played, loads the media, and from which the loaded media can be read.
        // A MediaSource is injected via ExoPlayer.prepare at the start of playback.
        if (mVideosUris == null) {
            return;
        }
        MediaSource[] mediaSources = new MediaSource[mVideosUris.length];
        for (int i = 0; i < mVideosUris.length; i++) {
            mediaSources[i] = buildMediaSource(MediaItem.fromUri(mVideosUris[i]));

            if (mSubTitlesUrls != null && i < mSubTitlesUrls.size() & mSubTitlesUrls.get(i) != null) {
                mediaSources[i] = addSubTitlesToMediaSource(mediaSources[i], mSubTitlesUrls.get(i));
            }
        }

        mMediaSource = mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);

        addAdsToMediaSource();
    }

    private MediaSource addSubTitlesToMediaSource(MediaSource mediaSource, String subTitlesUrl) {
        final Uri uri = Uri.parse(subTitlesUrl);
        final MediaSource subtitleSource = new SingleSampleMediaSource.Factory(mDataSourceFactory)
                .createMediaSource(
                        new MediaItem.SubtitleConfiguration.Builder(uri)
                                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                                .setLanguage("en")
                                .setSelectionFlags(Format.NO_VALUE)
                                .build(), C.TIME_UNSET
                );
        return new MergingMediaSource(mediaSource, subtitleSource);
    }

    private MediaSource buildMediaSource(MediaItem mediaItem) {
        int type = Util.inferContentType(mediaItem.localConfiguration.uri);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(mDataSourceFactory).createMediaSource(mediaItem);
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(mDataSourceFactory).createMediaSource(mediaItem);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(mediaItem);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(mDataSourceFactory).createMediaSource(mediaItem);
            case C.TYPE_RTSP:
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private void addAdsToMediaSource() {
        if (mMediaSource == null) {
            throw new IllegalStateException("setVideoUrls must be invoked before setTagUrl (mMediaSource is null)");
        }
        if (mTagUrl == null || isAdWasShown) {
            return;
        }

        if (mImaAdsLoader == null) {
            mImaAdsLoader = new ImaAdsLoader.Builder(mContext)
                    .setAdEventListener(this)
                    .setVideoAdPlayerCallback(this)
                    .build();
            mImaAdsLoader.setPlayer(mPlayer);
        }
        // Set player using adsLoader.setPlayer before preparing the player.
        mMediaSource = new AdsMediaSource(
                mMediaSource,
                new DataSpec.Builder().setUri(Uri.parse(mTagUrl)).build(),
                adIdentifier,
                new DefaultMediaSourceFactory(mDataSourceFactory),
                mImaAdsLoader, new AdViewProvider() {

            @Nullable
            @Override
            public ViewGroup getAdViewGroup() {
                return mExoPlayerView;
            }

            @NonNull
            @Override
            public List<AdOverlayInfo> getAdOverlayInfos() {
                return mExoPlayerView.getAdOverlayInfos();
            }
        });
        /*
        mMediaSource.addEventListener(null, null);
        mMediaSource = new AdsMediaSource(
                mMediaSource,
                this,
                mImaAdsLoader,
                mExoPlayerView.getOverlayFrameLayout());
       */
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
        mThumbImage.setBackgroundColor(Color.BLACK);
        frameLayout.addView(mThumbImage);

        if (mExoThumbListener != null) {
            mExoThumbListener.onThumbImageViewReady(mThumbImage);
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
        if (!visibility) {
            AspectRatioFrameLayout frameLayout = mExoPlayerView.findViewById(R.id.exo_content_frame);
            frameLayout.setOnClickListener(this);
        }
    }

    @SuppressLint("RtlHardcoded")
    private void addMuteButton(boolean isAdMuted, boolean isVideoMuted) {
        this.isVideoMuted = isVideoMuted;
        this.isAdMuted = isAdMuted;
        mBtnMute.setImageResource(this.isVideoMuted ? R.drawable.ic_action_mute : R.drawable.ic_action_volume_up);

         /*
        FrameLayout frameLayout = mExoPlayerView.getOverlayFrameLayout();
        mBtnMute = new ImageView(mContext);
        mBtnMute.setId(R.id.muteBtn);
        mBtnMute.setImageResource(this.isVideoMuted ? R.drawable.ic_action_mute : R.drawable.ic_action_volume_up);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.LEFT | Gravity.BOTTOM;
        //params.bottomMargin = Math.round(3 * mExoPlayerView.getContext().getResources().getDisplayMetrics().density);
        mBtnMute.setLayoutParams(params);

        mBtnMute.setOnClickListener(this);

        frameLayout.addView(mBtnMute);
        */
    }

    private void updateMutedStatus() {
        boolean isMuted = (mPlayer.isPlayingAd() && isAdMuted) || (!mPlayer.isPlayingAd() && isVideoMuted);
        if (isMuted) {
            mPlayer.setVolume(0f);
        } else {
            mPlayer.setVolume(mTempCurrentVolume);
        }
        if (mBtnMute != null) {
            mBtnMute.setImageResource(isMuted ? R.drawable.ic_action_mute : R.drawable.ic_action_volume_up);
        }
    }

    private void enableCache(int maxCacheSizeMb) {
        final LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(maxCacheSizeMb * 1024 * 1024L);
        final File file = new File(mContext.getCacheDir(), "media");
        Log.d("ZAQ", "enableCache (" + maxCacheSizeMb + " MB), file: " + file.getAbsolutePath());
        final SimpleCache simpleCache = new SimpleCache(file, evictor, (DatabaseProvider) null);

        mDataSourceFactory = new CacheDataSource.Factory()
                .setCache(simpleCache)
                .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                .setUpstreamDataSourceFactory(mDataSourceFactory)
                .setCacheReadDataSourceFactory(new FileDataSource.Factory())
                .setCacheWriteDataSinkFactory(new CacheDataSink.Factory()
                        .setCache(simpleCache)
                        .setFragmentSize(2 * 1024 * 1024L)
                )
                .setEventListener(new CacheDataSource.EventListener() {
                    @Override
                    public void onCacheIgnored(int reason) {
                        Log.d("ZAQ", "onCacheIgnored");
                    }

                    @Override
                    public void onCachedBytesRead(long cacheSizeBytes, long cachedBytesRead) {
                        Log.d("ZAQ", "onCachedBytesRead , cacheSizeBytes: " + cacheSizeBytes + "   cachedBytesRead: " + cachedBytesRead);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        // On video tap
        if (mExoPlayerListener != null && v.getId() == R.id.exo_content_frame) {
            mExoPlayerListener.onVideoTapped();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP && mExoPlayerListener != null) {
            if (view.getId() == mExoPlayerView.getId()) {
                mExoPlayerListener.onVideoTapped();
            }
            if (view.getId() == R.id.exo_play) {
                if (mExoPlayerListener.onPlayBtnTap()) {
                    return true;
                }
            }
            if (view.getId() == R.id.exo_pause) {
                if (mExoPlayerListener.onPauseBtnTap()) {
                    return true;
                }
            }

            if (view.getId() == R.id.btnFullScreen) {
                if (mExoPlayerListener != null) {
                    mExoPlayerListener.onFullScreenBtnTap();
                }
                return true;
            }
            if (view.getId() == R.id.btnMute) {
                if (mPlayer.isPlayingAd()) {
                    isVideoMuted = isAdMuted = !isAdMuted;
                } else {
                    isAdMuted = isVideoMuted = !isVideoMuted;
                }
                ((ImageView) view).setImageResource(isVideoMuted ? R.drawable.ic_action_mute : R.drawable.ic_action_volume_up);
                updateMutedStatus();
                if (mExoPlayerListener != null) {
                    mExoPlayerListener.onMuteStateChanged(isVideoMuted);
                }
                return true;
            }
        }

        // Player block
        FrameLayout layout = mExoPlayerView.getOverlayFrameLayout();
        return layout != null && view.getId() == layout.getId();

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
        mResumeWindow = mPlayer.getCurrentMediaItemIndex();
        mResumePosition = Math.max(0, mPlayer.getContentPosition());
    }

    @SuppressWarnings("unused")
    private void clearResumePosition() {
        mResumeWindow = C.INDEX_UNSET;
        mResumePosition = C.TIME_UNSET;
    }

    private int getNextWindowIndex() {
        return mPlayer.getCurrentTimeline().getNextWindowIndex(mPlayer.getCurrentMediaItemIndex(), mPlayer.getRepeatMode(), false);
    }

    private int getPreviousWindowIndex() {
        return mPlayer.getCurrentTimeline().getPreviousWindowIndex(mPlayer.getCurrentMediaItemIndex(), mPlayer.getRepeatMode(), false);
    }

    // Player events, internal handle
    private void onPlayerBuffering() {
        if (mPlayer.getPlayWhenReady()) {
            setProgressVisible(true);
        }
    }

    private void onPlayerPlaying() {
        setProgressVisible(false);
        removeThumbImageView();
        updateMutedStatus();
    }

    private void onPlayerLoadingChanged() {
        liveStreamCheck();
    }

    private void liveStreamCheck() {
        if (isLiveStreamSupportEnabled) {
            final boolean isLiveStream = mPlayer.isCurrentMediaItemDynamic() || !mPlayer.isCurrentMediaItemSeekable();
            /*
            Log.e("ZAQ", "isCurrentWindowDynamic: " + mPlayer.isCurrentWindowDynamic());
            Log.e("ZAQ", "isCurrentWindowSeekable: " + mPlayer.isCurrentWindowDynamic());
            Log.e("ZAQ", "isLiveStream: " + isLiveStream);
            */
            mBottomProgress.setVisibility(isLiveStream ? View.GONE : View.VISIBLE);
        }
    }

    private void onPlayerPaused() {
        setProgressVisible(false);
    }

    private void onAdEnded() {
        updateMutedStatus();
        isAdWasShown = true;
    }

    @SuppressWarnings("SameParameterValue")
    public static class Builder {

        private final ExoPlayerHelper mExoPlayerHelper;

        public Builder(Context context, StyledPlayerView exoPlayerView) {
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

        public Builder setSubTitlesUrls(ArrayList<String> list) {
            mExoPlayerHelper.setSubtitlesUrls(list);
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

        public Builder setThumbImageViewEnabled(ExoThumbListener exoThumbListener) {
            mExoPlayerHelper.setExoThumbListener(exoThumbListener);
            return this;
        }

        @SuppressWarnings("unused")
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

        public Builder enableLiveStreamSupport() {
            mExoPlayerHelper.isLiveStreamSupportEnabled = true;
            return this;
        }


        public Builder addProgressBarWithColor(int colorAccent) {
            mExoPlayerHelper.addProgressBar(colorAccent);
            return this;
        }

        public Builder setFullScreenBtnVisible() {
            mExoPlayerHelper.setFullScreenBtnVisibility(true);
            return this;
        }

        public Builder setMuteBtnVisible() {
            mExoPlayerHelper.mBtnMute.setVisibility(View.VISIBLE);
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
     * ExoPlayerControl interface methods
     */
    @Override
    public void setExoThumbListener(ExoThumbListener exoThumbListener) {
        isThumbImageViewEnabled = true;
        mExoThumbListener = exoThumbListener;
    }

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
            mExoPlayerListener.createExoPlayerCalled(isToPrepare);
        }
        if (mPlayer != null) {
            return;
        }

        if (isThumbImageViewEnabled) {
            addThumbImageView();
        }
        mPlayer = new ExoPlayer.Builder(mContext, new DefaultRenderersFactory(mContext))
                .setTrackSelector(new DefaultTrackSelector(mContext))
                .setLoadControl(mLoadControl)
                .build();

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

        mPlayer.setMediaSource(mMediaSource);
        mPlayer.prepare();

        if (mResumeWindow != C.INDEX_UNSET && !mPlayer.isPlayingAd()) {
            mPlayer.setPlayWhenReady(isResumePlayWhenReady);
            mPlayer.seekTo(mResumeWindow, mResumePosition + 100);
            if (mExoPlayerListener != null) {
                mExoPlayerListener.onVideoResumeDataLoaded(mResumeWindow, mResumePosition, isResumePlayWhenReady);
            }
            // mExoPlayerView.postDelayed(checkFreeze, 1000);
        }
    }

    // It looks like the issue was solved and no need for this Runnable
    private final Runnable checkFreeze = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null && mPlayer.getPlaybackState() == Player.STATE_BUFFERING && mPlayer.getPlayWhenReady()) {
                Log.e("zaq", "Player.STATE_BUFFERING stuck issue");
                mPlayer.seekTo(mPlayer.getContentPosition() > 500 ? mPlayer.getContentPosition() - 500 : 0);
            }
        }
    };

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
        }
    }

    @Override
    public void seekToDefaultPosition() {
        if (mPlayer != null) {
            mPlayer.seekToDefaultPosition();
        }
    }

    @Override
    public void releaseAdsLoader() {
        if (mImaAdsLoader != null) {
            mImaAdsLoader.release();
            mImaAdsLoader = null;
            final FrameLayout layout = mExoPlayerView.getOverlayFrameLayout();
            if (layout != null) {
                layout.removeAllViews();
            }
        }
    }

    @Override
    public void updateVideoUrls(String... urls) {
        if (isPlayerPrepared) {
            throw new IllegalStateException("Can't update url's when player is prepared");
        } else {
            setVideoUrls(urls);
            createMediaSource();
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
    public void playerNext() {
        if (mPlayer != null) {
            seekTo(getNextWindowIndex(), 0);
        }
    }

    @Override
    public void playerPrevious() {
        if (mPlayer != null) {
            seekTo(getPreviousWindowIndex(), 0);
        }
    }

    @Override
    public void seekTo(int windowIndex, long positionMs) {
        if (mPlayer != null) {
            mPlayer.seekTo(windowIndex, positionMs);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void playerBlock() {
        if (mExoPlayerView != null && mExoPlayerView.getOverlayFrameLayout() != null) {
            mExoPlayerView.getOverlayFrameLayout().setOnTouchListener(this);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void playerUnBlock() {
        if (mExoPlayerView != null && mExoPlayerView.getOverlayFrameLayout() != null) {
            mExoPlayerView.getOverlayFrameLayout().setOnTouchListener(null);
        }
    }

    @Override
    public void setFullScreenBtnVisibility(boolean isVisible) {
        mBtnFullScreen.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(PARAM_IS_AD_WAS_SHOWN, !mPlayer.isPlayingAd());
        outState.putBoolean(PARAM_AUTO_PLAY, mPlayer.getPlayWhenReady());
        outState.putInt(PARAM_WINDOW, mPlayer.getCurrentMediaItemIndex());
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

    /**
     * ExoPlayerStatus interface methods
     */
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
        if (mPlayer == null) {
            return 0;
        } else {
            return mPlayer.getCurrentMediaItemIndex();
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mPlayer == null) {
            return 0;
        } else {
            return mPlayer.getCurrentPosition();
        }
    }

    @Override
    public long getDuration() {
        if (mPlayer == null) {
            return 0;
        } else {
            return mPlayer.getDuration();
        }
    }

    @Override
    public boolean isPlayingAd() {
        return mPlayer != null && mPlayer.isPlayingAd();
    }

    /**
     * ExoPlayer Player.EventListener methods
     */
    @Override
    public void onTracksInfoChanged(@NonNull TracksInfo tracksInfo) {
        if (mExoPlayerListener != null) {
            mExoPlayerListener.onTracksChanged(
                    mPlayer.getCurrentMediaItemIndex(),
                    getNextWindowIndex(), mPlayer.getPlaybackState() == Player.STATE_READY
            );
        }
    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        onPlayerLoadingChanged();
        if (mExoPlayerListener != null) {
            mExoPlayerListener.onLoadingStatusChanged(isLoading, mPlayer.getBufferedPosition(), mPlayer.getBufferedPercentage());
        }
    }

    @Override
    public void onPlaybackStateChanged(@Player.State int state) {
        playerStateChanged();
    }

    @Override
    public void onPlayWhenReadyChanged(
            boolean playWhenReady, @Player.PlayWhenReadyChangeReason int reason
    ) {
        playerStateChanged();
    }

    private void playerStateChanged() {
        if (mExoPlayerListener == null || mPlayer == null) {
            return;
        }
        final int state = mPlayer.getPlaybackState();
        switch (state) {
            case Player.STATE_READY:
                if (mPlayer.getPlayWhenReady()) {
                    onPlayerPlaying();
                    mExoPlayerListener.onPlayerPlaying(mPlayer.getCurrentMediaItemIndex());
                } else {
                    onPlayerPaused();
                    mExoPlayerListener.onPlayerPaused(mPlayer.getCurrentMediaItemIndex());
                }
                break;
            case Player.STATE_BUFFERING:
                onPlayerBuffering();
                mExoPlayerListener.onPlayerBuffering(mPlayer.getCurrentMediaItemIndex());
                break;
            case Player.STATE_ENDED:
                mExoPlayerListener.onPlayerStateEnded(mPlayer.getCurrentMediaItemIndex());
                break;
            case Player.STATE_IDLE:
                mExoPlayerListener.onPlayerStateIdle(mPlayer.getCurrentMediaItemIndex());
                break;
            default:
                Log.e("ExoPlayerHelper-zaq", "onPlayerStateChanged unknown: " + state);

        }
    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        String errorString = null;
        if (error instanceof ExoPlaybackException) {
            ExoPlaybackException e = (ExoPlaybackException) error;
            switch (e.type) {
                case ExoPlaybackException.TYPE_SOURCE:
                    // https://github.com/google/ExoPlayer/issues/2702
                    final IOException ex = e.getSourceException();
                    final String msg = ex.getMessage();
                    if (msg != null) {
                        Log.e("ExoPlayerHelper", msg);
                        errorString = msg;
                    }
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    final Exception exception = e.getRendererException();
                    if (exception.getMessage() != null) {
                        Log.e("ExoPlayerHelper", exception.getMessage());
                    }
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    final RuntimeException runtimeException = e.getUnexpectedException();
                    Log.e("ExoPlayerHelper", runtimeException.getMessage() == null ? "Message is null" : runtimeException.getMessage());
                    if (runtimeException.getMessage() == null) {
                        runtimeException.printStackTrace();
                    }
                    errorString = runtimeException.getMessage();
                    break;
                case ExoPlaybackException.TYPE_REMOTE:
                    break;
            }

            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                final Exception cause = e.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    final MediaCodecRenderer.DecoderInitializationException decoderInitException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitException.codecInfo == null) {
                        if (decoderInitException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                            errorString = mContext.getString(R.string.error_querying_decoders);
                        } else if (decoderInitException.secureDecoderRequired) {
                            errorString = mContext.getString(
                                    R.string.error_no_secure_decoder, decoderInitException.mimeType
                            );
                        } else {
                            errorString = mContext.getString(
                                    R.string.error_no_decoder, decoderInitException.mimeType
                            );
                        }
                    } else {
                        errorString = mContext.getString(
                                R.string.error_instantiating_decoder, decoderInitException.codecInfo.name
                        );
                    }
                }
            }
            if (errorString != null) {
                Log.e("ExoPlayerHelper", "errorString: " + errorString);
            }

            if (isBehindLiveWindow(e)) {
                createPlayer(true);
                Log.e("ExoPlayerHelper", "isBehindLiveWindow is true");
            }

            if (mExoPlayerListener != null) {
                mExoPlayerListener.onPlayerError(errorString);
            }
        } else {
            // TODO: impl!!
        }
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPositionDiscontinuity(
            @NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition,
            @Player.DiscontinuityReason int reason
    ) {
        // if (Player.DISCONTINUITY_REASON_SEEK == reason) {
        // onSeekProceeded
        // }
    }

    @Override
    public void onPlaybackParametersChanged(@NonNull PlaybackParameters playbackParameters) {

    }

    @Override
    public void onTimelineChanged(@NonNull Timeline timeline, @Player.TimelineChangeReason int reason) {

    }

    /**
     * ImaAdsLoader.VideoAdPlayerCallback
     */
    @Override
    public void onPlay(AdMediaInfo adMediaInfo) {
        if (mExoAdListener != null) {
            mExoAdListener.onAdPlay();
        }
    }

    @Override
    public void onVolumeChanged(AdMediaInfo adMediaInfo, int i) {

    }

    @Override
    public void onPause(AdMediaInfo adMediaInfo) {
        if (mExoAdListener != null) {
            mExoAdListener.onAdPause();
        }
        if (mExoPlayerView != null) {
            mExoPlayerView.removeCallbacks(checkFreeze);
        }
    }

    @Override
    public void onAdProgress(AdMediaInfo adMediaInfo, VideoProgressUpdate videoProgressUpdate) {

    }

    @Override
    public void onLoaded(AdMediaInfo adMediaInfo) {

    }

    @Override
    public void onResume(AdMediaInfo adMediaInfo) {
        if (mExoAdListener != null) {
            mExoAdListener.onAdResume();
        }
    }

    @Override
    public void onEnded(AdMediaInfo adMediaInfo) {
        onAdEnded();
        if (mExoAdListener != null) {
            mExoAdListener.onAdEnded();
        }
    }

    @Override
    public void onContentComplete() {

    }

    @Override
    public void onError(AdMediaInfo adMediaInfo) {
        if (mExoAdListener != null) {
            mExoAdListener.onAdError();
        }
    }

    @Override
    public void onBuffering(AdMediaInfo adMediaInfo) {
        if (mExoAdListener != null) {
            mExoAdListener.onBuffering();
        }
    }

    /**
     * AdEvent.AdEventListener
     */
    @Override
    public void onAdEvent(AdEvent adEvent) {
        if (mExoAdListener == null) {
            return;
        }
        switch (adEvent.getType()) {
            case TAPPED:
                mExoAdListener.onAdTapped();
                break;
            case CLICKED:
                mExoAdListener.onAdClicked();
                break;
        }
    }
}
