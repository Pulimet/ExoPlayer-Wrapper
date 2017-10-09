package net.alexandroid.utils.exoplayerlibrary.list;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.squareup.picasso.Picasso;

import net.alexandroid.shpref.MyLog;
import net.alexandroid.utils.exoplayerhelper.ExoAdListener;
import net.alexandroid.utils.exoplayerhelper.ExoPlayerHelper;
import net.alexandroid.utils.exoplayerhelper.ExoPlayerListener;
import net.alexandroid.utils.exoplayerlibrary.R;

import java.util.ArrayList;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements LifecycleObserver {

    public static final String TEST_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";

    private final ArrayList<VideoItem> mList;
    private RecyclerView mRecyclerView;
    private int currentFirstVisible;

    private boolean isFirstItemPlayed;

    public RecyclerViewAdapter(ArrayList<VideoItem> pList) {
        mList = pList;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        MyLog.e("onAttachedToRecyclerView");
        mRecyclerView = recyclerView;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisible = ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findFirstCompletelyVisibleItemPosition();
                if (firstVisible != currentFirstVisible) {
                    onFirstCompleteVisibleItemChange(firstVisible);
                }
            }
        });
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        MyLog.e("onDetachedFromRecyclerView");
    }

    private void onFirstCompleteVisibleItemChange(int firstVisible) {
        MyLog.d("New first visible is: " + firstVisible);
        ExoPlayerHelper oldPlayer = getExoPlayerByPosition(currentFirstVisible);
        if (oldPlayer != null) {
            oldPlayer.playerPause();
        }

        ExoPlayerHelper newPlayer = getExoPlayerByPosition(firstVisible);
        if (newPlayer != null) {
            newPlayer.preparePlayer();
            newPlayer.playerPlay();
        }

        currentFirstVisible = firstVisible;
    }

    private ExoPlayerHelper getExoPlayerByPosition(int firstVisible) {
        ViewHolder holder = getViewHolder(firstVisible);
        if (holder != null) {
            return getViewHolder(firstVisible).mExoPlayerHelper;
        } else {
            return null;
        }
    }

    private RecyclerViewAdapter.ViewHolder getViewHolder(int position) {
        return (RecyclerViewAdapter.ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
    }

    private ArrayList<ExoPlayerHelper> getAllExoPlayers() {
        ArrayList<ExoPlayerHelper> list = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            ExoPlayerHelper exoPlayerHelper = getExoPlayerByPosition(i);
            if (exoPlayerHelper != null) {
                list.add(exoPlayerHelper);
            }
        }
        return list;
    }


    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mTextView.setText(mList.get(position).getVideoUrl());
        holder.mVideoUrl = mList.get(position).getVideoUrl();
        holder.mThumbUrl = mList.get(position).getThumbUrl();
        holder.mPosition = position;
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        MyLog.i("Position: " + holder.mPosition + " - onViewAttachedToWindow");
        holder.createPlayer();

        if (!isFirstItemPlayed && holder.mPosition == 0) {
            isFirstItemPlayed = true;
            holder.mExoPlayerHelper.preparePlayer();
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        MyLog.i("Position: " + holder.mPosition + " - onViewDetachedFromWindow");
        holder.mExoPlayerHelper.releasePlayer();
        holder.mExoPlayerHelper.onActivityDestroy();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    @SuppressWarnings("WeakerAccess")
    public class ViewHolder extends RecyclerView.ViewHolder implements ExoPlayerListener, ExoAdListener {

        public final View mView;
        public final TextView mTextView;
        public final SimpleExoPlayerView mExoPlayerView;
        public ExoPlayerHelper mExoPlayerHelper;
        public String mThumbUrl;
        public String mVideoUrl;
        private int mPosition;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mTextView = mView.findViewById(R.id.textView);
            mExoPlayerView = mView.findViewById(R.id.exoPlayerView);
        }

        public void createPlayer() {
            mExoPlayerHelper = new ExoPlayerHelper.Builder(mExoPlayerView.getContext(), mExoPlayerView)
                    .setUiControllersVisibility(true)
                    .setAutoPlayOn(false)
                    .setToPrepareOnResume(false)
                    .setVideoUrls(mVideoUrl)
                    .setTagUrl(TEST_TAG_URL)
                    .setExoPlayerEventsListener(this)
                    .setExoAdEventsListener(this)
                    .setThumbImageViewEnabled()
                    .create();
        }

        /**
         * ExoPlayerListener
         */

        @Override
        public void onThumbImageViewReady(ImageView imageView) {
            Picasso.with(mView.getContext())
                    .load(mThumbUrl)
                    .placeholder(R.drawable.no_image_wide)
                    .error(R.drawable.red_y_logo)
                    .into(imageView);
        }


        @Override
        public void onLoadingStatusChanged(boolean isLoading, long bufferedPosition, int bufferedPercentage) {
/*
        MyLog.d("onLoadingStatusChanged, isLoading: " + isLoading +
                "   Buffered Position: " + bufferedPosition +
                "   Buffered Percentage: " + bufferedPercentage);
*/
        }

        @Override
        public void onPlayerPlaying(int currentWindowIndex) {
            MyLog.d("Position: " + mPosition + " - onPlayerPlaying, currentWindowIndex: " + currentWindowIndex);
        }

        @Override
        public void onPlayerPaused(int currentWindowIndex) {
            MyLog.d("Position: " + mPosition + " - onPlayerPaused, currentWindowIndex: " + currentWindowIndex);
        }

        @Override
        public void onPlayerBuffering(int currentWindowIndex) {
            MyLog.d("Position: " + mPosition + " - onPlayerBuffering, currentWindowIndex: " + currentWindowIndex);
        }

        @Override
        public void onPlayerStateEnded(int currentWindowIndex) {
            MyLog.d("Position: " + mPosition + " - onPlayerStateEnded, currentWindowIndex: " + currentWindowIndex);
        }

        @Override
        public void onPlayerStateIdle(int currentWindowIndex) {
            MyLog.d("Position: " + mPosition + " - onPlayerStateIdle, currentWindowIndex: " + currentWindowIndex);
        }

        @Override
        public void onPlayerError() {
            MyLog.e("Position: " + mPosition + " - onPlayerError");
        }

        @Override
        public void createExoPlayerCalled() {
            MyLog.d("Position: " + mPosition + " - createExoPlayerCalled");
        }

        @Override
        public void releaseExoPlayerCalled() {
            MyLog.d("Position: " + mPosition + " - releaseExoPlayerCalled");
        }

        @Override
        public void onVideoResumeDataLoaded(int window, long position, boolean isResumeWhenReady) {
            MyLog.d("Position: " + mPosition + " - window: " + window + "  position: " + position + " autoPlay: " + isResumeWhenReady);
        }

        @Override
        public void onVideoTapped() {
            MyLog.d("Position: " + mPosition + " - onVideoTapped");
        }

        @Override
        public void onTracksChanged(int currentWindowIndex, int nextWindowIndex, boolean isPlayBackStateReady) {
            MyLog.d("currentWindowIndex: " + currentWindowIndex + "  nextWindowIndex: " + nextWindowIndex + " isPlayBackStateReady: " + isPlayBackStateReady);
        }
        /**
         * ExoAdListener
         */
        @Override
        public void onAdPlay() {
            MyLog.d("Position: " + mPosition + " - onAdPlay");
        }

        @Override
        public void onAdPause() {
            MyLog.d("Position: " + mPosition + " - onAdPause");
        }

        @Override
        public void onAdResume() {
            MyLog.d("Position: " + mPosition + " - onAdResume");
        }

        @Override
        public void onAdEnded() {
            MyLog.d("Position: " + mPosition + " - onAdEnded");
        }

        @Override
        public void onAdError() {
            MyLog.d("Position: " + mPosition + " - onAdError");
        }

        @Override
        public void onAdLoadError() {
            MyLog.d("Position: " + mPosition + " - onAdLoadError");
        }

        @Override
        public void onAdClicked() {
            MyLog.d("Position: " + mPosition + " - onAdClicked");
        }

        @Override
        public void onAdTapped() {
            MyLog.d("Position: " + mPosition + " - onAdTapped");
        }
    }


    // Activity LifeCycle

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onStart() {
        MyLog.i("onActivityStart");
        for (ExoPlayerHelper exoPlayerHelper : getAllExoPlayers()) {
            exoPlayerHelper.onActivityStart();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void onResume() {
        MyLog.i("onActivityResume");
        for (ExoPlayerHelper exoPlayerHelper : getAllExoPlayers()) {
            exoPlayerHelper.onActivityResume();
        }

        ExoPlayerHelper newPlayer = getExoPlayerByPosition(currentFirstVisible);
        if (newPlayer != null) {
            newPlayer.preparePlayer();
            newPlayer.playerPlay();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void onPause() {
        MyLog.i("onActivityPause");
        for (ExoPlayerHelper exoPlayerHelper : getAllExoPlayers()) {
            exoPlayerHelper.onActivityPause();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected void onStop() {
        MyLog.i("onActivityStop");
        for (ExoPlayerHelper exoPlayerHelper : getAllExoPlayers()) {
            exoPlayerHelper.onActivityStop();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void onDestroy() {
        MyLog.e("onActivityDestroy");
        for (ExoPlayerHelper exoPlayerHelper : getAllExoPlayers()) {
            exoPlayerHelper.releaseAdsLoader();
        }
    }
}
