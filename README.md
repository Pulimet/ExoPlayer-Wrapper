[ ![Download](https://api.bintray.com/packages/pulimet/utils/exoplayerhelper/images/download.svg) ](https://bintray.com/pulimet/utils/exoplayerhelper/_latestVersion)      [![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)  

<img align="right" width="200" src="https://raw.githubusercontent.com/Pulimet/ExoPlayer-Wrapper/master/art/1.jpg">


# ExoPlayer-Wrapper

Simpe ExoPlayer wrapper

# Installation

- Add the dependency from jCenter to your app's (not project) build.gradle file:

```sh
repositories {
    jcenter()
}

dependencies {
    compile 'net.alexandroid.utils:exoplayerhelper:1.98'
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
        
    public static final String SAMPLE_1 = "http://cdn-fms.rbs.com.br/vod/hls_sample1_manifest.m3u8";
    public static final String TEST_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
    private SimpleExoPlayerView mExoPlayerView;
    private ExoPlayerHelper mExoPlayerHelper;
```
...

```java
 
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mExoPlayerView = findViewById(R.id.exoPlayerView);

    mExoPlayerHelper = new ExoPlayerHelper.Builder(this, mExoPlayerView)
            .enableCache(10)
            .addMuteButton(false, true)
            .setUiControllersVisibility(true)
            .setRepeatModeOn(true)
            .setAutoPlayOn(true)
            .setVideoUrls(SAMPLE_1)
             .setTagUrl(TEST_TAG_URL)
            .setExoPlayerEventsListener(this)
            .setExoAdEventsListener(this)      
            .setThumbImageViewEnabled()
            .createAndPrepare();
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
