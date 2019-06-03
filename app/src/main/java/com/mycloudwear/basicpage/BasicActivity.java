package com.mycloudwear.basicpage;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is the basic class to find the current system language to
 * achieve localization.
 */
public class BasicActivity extends AppCompatActivity {
    /**
     * This method implements the super method.
     * The main purpose of this method is to apply system language.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeAppLanguage();
    }

    /**
     * This function firstly find the current system language.
     * And then set the language of the MyCloudwear as the system language.
     */
    public void changeAppLanguage() {
        String language = Language.getLanguageLocal(this);
        if(language != null){
            // Find the language preference.
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            /*
             * If the detected language is "English" then set the language of the MyCloudwear.
             * Else set the language to simplified chinese.
             */
            switch (language){
            case "English":
                conf.setLocale(Locale.ENGLISH);
                break;
            case "简体中文":
                conf.setLocale(Locale.SIMPLIFIED_CHINESE);
                break;
            default:
                conf.setLocale(Locale.ENGLISH);
                break;
        }
            res.updateConfiguration(conf, dm);
        }

    }

}
