package net.alexandroid.utils.exoplayerhelper;

import android.widget.ImageView;

public interface ExoPlayerListener {

    void onThumbImageViewReady(ImageView imageView);

    void onLoadingStatusChanged(boolean isLoading, long bufferedPosition, int bufferedPercentage);

    void onPlayerPlaying(int currentWindowIndex);

    void onPlayerPaused(int currentWindowIndex);

    void onPlayerBuffering(int currentWindowIndex);

    void onPlayerStateEnded(int currentWindowIndex);

    void onPlayerStateIdle(int currentWindowIndex);

    void onPlayerError();

    void createExoPlayerCalled(boolean isToPrepare);

    void releaseExoPlayerCalled();

    void onVideoResumeDataLoaded(int window, long position, boolean isResumeWhenReady);

    void onVideoTapped();

    void onTracksChanged(int currentWindowIndex, int nextWindowIndex, boolean isPlayBackStateReady);

    void onMuteStateChanged(boolean isMuted);
}
