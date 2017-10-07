package net.alexandroid.utils.exoplayerlibrary;


public interface ExoPlayerControl {

    void initPlayer();

    void releasePlayer();

    void pause();

    void play();

    void onActivityStart();

    void onActivityResume();

    void onActivityPause();

    void onActivityStop();

    void onActivityDestroy();
}
