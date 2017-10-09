package net.alexandroid.utils.exoplayerhelper;


import android.os.Bundle;

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

    void onSaveInstanceState(Bundle outState);

    void onPreparePlayer();

    void setExoPlayerEventsListener(ExoPlayerListener pExoPlayerListenerListener);

    void setExoAdListener(ExoAdListener exoAdListener);
}
