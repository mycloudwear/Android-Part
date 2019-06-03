package com.mycloudwear.basicpage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class could store the current system language preference.
 */
public class Language {

    /**
     * This function could update the language preference once it is invoked.
     * @param context the current context.
     * @param language the current system language.
     */
    public static void setLanguageLocal(Context context, String language){
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        editor.putString("language", language);
        editor.commit();
    }

    /**
     * This function could retrieve the language preference once it is invoked.
     * @param context the current context.
     * @return the preferred language.
     */
    public static String getLanguageLocal(Context context){
        SharedPreferences preferences;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String language = preferences.getString("language", "");
        return language;
    }
}
