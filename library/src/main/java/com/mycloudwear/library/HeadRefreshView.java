package com.mycloudwear.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author jinwenfeng
 * @version 1.0.1
 * @since 14/5/2019
 * Published on 6/12/2016.
 * The original code was provided by jinwenfeng (https://github.com/823546371) but in our app we
 * only use part of his code to achieve our function.
 */
public class HeadRefreshView extends FrameLayout implements HeadView {

    private TextView tv;
    private ImageView arrow;
    private ProgressBar progressBar;

    // Construction.
    public HeadRefreshView(Context context) {
        this(context,null);
    }

    // Construction.
    public HeadRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    // Construction.
    public HeadRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * This function could help user initialize the view.
     * @param context current context.
     */
    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_header,null);
        addView(view);
        tv = (TextView) view.findViewById(R.id.header_tv);
        arrow = (ImageView) view.findViewById(R.id.header_arrow);
        progressBar = (ProgressBar) view.findViewById(R.id.header_progress);
    }

    /**
     * As view is opened.
     */
    @Override
    public void begin() {

    }

    /**
     * As view still in progress.
     * @param progress current height.
     * @param all      total height.
     */
    @Override
    public void progress(float progress, float all) {
        float s = progress / all;
        if (s >= 0.9f){
            arrow.setRotation(180);
        }else{
            arrow.setRotation(0);
        }
        if (progress >= all-10){
            tv.setText(getContext().getString(R.string.release_refresh));
        }else{
            tv.setText(getContext().getString(R.string.pull_down_loading));
        }
    }

    /**
     * As view is finished.
     */
    @Override
    public void finishing(float progress, float all) {

    }

    /**
     * As view is loading.
     */
    @Override
    public void loading() {
        arrow.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        tv.setText(getContext().getString(R.string.refreshing));
    }

    /**
     * Set the view as normal.
     */
    @Override
    public void normal() {
        arrow.setVisibility(VISIBLE);
        progressBar.setVisibility(GONE);
        tv.setText(getContext().getString(R.string.pull_down_to_refresh));
    }

    /**
     * Get the current view.
     * @return
     */
    @Override
    public View getView() {
        return this;
    }
}
