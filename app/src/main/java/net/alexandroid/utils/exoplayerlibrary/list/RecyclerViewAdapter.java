package net.alexandroid.utils.exoplayerlibrary.list;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.squareup.picasso.Picasso;

import net.alexandroid.utils.exoplayerhelper.ExoAdListener;
import net.alexandroid.utils.exoplayerhelper.ExoPlayerHelper;
import net.alexandroid.utils.exoplayerhelper.ExoPlayerListener;
import net.alexandroid.utils.exoplayerhelper.ExoThumbListener;
import net.alexandroid.utils.exoplayerlibrary.R;
import net.alexandroid.utils.mylog.MyLog;

import java.util.ArrayList;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
        implements LifecycleObserver, View.OnClickListener {

    public static final String TEST_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";

    private final ArrayList<VideoItem> mList;
    private RecyclerView mRecyclerView;
    private boolean isFirstItemPlayed;
    private int currentSelected = 0;
    private SliderLayoutManager mLayoutManager;
    private boolean mIsFirstItemSelected;
    private boolean mIsInFullScreen;
    private int mPositionInFullScreen;
    private final Toolbar mToolbar;

    public RecyclerViewAdapter(ArrayList<VideoItem> pList, Toolbar toolbar) {
        mList = pList;
        mToolbar = toolbar;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        MyLog.e("onAttachedToRecyclerView");
        mRecyclerView = recyclerView;
        mLayoutManager = ((SliderLayoutManager) mRecyclerView.getLayoutManager());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                int top = mRecyclerView.getChildAt(0).getTop();
                int height = mRecyclerView.getChildAt(0).getHeight();

                //MyLog.d("firstVisible: " + firstVisible + "  top: " + top + "  height: " + height);

                if (top < height / 3 * (-1)) {
                    firstVisible++;
                }

                if (lastVisible == getItemCount() - 1) {
                    int lastViewTop = mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1).getBottom();
                    int listHeight = mRecyclerView.getHeight();

                    if (lastViewTop - listHeight < height / 4) {
                        firstVisible++;
                    }

/*                    MyLog.d("getChildCount: " + mRecyclerView.getChildCount()
                            + "  lastViewTop: " + lastViewTop
                            + "  listHeight: " + listHeight
                            + "  lastViewTop - listHeight: " + (lastViewTop - listHeight)
                    );*/
                }

                if (firstVisible != currentSelected) {
                    onSelectedItemChanged(firstVisible);
                }
            }
        });
    }


    private void onSelectedItemChanged(int newSelected) {
        MyLog.d("New first visible is: " + newSelected);

        changeAlphaToVisible(currentSelected, false);
        pausePlayerByPosition(currentSelected);
        blockPlayerByPosition(currentSelected);
        //---------
        changeAlphaToVisible(newSelected, true);
        prepareAndPlayByPosition(newSelected);
        unBlockPlayerByPosition(newSelected);

        currentSelected = newSelected;
    }

    private void unBlockPlayerByPosition(int newSelected) {
        ViewHolder viewHolder = getViewHolder(newSelected);
        if (viewHolder != null) {
            viewHolder.mExoPlayerHelper.playerUnBlock();
        }
    }

    private void prepareAndPlayByPosition(int position) {
        ExoPlayerHelper newPlayer = getExoPlayerByPosition(position);
        if (newPlayer != null) {
            newPlayer.preparePlayer();
            newPlayer.playerPlay();
        }
    }

    private void blockPlayerByPosition(int position) {
        ViewHolder viewHolder = getViewHolder(position);
        if (viewHolder != null) {
            viewHolder.mExoPlayerHelper.playerBlock();
        }
    }

    private void pausePlayerByPosition(int position) {
        ExoPlayerHelper oldPlayer = getExoPlayerByPosition(position);
        if (oldPlayer != null) {
            oldPlayer.playerPause();
        }
    }

    private void changeAlphaToVisible(int position, boolean isVisible) {
        ViewHolder viewHolder = getViewHolder(position);
        if (viewHolder != null) {
            viewHolder.mView.setAlpha(isVisible ? 1.0f : 0.2f);
        }
    }


    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        MyLog.e("onDetachedFromRecyclerView");
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
        return new ViewHolder(view, mLayoutManager);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mTextView.setText(mList.get(position).getVideoUrl());
        holder.mVideoUrl = mList.get(position).getVideoUrl();
        holder.mThumbUrl = mList.get(position).getThumbUrl();
        holder.mPosition = position;

        setClickListenerAndPositionAsTag(position, holder.mBtnFullScreen);

        if (!mIsFirstItemSelected) {
            mIsFirstItemSelected = true;
            holder.mView.setAlpha(1.0f);
        } else {
            holder.mView.setAlpha(0.2f);
        }
    }

    private void setClickListenerAndPositionAsTag(int position, View... views) {
        for (View view : views) {
            view.setOnClickListener(this);
            view.setTag(position);
        }
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();
        switch (v.getId()) {
            case R.id.btnFullScreen:
                onBtnFullScreen(position);
                break;
        }
    }

    private void onBtnFullScreen(int position) {
        ViewHolder holder = getViewHolder(position);

        if (mIsInFullScreen) {
            mIsInFullScreen = false;
            mPositionInFullScreen = 0;
            mToolbar.setVisibility(View.VISIBLE);
            holder.changeToNormalScreen();
        } else if (position == currentSelected) {
            mIsInFullScreen = true;
            mPositionInFullScreen = position;
            mToolbar.setVisibility(View.GONE);
            holder.changeToFullScreen();
        }
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        MyLog.i("Position: " + holder.mPosition + " - onViewAttachedToWindow");
        holder.createPlayer();

        if (!isFirstItemPlayed && holder.getAdapterPosition() == 0) {
            isFirstItemPlayed = true;
            holder.mExoPlayerHelper.preparePlayer();
            holder.mExoPlayerHelper.playerPlay();
            holder.mExoPlayerHelper.playerUnBlock();
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
    public class ViewHolder extends RecyclerView.ViewHolder implements ExoPlayerListener, ExoAdListener, ExoThumbListener {

        public final View mView;
        public final TextView mTextView;
        public final PlayerView mExoPlayerView;
        public final ImageView mBtnFullScreen;
        public ExoPlayerHelper mExoPlayerHelper;
        public String mThumbUrl;
        public String mVideoUrl;
        private int mPosition;

        private final SliderLayoutManager mLayouManager;
        private int mItemHeight;
        private int mItemWidth;
        private int mItemTopMargin;
        private int mItemBottomMargin;
        private int mVideoHeight;
        private int mVideoWidth;
        private int mItemLeftMargin;
        private int mItemRightMargin;

        public ViewHolder(View itemView, SliderLayoutManager layouManager) {
            super(itemView);
            mView = itemView;
            mLayouManager = layouManager;
            mTextView = mView.findViewById(R.id.textView);
            mExoPlayerView = mView.findViewById(R.id.exoPlayerView);
            mBtnFullScreen = mView.findViewById(R.id.btnFullScreen);
        }

        public void createPlayer() {
            mExoPlayerHelper = new ExoPlayerHelper.Builder(mExoPlayerView.getContext(), mExoPlayerView)
                    //.enableCache(50)
                    .setUiControllersVisibility(true)
                    .setAutoPlayOn(false)
                    .setToPrepareOnResume(false)
                    .setVideoUrls(mVideoUrl)
                    //.setTagUrl(TEST_TAG_URL)
                    .setExoPlayerEventsListener(this)
                    .setExoAdEventsListener(this)
                    .setThumbImageViewEnabled(this)
                    .addProgressBarWithColor(Color.RED)
                    .create();
        }

        public void changeToFullScreen() {
            mLayouManager.disableScroll();
            RecyclerView.LayoutParams layoutParamsItem = (RecyclerView.LayoutParams) mView.getLayoutParams();
            mItemHeight = layoutParamsItem.height;
            mItemWidth = layoutParamsItem.width;
            mItemTopMargin = layoutParamsItem.topMargin;
            mItemBottomMargin = layoutParamsItem.bottomMargin;
            mItemLeftMargin = layoutParamsItem.leftMargin;
            mItemRightMargin = layoutParamsItem.rightMargin;

            layoutParamsItem.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParamsItem.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParamsItem.topMargin = 0;
            layoutParamsItem.bottomMargin = 0;
            layoutParamsItem.leftMargin = 0;
            layoutParamsItem.rightMargin = 0;
            mView.setLayoutParams(layoutParamsItem);

            ConstraintLayout.LayoutParams videoParams = (ConstraintLayout.LayoutParams) mExoPlayerView.getLayoutParams();
            mVideoHeight = videoParams.height;
            mVideoWidth = videoParams.width;
            videoParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            videoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            //videoParams.bottomToBottom = R.id.parent;
            mExoPlayerView.setLayoutParams(videoParams);

            mExoPlayerView.post(new Runnable() {
                @Override
                public void run() {
                    mLayouManager.scrollToPosition(getAdapterPosition());
                }
            });

            mTextView.setVisibility(View.GONE);

            ((Activity) mTextView.getContext()).setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);

        }

        public void changeToNormalScreen() {
            ((Activity) mTextView.getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            mLayouManager.enableScroll();

            RecyclerView.LayoutParams layoutParamsItem = (RecyclerView.LayoutParams) mView.getLayoutParams();
            layoutParamsItem.height = mItemHeight;
            layoutParamsItem.width = mItemWidth;
            layoutParamsItem.topMargin = mItemTopMargin;
            layoutParamsItem.bottomMargin = mItemBottomMargin;
            layoutParamsItem.leftMargin = mItemLeftMargin;
            layoutParamsItem.rightMargin = mItemRightMargin;
            mView.setLayoutParams(layoutParamsItem);

            ConstraintLayout.LayoutParams videoParams = (ConstraintLayout.LayoutParams) mExoPlayerView.getLayoutParams();
            videoParams.height = mVideoHeight;
            videoParams.width = mVideoWidth;
            //videoParams.bottomToBottom = 0;
            mExoPlayerView.setLayoutParams(videoParams);

            mTextView.setVisibility(View.VISIBLE);
        }

        /**
         * ExoPlayerListener
         */

        @Override
        public void onThumbImageViewReady(ImageView imageView) {
            Picasso.get()
                    .load(mThumbUrl)
                    .placeholder(R.drawable.place_holder)
                    .error(R.drawable.error_image)
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
        public void onPlayerError(String errorString) {
            MyLog.e("Position: " + mPosition + " - onPlayerError");
        }

        @Override
        public void createExoPlayerCalled(boolean isToPrepare) {
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
        public boolean onPlayBtnTap() {
            MyLog.d("Position: " + mPosition + " - onPlayBtnTap");
            return false;
        }

        @Override
        public boolean onPauseBtnTap() {
            MyLog.d("Position: " + mPosition + " - onPauseBtnTap");
            return false;
        }

        @Override
        public void onFullScreenBtnTap() {

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

        ExoPlayerHelper newPlayer = getExoPlayerByPosition(currentSelected);
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

    public boolean onBack() {
        if (mIsInFullScreen) {
            onBtnFullScreen(mPositionInFullScreen);
            return true;
        } else {
            return false;
        }
    }
}
