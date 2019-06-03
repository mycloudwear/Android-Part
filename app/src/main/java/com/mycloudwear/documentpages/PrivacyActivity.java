package com.mycloudwear.documentpages;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.R;

import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class could directly link to the document about the privacy policy.
 */
public class PrivacyActivity extends BasicActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_privacy);

        // Get the phone number data from the another intent.
        Intent intent = this.getIntent();
        String language = intent.getStringExtra("phoneNumber");

        ImageView back = findViewById(R.id.ib_title_back);
        WebView privacy = findViewById(R.id.privacy);

        // Set the "back" button listener.
        back.setOnClickListener(v ->{
            this.finish();
            this.overridePendingTransition(0,0);
        });

        WebSettings settings = privacy.getSettings();
        // Set property of the WebView in order to execute Javascript scripts.
        settings.setJavaScriptEnabled(true);
        // Set property of the WebView in order to get file access permission.
        settings.setAllowFileAccess(true);
        // Set property of the WebView in order to support zoom.
        settings.setBuiltInZoomControls(true);
        // Load the appropriate page.
        if(language.equals("简体中文"))privacy.loadUrl("https://www.mycloudwear.com/WebLogin/docs/Privacy-ZH.html");
        else privacy.loadUrl("https://www.mycloudwear.com/WebLogin/docs/Privacy-EN.html");
    }
}