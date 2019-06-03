package com.mycloudwear.settingspage;


import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;

import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.basicpage.Language;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.R;
import com.mycloudwear.mainpages.MyActivity;



import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to display the general page, the user could change TA's prderred language.
 */
public class GeneralActivity extends BasicActivity{

    private String phoneNumber;

    private GoToNextActivity thisActivity = new GoToNextActivity(this);

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the general page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_general);

        // Get the phone number from previous activity.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        ImageView back = findViewById(R.id.ib_title_back);
        back.setOnClickListener(v -> thisActivity.toNextActivityWithParameter(MyActivity.class, phoneNumber));
    }

    /**
     * This function could set the preferred language and update the current page and other pages.
     * @param view the current view.
     */
    public void setLanguage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String[] locals = {"English", "简体中文"};
        builder.setItems(locals, (dialog, which) -> {
            Language.setLanguageLocal(GeneralActivity.this, locals[which]);
            recreate();
        });

        builder.create().show();

    }
}
