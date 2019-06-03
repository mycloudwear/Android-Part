package com.mycloudwear.library;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;
import android.widget.TextView;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to make the toast on the center of the screen.
 */
public class ToastCenterText{

    private TextView v;
    private Toast toast;

    // Construction.
    public ToastCenterText() {
    }

    /**
     * This function displays the toast on the center of the screen.
     * @param context the current context.
     * @param hint the text.
     */
    public void displayToast(Context context, String hint){
        toast = Toast.makeText(context,hint,Toast.LENGTH_SHORT);
        v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setGravity(Gravity.CENTER);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

}
