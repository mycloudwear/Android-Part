package com.mycloudwear.library;

import android.view.View;

/**
 * @author jinwenfeng
 * @version 1.0.1
 * @since 14/5/2019
 * Published on 6/12/2016.
 * The original code was provided by jinwenfeng (https://github.com/823546371) but in our app we
 * only use part of his code to achieve our function.
 */
public interface HeadView {

    /**
     * Start the drop-down
     */
    void begin();

    /**
     * This function is used when it in progress.
     * @param progress current height.
     * @param all      total height.
     */
    void progress(float progress, float all);

    /**
     * This function is used when it is finished.
     * @param progress current height.
     * @param all total height.
     */
    void finishing(float progress, float all);

    /**
     * The drop-down to complete
     */
    void loading();

    /**
     * The invisible state
     */
    void normal();

    /**
     * Return to current view
     */
    View getView();

}
