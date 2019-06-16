package net.alexandroid.utils.exoplayerhelper;


public interface ExoAdListener {

    void onAdPlay();

    void onAdPause();

    void onAdResume();

    void onAdEnded();

    void onAdError();

    void onAdClicked();

    void onAdTapped();

    void onBuffering();
}
