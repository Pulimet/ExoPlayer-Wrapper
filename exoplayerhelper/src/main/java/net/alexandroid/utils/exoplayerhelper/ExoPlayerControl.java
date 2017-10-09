package net.alexandroid.utils.exoplayerhelper;


import android.os.Bundle;

public interface ExoPlayerControl {

    void createPlayer(boolean isToPrepare);

    void preparePlayer();

    void releasePlayer();

    void releaseAdsLoader();

    void playerPause();

    void playerPlay();

    void setExoPlayerEventsListener(ExoPlayerListener pExoPlayerListenerListener);

    void setExoAdListener(ExoAdListener exoAdListener);

    void onActivityStart();

    void onActivityResume();

    void onActivityPause();

    void onActivityStop();

    void onActivityDestroy();

    void onSaveInstanceState(Bundle outState);

    boolean isPlayerVideoMuted();

    int getCurrentWindowIndex();
}
