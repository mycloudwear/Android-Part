package com.mycloudwear.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author jinwenfeng
 * @version 1.0.1
 * @since 14/5/2019
 * Published on 6/12/2016.
 * The original code was provided by jinwenfeng (https://github.com/823546371) but in our app we
 * only use part of his code to achieve our function.
 */
public class PullToRefreshLayout extends FrameLayout {

    private HeadView mHeaderView;
    private View mChildView;

    private static final long ANIM_TIME = 300;
    private static int HEAD_HEIGHT = 60;

    private static int head_height;
    private static int head_height_2;

    private float mTouchY;
    private float mCurrentY;

    private boolean canLoadMore = true;
    private boolean canRefresh = true;
    private boolean isRefresh;

    private int mTouchSlope;

    private BaseRefreshListener refreshListener;

    // Set a refresh listener.
    public void setRefreshListener(BaseRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    // Construction.
    public PullToRefreshLayout(Context context) {
        this(context, null);
    }

    // Construction.
    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Construction.
    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Set the height of the layout.
     */
    private void cal() {
        head_height = dp2Px(getContext(), HEAD_HEIGHT);
        head_height_2 = dp2Px(getContext(), HEAD_HEIGHT);

        mTouchSlope = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    /**
     * Initialization.
     */
    private void init() {
        cal();
        int count = getChildCount();
        if (count != 1) {
            new IllegalArgumentException("child only can be one");
        }

    }

    /**
     * Finish.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mChildView = getChildAt(0);
        addHeadView();
    }

    /**
     * Attach to the current window.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    /**
     * Add new head view.
     */
    private void addHeadView() {
        if (mHeaderView == null) {
            mHeaderView = new HeadRefreshView(getContext());
        }else{
            removeView(mHeaderView.getView());
        }
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        mHeaderView.getView().setLayoutParams(layoutParams);
        if (mHeaderView.getView().getParent() != null)
            ((ViewGroup) mHeaderView.getView().getParent()).removeAllViews();
        addView(mHeaderView.getView(), 0);
    }

    /**
     * Listen to motion event.
     * @param ev motion event.
     * @return motion result.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!canLoadMore && !canRefresh) return super.onInterceptTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchY = ev.getY();
                mCurrentY = mTouchY;
                break;
            case MotionEvent.ACTION_MOVE:
                float currentY = ev.getY();
                float dy = currentY - mCurrentY;
                if (canRefresh) {
                    boolean canChildScrollUp = canChildScrollUp();
                    if (dy > mTouchSlope && !canChildScrollUp) {
                        mHeaderView.begin();
                        return true;
                    }
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * Listen to motion event.
     * @param event motion event.
     * @return motion result.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRefresh) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mCurrentY = event.getY();
                float dura = (mCurrentY - mTouchY) / 3.0f;
                if (dura > 0 && canRefresh) {
                    dura = Math.min(head_height_2, dura);
                    dura = Math.max(0, dura);
                    mHeaderView.getView().getLayoutParams().height = (int) dura;
                    ViewCompat.setTranslationY(mChildView, dura);
                    requestLayout();
                    mHeaderView.progress(dura, head_height);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float currentY = event.getY();
                final int dy1 = (int) (currentY - mTouchY) / 3;
                if (dy1 > 0 && canRefresh) {
                    if (dy1 >= head_height) {
                        createAnimatorTranslationY(State.REFRESH,
                                dy1 > head_height_2 ? head_height_2 : dy1, head_height,
                                new CallBack() {
                                    @Override
                                    public void onSuccess() {
                                        isRefresh = true;
                                        if (refreshListener != null) {
                                            refreshListener.refresh();
                                        }
                                        mHeaderView.loading();
                                    }
                                });
                    } else if (dy1 > 0 && dy1 < head_height) {
                        setFinish(dy1, State.REFRESH);
                        mHeaderView.normal();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * This function allows window to be scrolled up.
     * @return the scroll result.
     */
    private boolean canChildScrollUp() {
        if (mChildView == null) {
            return false;
        }
        return ViewCompat.canScrollVertically(mChildView, -1);
    }

    /**
     * Create animation.
     */
    public void createAnimatorTranslationY(@State.REFRESH_STATE final int state, final int start,
                                           final int purpose, final CallBack callBack) {
        final ValueAnimator anim;
        anim = ValueAnimator.ofInt(start, purpose);
        anim.setDuration(ANIM_TIME);
        anim.addUpdateListener(valueAnimator -> {
            int value = (int) valueAnimator.getAnimatedValue();
            if (state == State.REFRESH) {
                mHeaderView.getView().getLayoutParams().height = value;
                ViewCompat.setTranslationY(mChildView, value);
                if (purpose == 0) { //代表结束加载
                    mHeaderView.finishing(value, head_height_2);
                } else {
                    mHeaderView.progress(value, head_height);
                }
            }
            if (value == purpose) {
                if (callBack != null)
                    callBack.onSuccess();
            }
            requestLayout();


        });
        anim.start();
    }

    /**
     * End with drop-down refresh
     */
    private void setFinish(int height, @State.REFRESH_STATE final int state) {
        createAnimatorTranslationY(state, height, 0, () -> {
            if (state == State.REFRESH) {
                isRefresh = false;
                mHeaderView.normal();

            }
        });
    }

    /**
     * Set state as finish.
     * @param state the animation state.
     */
    private void setFinish(@State.REFRESH_STATE int state) {
        if (state == State.REFRESH) {
            if (mHeaderView != null && mHeaderView.getView().getLayoutParams().height > 0 && isRefresh) {
                setFinish(head_height, state);
            }
        }
    }

    /**
     * Pull successfully.
     */
    public interface CallBack {
        void onSuccess();
    }

    /**
     * Finish before refresh.
     */
    public void finishRefresh() {
        setFinish(State.REFRESH);
    }

    /**
     * Translate dp to px.
     * @param context current context.
     * @param dp dp.
     * @return px.
     */
    public static int dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
