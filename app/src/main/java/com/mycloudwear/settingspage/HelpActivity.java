package com.mycloudwear.settingspage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.documentpages.DeleteAccountActivity;
import com.mycloudwear.documentpages.DeletePhotoActivity;
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
 * This class is used to display the help and feedback page, the user could find the solutions to common problems.
 * If the user has any thing to complain, please feel free to write a email to us:)
 */
public class HelpActivity extends BasicActivity {

    private String phoneNumber;
    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    private ToastCenterText text = new ToastCenterText();

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the help and feedback page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_feedback);

        // Get the phone number from previous activity.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        ImageView back = findViewById(R.id.ib_title_back);

        back.setOnClickListener(v -> thisActivity.toNextActivityWithParameter(MyActivity.class, phoneNumber));
    }

    /**
     * This function could send the user to guide of how to delete a account.
     * @param view the current context.
     */
    public void deleteAccountGuide(View view) {
        String language = getLanguageLocal(this);
        thisActivity.toNextActivityWithParameter(DeleteAccountActivity.class, language);
    }

    /**
     * This function could send the user to guide of how to delete a photo.
     * @param view the current context.
     */
    public void deletePhotoGuide(View view) {
        String language = getLanguageLocal(this);
        thisActivity.toNextActivityWithParameter(DeletePhotoActivity.class, language);
    }
}
