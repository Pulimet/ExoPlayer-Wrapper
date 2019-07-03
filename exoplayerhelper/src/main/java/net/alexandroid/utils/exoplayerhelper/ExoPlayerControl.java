package net.alexandroid.utils.exoplayerhelper;

import android.os.Bundle;

public interface ExoPlayerControl {

    void createPlayer(boolean isToPrepare);

    void preparePlayer();

    void releasePlayer();

    void releaseAdsLoader();

    void updateVideoUrls(String... urls);

    void playerPause();

    void playerPlay();

    void playerNext();

    void playerPrevious();

    void seekTo(int windowIndex, long positionMs);

    void seekToDefaultPosition();

    void setExoPlayerEventsListener(ExoPlayerListener pExoPlayerListenerListener);

    void setExoAdListener(ExoAdListener exoAdListener);

    void setExoThumbListener(ExoThumbListener exoThumbListener);

    void onActivityStart();

    void onActivityResume();

    void onActivityPause();

    void onActivityStop();

    void onActivityDestroy();

    void onSaveInstanceState(Bundle outState);

    void playerBlock();

    void playerUnBlock();

    void setFullScreenBtnVisibility(boolean isVisible);
}
