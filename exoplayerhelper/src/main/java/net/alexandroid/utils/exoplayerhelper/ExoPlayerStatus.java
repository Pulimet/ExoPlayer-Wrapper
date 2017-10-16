package net.alexandroid.utils.exoplayerhelper;

public interface ExoPlayerStatus {

    boolean isPlayerVideoMuted();

    int getCurrentWindowIndex();

    long getCurrentPosition();

    long getDuration();

    boolean isPlayerCreated();

    boolean isPlayerPrepared();

    boolean isPlayingAd();

}
