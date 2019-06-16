package net.alexandroid.utils.exoplayerlibrary.list;

import android.content.Context;
import android.graphics.PointF;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;

public class SliderLayoutManager extends LinearLayoutManager {
    private boolean mScrollEnabled = true;
    private RecyclerView mRecyclerView;

    public SliderLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        mRecyclerView = view;
        super.onAttachedToWindow(view);
    }

    @Override
    public boolean canScrollVertically() {
        return mScrollEnabled && super.canScrollVertically();
    }

    public void disableScroll() {
        mScrollEnabled = false;
    }

    public void enableScroll() {
        mScrollEnabled = true;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, final int positionToScrollTo) {

        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            static final int SNAP_TO_CENTER = 1234;
            static final float MILLISECONDS_PER_INCH_FAST = 25f;
            static final float MILLISECONDS_PER_INCH_SLOW = 150f; //default is 25f (bigger = slower)

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return SliderLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                if (shouldScrollFast) {
                    return MILLISECONDS_PER_INCH_FAST / displayMetrics.densityDpi;
                }
                return MILLISECONDS_PER_INCH_SLOW / displayMetrics.densityDpi;
            }

            @Override
            protected int getVerticalSnapPreference() {
                final boolean shouldScrollToFirstItem = positionToScrollTo == 0;
                if (shouldScrollToFirstItem) {
                    return SNAP_TO_START;
                }
                return SNAP_TO_CENTER;
            }


            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int
                    snapPreference) {
                if (snapPreference == SNAP_TO_CENTER) {
                    return getDtToFitCenter(viewStart, viewEnd, boxEnd);
                } else
                    return super.calculateDtToFit(viewStart, viewEnd, boxStart, boxEnd, snapPreference);
            }

            private int getDtToFitCenter(int viewStart, int viewEnd, int boxEnd) {
                // this logic doesnt work well on first item.
                int viewSize = viewEnd - viewStart;
                return boxEnd - viewEnd - (viewSize / 2);
            }
        };

        linearSmoothScroller.setTargetPosition(positionToScrollTo);
        startSmoothScroll(linearSmoothScroller);
    }

    boolean shouldScrollFast;

    public void fastSmoothScroll(int position) {
        shouldScrollFast = true;
        mRecyclerView.smoothScrollToPosition(position);
    }

    public void slowSmoothScroll(int position) {
        shouldScrollFast = false;
        mRecyclerView.smoothScrollToPosition(position);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }
}
