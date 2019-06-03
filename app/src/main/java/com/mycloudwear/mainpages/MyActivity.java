package com.mycloudwear.mainpages;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.library.ToastCenterText;
import com.mycloudwear.login.LoginActivity;
import com.mycloudwear.R;
import com.mycloudwear.settingspage.AccountActivity;
import com.mycloudwear.settingspage.GeneralActivity;
import com.mycloudwear.settingspage.HelpActivity;
import com.mycloudwear.settingspage.ProfileActivity;
import com.mycloudwear.settingspage.VersionActivity;

import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to display the settings page, the user could use following services:
 * 1. Personal information.
 * 2. Profile.
 * 3. General.
 * 4. Help and feedback.
 * 5. About.
 * 6. Log out.
 */
public class MyActivity extends BasicActivity {

    private ImageView btn_me;
    private TextView text_me;
    private String phoneNumber;
    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    private ToastCenterText text = new ToastCenterText();

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the settings page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_settings);
        btn_me = findViewById(R.id.img_me);
        text_me = findViewById(R.id.text_me);
        btn_me.setSelected(true);
        text_me.setSelected(true);

        // Get the phone number from previous activity.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
    }

    /**
     * This function rewrites the android's back button to achieve "go to home" function.
     */
    @Override
    public void onBackPressed() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);

    }

    /**
     * This function is used to respond the "go to homapge" request.
     * @param view the current context.
     */
    public void clickHome(View view) {
        btn_me.setSelected(false);
        text_me.setSelected(false);
        thisActivity.toNextActivityWithParameter(HomepageActivity.class, phoneNumber);
        this.overridePendingTransition(0, 0);
    }

    /**
     * This function is used to respond the "go to photo management" request.
     * @param view the current context.
     */
    public void clickPhotos(View view) {
        btn_me.setSelected(false);
        text_me.setSelected(false);
        thisActivity.toNextActivityWithParameter(PhotoActivity.class, phoneNumber);
        this.overridePendingTransition(0, 0);
    }

    /**
     * This function is used to help the user log out.
     * @param view the current context.
     */
    public void logOut(View view){
        text.displayToast(this,getString(R.string.logout_successfully));
        thisActivity.toNextActivityWithoutParameter(LoginActivity.class);
        finish();
    }

    /**
     * This function is used to respond the "go to personal information" request.
     * @param view the current context.
     */
    public void gotoAccountInfo(View view) {
        thisActivity.toNextActivityWithParameter(AccountActivity.class, phoneNumber);
    }

    /**
     * This function is used to respond the "go to profile" request.
     * @param view the current context.
     */
    public void gotoProfile(View view) {
        thisActivity.toNextActivityWithParameter(ProfileActivity.class, phoneNumber);
    }

    /**
     * This function is used to respond the "go to general" request.
     * @param view the current context.
     */
    public void gotoGeneral(View view) {
        thisActivity.toNextActivityWithParameter(GeneralActivity.class, phoneNumber);
    }

    /**
     * This function is used to respond the "go to help center" request.
     * @param view the current context.
     */
    public void gotoHelp(View view) {
        thisActivity.toNextActivityWithParameter(HelpActivity.class, phoneNumber);
    }

    /**
     * This function is used to respond the "go to version" request.
     * @param view the current context.
     */
    public void gotoVersion(View view) {
        thisActivity.toNextActivityWithParameter(VersionActivity.class, phoneNumber);
    }
}
