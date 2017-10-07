package net.alexandroid.utils.exoplayerlibrary;


public interface ExoPlayerControl {

    void onInitPlayer();

    void onReleasePlayer();

    void onPausePlayer();

    void onPlayPlayer();

    void onActivityStart();

    void onActivityResume();

    void onActivityPause();

    void onActivityStop();

    void onActivityDestroy();
}
