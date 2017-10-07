package net.alexandroid.utils.exoplayerlibrary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

public class MainActivity extends AppCompatActivity {
    public static final String VIDEO_URL = "http://ynethd-i.akamaihd.net/i/cdnwiz/0917/190917_maya_dagan_laisha_Qnw9LprZ_800.mp4/master.m3u8";
    public static final String PPLUS_TAG_URL = "https://pubads.g.doubleclick.net/gampad/live/ads?sz=640x480&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&url=&correlator=&iu=/6870/ynet_mobile/pplus.preroll.apps&description_url=http://pplus.ynet.co.il/articles/0,7340,L-5020780,00.html&cust_params=dcPath%3D11211.PnaiPlus%26yncd%3D5020780%26videoPosition%3DPreroll%26autoplay%3Dtrue%26videoPosition_autoplay%3DPreroll_false%26AppVersion%3D1.0%26View%3D1";
    public static final String TEST_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";


    private SimpleExoPlayerView mExoPlayerView;
    private ExoPlayer mExoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mExoPlayerView = findViewById(R.id.exoPlayerView);

        mExoPlayer = new ExoPlayer.Builder(this)
                .setExoPlayerView(mExoPlayerView)
                .addMuteButton(false, true)
                .setUiControllersVisibility(true)
                .setRepeatModeOn(true)
                .setAutoPlayOn(true)
                .setVideoUrls(VIDEO_URL)
                .setTagUrl(TEST_TAG_URL)
                .build();
    }



}
