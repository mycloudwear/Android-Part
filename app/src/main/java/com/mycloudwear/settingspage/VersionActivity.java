package com.mycloudwear.settingspage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.documentpages.FreshActivity;
import com.mycloudwear.documentpages.PrivacyActivity;
import com.mycloudwear.documentpages.TermsActivity;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.library.ToastCenterText;
import com.mycloudwear.R;
import com.mycloudwear.mainpages.MyActivity;

import static com.mycloudwear.basicpage.Language.getLanguageLocal;
import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to display the version page, the user could find the updates of MyCloudwear.
 */
public class VersionActivity extends BasicActivity {

    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    private ToastCenterText text = new ToastCenterText();
    private String phoneNumber;
    private String language;

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the version page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_version);
        language = getLanguageLocal(this);

        // Get the phone number from previous activity.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("");
        ImageView back = findViewById(R.id.ib_title_back);
        back.setOnClickListener(v -> thisActivity.toNextActivityWithParameter(MyActivity.class, phoneNumber));
    }

    /**
     * This function could help user turn the terms of services.
     * @param view the current context.
     */
    public void gotoTerms(View view) {
        thisActivity.toNextActivityWithParameter(TermsActivity.class, language);
    }

    /**
     * This function could help user turn the privacy policy.
     * @param view the current context.
     */
    public void gotoPrivacy(View view) {
        thisActivity.toNextActivityWithParameter(PrivacyActivity.class, language);
    }

    /**
     * This function could help user turn what's new.
     * @param view the current context.
     */
    public void gotoFresh(View view) {
        thisActivity.toNextActivityWithParameter(FreshActivity.class, language);
    }
}
