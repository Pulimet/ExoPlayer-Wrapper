[ ![Download](https://api.bintray.com/packages/pulimet/utils/exoplayerhelper/images/download.svg) ](https://bintray.com/pulimet/utils/exoplayerhelper/_latestVersion)      [![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-ExoPlayer%20wrapper-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/6717)

<img align="right" width="200" src="https://raw.githubusercontent.com/Pulimet/ExoPlayer-Wrapper/master/art/1.jpg">


# ExoPlayer-Wrapper

Simple ExoPlayer v. 2.9.0 wrapper

# Installation

- Add the dependency from jCenter to your app's (not project) build.gradle file:

```sh
repositories {
    jcenter()
}

dependencies {
    compile 'net.alexandroid.utils:exoplayerhelper:2.17'
}
```

<img align="right" width="200" src="https://raw.githubusercontent.com/Pulimet/ExoPlayer-Wrapper/master/art/2.jpg">

# How to use it

Add view:
```xml
    <com.google.android.exoplayer2.ui.SimpleExoPlayerView
        android:id="@+id/exoPlayerView"
        android:layout_width="match_parent"
        android:layout_height="200dp"/>
```

And in your code:
```java

public class MainActivity extends AppCompatActivity 
        implements ExoPlayerListener, ExoAdListener{
        
    public static final String SAMPLE_1 = "http://...video.m3u8";
    public static final String TEST_TAG_URL = "https://pubads.g.doubleclick.net/...";
    private ExoPlayerHelper mExoPlayerHelper;
```
...

```java
 
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

        SimpleExoPlayerView exoPlayerView = findViewById(R.id.exoPlayerView);

        mExoPlayerHelper = new ExoPlayerHelper.Builder(this, exoPlayerView)
                .addMuteButton(false, false)
                .setUiControllersVisibility(false)
                .setRepeatModeOn(true)
                .setAutoPlayOn(false)
                .setVideoUrls(SAMPLE_1)
                .setTagUrl(TEST_TAG_URL)
                .setExoPlayerEventsListener(this)
                .setExoAdEventsListener(this)
                .addSavedInstanceState(savedInstanceState)
                .setThumbImageViewEnabled(this)
                .enableLiveStreamSupport()
                .addProgressBarWithColor(getResources().getColor(R.color.colorAccent))
                .setFullScreenBtnVisible()
                .setMuteBtnVisible()
                .createAndPrepare();
}


// Lifecycle events

@Override
protected void onSaveInstanceState(Bundle outState) {
    mExoPlayerHelper.onSaveInstanceState(outState);
    super.onSaveInstanceState(outState);
}

@Override
protected void onStart() {
    super.onStart();
    mExoPlayerHelper.onActivityStart();
}

@Override
protected void onResume() {
    super.onResume();
    mExoPlayerHelper.onActivityResume();
}

@Override
protected void onPause() {
    super.onPause();
    mExoPlayerHelper.onActivityPause();
}

@Override
protected void onStop() {
    super.onStop();
    mExoPlayerHelper.onActivityStop();
}

@Override
protected void onDestroy() {
    super.onDestroy();
    mExoPlayerHelper.onActivityDestroy();
}

// ExoPlayerListener methods

@Override
public void onThumbImageViewReady(ImageView imageView) {
    Picasso.with(this)
            .load(THUMB_IMG_URL)
            .placeholder(R.drawable.place_holder)
            .error(R.drawable.error_image)
            .into(imageView);
}
@Override
public void onLoadingStatusChanged(boolean isLoading, long bufferedPosition, int bufferedPercentage) {
    MyLog.w("onLoadingStatusChanged, isLoading: " + isLoading +
            "   Buffered Position: " + bufferedPosition +
            "   Buffered Percentage: " + bufferedPercentage);
}
@Override
public void onPlayerPlaying(int currentWindowIndex) {
    MyLog.d("onPlayerPlaying, currentWindowIndex: " + currentWindowIndex);
}
@Override
public void onPlayerPaused(int currentWindowIndex) {
    MyLog.d("onPlayerPaused, currentWindowIndex: " + currentWindowIndex);
}
@Override
public void onPlayerBuffering(int currentWindowIndex) {
    MyLog.d("onPlayerBuffering, currentWindowIndex: " + currentWindowIndex);
}
@Override
public void onPlayerStateEnded(int currentWindowIndex) {
    MyLog.d("onPlayerStateEnded, currentWindowIndex: " + currentWindowIndex);
}
@Override
public void onPlayerStateIdle(int currentWindowIndex) {
    MyLog.d("onPlayerStateIdle, currentWindowIndex: " + currentWindowIndex);
}
@Override
public void onPlayerError(String errorString) {
    MyLog.e("onPlayerError: " + errorString);
}
@Override
public void createExoPlayerCalled(boolean isToPrepare) {
    MyLog.d("createExoPlayerCalled, isToPrepare: " + isToPrepare);
}
@Override
public void releaseExoPlayerCalled() {
    MyLog.d("releaseExoPlayerCalled");
}
@Override
public void onVideoResumeDataLoaded(int window, long position, boolean isResumeWhenReady) {
    MyLog.d("window: " + window + "  position: " + position + " autoPlay: " + isResumeWhenReady);
}
@Override
public void onVideoTapped() {
    MyLog.d("onVideoTapped");
}
@Override
public boolean onPlayBtnTap() {
    MyLog.d("onPlayBtnTap");
    return false;
}
@Override
public boolean onPauseBtnTap() {
    MyLog.d("onPauseBtnTap");
    return false;
}
@Override
public void onTracksChanged(int currentWindowIndex, int nextWindowIndex, boolean isPlayBackStateReady) {
    MyLog.d("currentWindowIndex: " + currentWindowIndex + "  nextWindowIndex: " + nextWindowIndex + " isPlayBackStateReady: " + isPlayBackStateReady);
}
@Override
public void onMuteStateChanged(boolean isMuted) {
}
/**
 * ExoAdListener
 */
@Override
public void onAdPlay() {
    MyLog.d("onAdPlay");
}
@Override
public void onAdPause() {
    MyLog.d("onAdPause");
}
@Override
public void onAdResume() {
    MyLog.d("onAdResume");
}
@Override
public void onAdEnded() {
    MyLog.d("onAdEnded");
}
@Override
public void onAdError() {
    MyLog.d("onAdError");
}
@Override
public void onAdClicked() {
    MyLog.d("onAdClicked");
}
@Override
public void onAdTapped() {
    MyLog.d("onAdTapped");
}

```

 <br>  <br>  <br> 
# License

```
Copyright 2017 Alexey Korolev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
