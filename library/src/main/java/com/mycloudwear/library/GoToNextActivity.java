package com.mycloudwear.library;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;
import java.util.List;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to achieve from one intent to another intent.
 */
public class GoToNextActivity {

    private Context context;

    // Construction.
    public GoToNextActivity(Context context){
        this.context = context;
    }

    /**
     * This function is used to start another activity without any parameters.
     * @param cls
     */
    public void toNextActivityWithoutParameter(Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        context.startActivity(intent);
    }

    /**
     * This function is used to start another activity with user phone number.
     * @param cls the current activity's class.
     * @param phoneNum the user phone number.
     */
    public void toNextActivityWithParameter(Class<?> cls, String phoneNum) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra("phoneNumber", phoneNum);
        context.startActivity(intent);
    }

    /**
     * This function is used to start another activity with lists.
     * @param cls the current activity's class.
     * @param phoneNum the user phone number.
     * @param photoPaths the paths of the photos.
     * @param fileName the file name of each photo.
     */
    public void toNextActivityWithList(Class<?> cls, String phoneNum, List<?> photoPaths, List<?> fileName) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra("phoneNumber", phoneNum);
        intent.putExtra("photo",(Serializable) photoPaths);
        intent.putExtra("name",(Serializable) fileName);
        context.startActivity(intent);
    }

    /**
     * This function is used to start another activity with lists and label as well.
     * @param cls the current activity's class.
     * @param phoneNum the user phone number.
     * @param photoPaths the paths of the photos.
     * @param fileName the file name of each photo.
     * @param label label name (top, pant or skirt).
     */
    public void toNextActivityWithList(Class<?> cls, String phoneNum, List<?> photoPaths, List<?> fileName, String label) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra("phoneNumber", phoneNum);
        intent.putExtra("photo",(Serializable) photoPaths);
        intent.putExtra("name",(Serializable) fileName);
        intent.putExtra("label",label);
        context.startActivity(intent);
    }
}
