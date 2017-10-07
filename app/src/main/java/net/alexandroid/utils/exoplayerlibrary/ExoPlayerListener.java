package net.alexandroid.utils.exoplayerlibrary;

public interface ExoPlayerListener {

    void onLoadingStatusChanged(boolean isLoading);

    void onPlayerPlaying();

    void onPlayerPaused();

    void onPlayerBuffering();

    void onPlayerStateEnded();

    void onPlayerStateIdle();

    void onPlayerError();

    void createExoPlayerCalled();

    void releaseExoPlayerCalled();

    void onVideoResumeDataLoaded(int window, long position, boolean isResumeWhenReady);
}
