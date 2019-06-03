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
 * This class could directly link to the document about how to delete a photo.
 */
public class DeletePhotoActivity extends BasicActivity {

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the web page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_delete_photo);

        // Get the phone number data from the another intent.
        Intent intent = this.getIntent();
        String language = intent.getStringExtra("phoneNumber");

        ImageView back = findViewById(R.id.ib_title_back);
        WebView fresh = findViewById(R.id.delete_photo);

        // Set the "back" button listener.
        back.setOnClickListener(v -> {
            this.finish();
            this.overridePendingTransition(0,0);
        });

        WebSettings settings = fresh.getSettings();
        // Set property of the WebView in order to execute Javascript scripts.
        settings.setJavaScriptEnabled(true);
        // Set property of the WebView in order to get file access permission.
        settings.setAllowFileAccess(true);
        // Set property of the WebView in order to support zoom.
        settings.setBuiltInZoomControls(true);
        // Load the appropriate page.
        if(language.equals("简体中文"))fresh.loadUrl("https://www.mycloudwear.com/WebLogin/docs/Gallery-Using-Guide-ZH.html");
        else fresh.loadUrl("https://www.mycloudwear.com/WebLogin/docs/Gallery-Using-Guide-EN.html");
    }
}
