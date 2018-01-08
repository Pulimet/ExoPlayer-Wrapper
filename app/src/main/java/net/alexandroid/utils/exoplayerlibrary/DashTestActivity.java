package net.alexandroid.utils.exoplayerlibrary;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class DashTestActivity extends AppCompatActivity {

    public static final String DASH_SAMPLE = "http://www.bok.net/dash/tears_of_steel/cleartext/stream.mpd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_test);

        SimpleExoPlayerView exoPlayerView = findViewById(R.id.exoPlayerView);

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        // Produces DataSource instances through which media data is loaded.
        DefaultDataSourceFactory mDataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter);

        // TrackSelector that selects tracks provided by the MediaSource to be consumed by each of the available Renderer's.
        // A TrackSelector is injected when the exoPlayer is created.
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this,
                null,  DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);

        SimpleExoPlayer exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
        exoPlayerView.setPlayer(exoPlayer);

        Uri uri = Uri.parse(DASH_SAMPLE);

        DashMediaSource dashMediaSource = new DashMediaSource(uri, mDataSourceFactory,
                new DefaultDashChunkSource.Factory(mDataSourceFactory), null, null);

        exoPlayer.prepare(dashMediaSource);
    }
}
