package com.mycloudwear.library;

import android.graphics.Color;
import android.os.Build;
import android.support.v4.graphics.ColorUtils;
import android.view.View;
import android.view.Window;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class could is used to change the Android's status color.
 */
public class StatusBar {

    /**
     * This function is to make window transparent.
     * @param window
     */
    public static void changeStatusColor(Window window){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            int color = window.getStatusBarColor();
            if (isLightColor(color)) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
            window.setStatusBarColor(Color.TRANSPARENT);
        }

    }

    /**
     * This function is to check the color lightness.
     * @param color the color code.
     * @return check result.
     */
    public static boolean isLightColor(int color) {
        return ColorUtils.calculateLuminance(color) >= 0.1778;
    }
}
