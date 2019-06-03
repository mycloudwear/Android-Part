package com.mycloudwear.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.mycloudwear.R;
import com.mycloudwear.basicpage.BasicActivity;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used for splash.
 * When user open the app at first time, our app could show our logo and copyright.
 */
public class Splash extends BasicActivity {

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the splash.
     * The duration is 3 seconds.
     * @param savedInstanceState current state of the instance.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splash.this, LoginActivity.class));
            finish();
        }, 3000);
    }
}
