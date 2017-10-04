package net.alexandroid.utils.exoplayerlibrary;


import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.ext.ima.ImaAdsMediaSource;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

@SuppressWarnings("WeakerAccess")
public class ExoPlayer implements View.OnClickListener {

    // TODO 1. Listeners
    // TODO 2. Release SimpleExoPlayer and ImaAdsLoader (Demo app and: https://goo.gl/e4pgHR )
    // TODO 3. Compatible for integration at ynet main page
    // TODO 4. Compatible for integration at ynet slider activity


    public static final String APPLICATION_NAME = "ExoPlayerLibrary";

    private SimpleExoPlayerView mExoPlayerView;
    private SimpleExoPlayer mPlayer;
    private ImaAdsLoader mImaAdsLoader;
    private DataSource.Factory mDataSourceFactory;
    private final DefaultLoadControl mLoadControl;

    private MediaSource mMediaSource;
    private Context mContext;

    private float mTempCurrentVolume;
    private boolean isMuted;
    private boolean isRepeatModeOn;
    private final TrackSelector mTrackSelector;

    private ExoPlayer(Context context) {
        mContext = context;

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        // Produces DataSource instances through which media data is loaded.
        mDataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, APPLICATION_NAME), bandwidthMeter);

        // TrackSelector that selects tracks provided by the MediaSource to be consumed by each of the available Renderers.
        // A TrackSelector is injected when the exoPlayer is created.
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        mTrackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // LoadControl that controls when the MediaSource buffers more media, and how much media is buffered.
        // LoadControl is injected when the player is created.
        mLoadControl = (new DefaultLoadControl(
                new DefaultAllocator(false, 2 * 1024 * 1024),
                1000, 3000, 3000, 3000));

    }

    private void setExoPlayerView(SimpleExoPlayerView exoPlayerView) {
        mExoPlayerView = exoPlayerView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.muteBtn:
                isMuted = !isMuted;
                ((ImageView) v).setImageResource(isMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
                setMute(isMuted);
                break;
        }
    }

    private void setUiControllersVisibility(boolean visibility) {
        mExoPlayerView.setUseController(visibility);
    }

    private void setRepeatModeOn(boolean isOn) {
        isRepeatModeOn = isOn;
    }

    private void setVideoUrls(String[] urls) {
        Uri[] uris = new Uri[urls.length];
        for (int i = 0; i < urls.length; i++) {
            uris[i] = Uri.parse(urls[i]);
        }

        // A MediaSource defines the media to be played, loads the media, and from which the loaded media can be read.
        // A MediaSource is injected via ExoPlayer.prepare at the start of playback.

        MediaSource[] mediaSources = new MediaSource[uris.length];
        for (int i = 0; i < uris.length; i++) {
            mediaSources[i] = new HlsMediaSource(uris[i], mDataSourceFactory, null, null);
        }

        mMediaSource = mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);
    }

    private void setTagUrl(String tagUrl) {
        if (mMediaSource == null) {
            throw new IllegalStateException("setVideoUrls must be invoked before setTagUrl (mMediaSource is null)");
        }
        mImaAdsLoader = new ImaAdsLoader(mContext, Uri.parse(tagUrl));

        mMediaSource = new ImaAdsMediaSource(mMediaSource,
                mDataSourceFactory,
                mImaAdsLoader,
                mExoPlayerView.getOverlayFrameLayout(),
                null,
                null);
    }

    private void addMuteButton(boolean isMuted) {
        this.isMuted = isMuted;

        FrameLayout frameLayout = mExoPlayerView.getOverlayFrameLayout();

        ImageView muteBtn = new ImageView(mContext);
        muteBtn.setId(R.id.muteBtn);
        muteBtn.setImageResource(isMuted ? R.drawable.mute_ic : R.drawable.sound_on_ic);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        muteBtn.setLayoutParams(params);

        muteBtn.setOnClickListener(this);

        frameLayout.addView(muteBtn);
    }

    private void createExoPlayer() {
        //mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector, mLoadControl);

        mExoPlayerView.setPlayer(mPlayer);
        mExoPlayerView.setControllerShowTimeoutMs(1500);

        mTempCurrentVolume = mPlayer.getVolume();
        setMute(isMuted);

        mPlayer.setRepeatMode(isRepeatModeOn ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
        mPlayer.setPlayWhenReady(true);

        mPlayer.prepare(mMediaSource);
    }

    private void setMute(boolean isMute) {
        isMuted = isMute;
        if (isMute) {
            mPlayer.setVolume(0f);
        } else {
            mPlayer.setVolume(mTempCurrentVolume);
        }
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

        public Builder addMuteButton(boolean isMuted) {
            mExoPlayer.addMuteButton(isMuted);
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

        public ExoPlayer build() {
            mExoPlayer.createExoPlayer();
            return mExoPlayer;
        }

    }

}
