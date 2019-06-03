package com.mycloudwear.library;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author jinwenfeng
 * @version 1.0.1
 * @since 14/5/2019
 * Published on 6/12/2016.
 * The original code was provided by jinwenfeng (https://github.com/823546371) but in our app we
 * only use part of his code to achieve our function.
 */
public class State {


    @IntDef({REFRESH, LOADMORE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface REFRESH_STATE {

    }

    public static final int REFRESH = 10;
    public static final int LOADMORE = 11;
}
