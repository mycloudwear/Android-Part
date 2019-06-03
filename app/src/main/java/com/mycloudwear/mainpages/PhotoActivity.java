package com.mycloudwear.mainpages;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.R;

import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to display the photo management page, the user could use following services:
 * 1. Top.
 * 2. Pant.
 * 3. Skirt.
 */
public class PhotoActivity extends BasicActivity {

    private ImageView btn_photo;
    private TextView text_photo;
    private String phoneNumber;
    private GoToNextActivity thisActivity = new GoToNextActivity(this);

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the photo management page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_photopage);

        btn_photo = findViewById(R.id.img_photo);
        text_photo = findViewById(R.id.text_photo);

        btn_photo.setSelected(true);
        text_photo.setSelected(true);

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
        btn_photo.setSelected(false);
        text_photo.setSelected(false);
        thisActivity.toNextActivityWithParameter(HomepageActivity.class, phoneNumber);
        this.overridePendingTransition(0, 0);
    }

    /**
     * This function is used to respond the "go to settings" request.
     * @param view the current context.
     */
    public void clickMe(View view) {
        btn_photo.setSelected(false);
        text_photo.setSelected(false);
        thisActivity.toNextActivityWithParameter(MyActivity.class, phoneNumber);
        this.overridePendingTransition(0, 0);
    }

    /**
     * This function could help the user turn to top page.
     * @param view the current context.
     */
    public void gotoTop(View view) {
        thisActivity.toNextActivityWithParameter(TopActivity.class, phoneNumber);
    }

    /**
     * This function could help the user turn to pant page.
     * @param view the current context.
     */
    public void gotoPant(View view) {
        thisActivity.toNextActivityWithParameter(PantActivity.class, phoneNumber);
    }

    /**
     * This function could help the user turn to skirt page.
     * @param view the current context.
     */
    public void gotoSkirt(View view) {
        thisActivity.toNextActivityWithParameter(SkirtActivity.class, phoneNumber);
    }
}
