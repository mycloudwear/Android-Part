package com.mycloudwear.settingspage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.library.Encoder;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.library.StreamUtils;
import com.mycloudwear.library.ToastCenterText;
import com.mycloudwear.login.LoginActivity;
import com.mycloudwear.R;
import com.mycloudwear.mainpages.MyActivity;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to display the personal information page, the user could use following services:
 * 1. Check TA's MyCloudwear id.
 * 2. Reset password.
 * 3. Change user's photo.
 * 4. Change user's nick name.
 * 4. Delete the account.
 */
public class AccountActivity extends BasicActivity {

    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_ERROR = -1;
    private String phoneNumber;
    private TextView name;
    private SharedPreferences config;
    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    private ToastCenterText text = new ToastCenterText();

    /*
     * This handler could handle the delete event.
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATUS_SUCCESS:
                    text.displayToast(AccountActivity.this,"Your account has been deleted successfully!");
                    thisActivity.toNextActivityWithoutParameter(LoginActivity.class);
                    break;
                case STATUS_ERROR:
                    text.displayToast(AccountActivity.this,"Network Error!\n"+"Try again!");
                    break;
                default:
                    break;
            }
        }
    };

    /*
     * This handler could write the user name from the SharedPreferences file.
     */
    private Handler nameHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATUS_SUCCESS:
                    // Need to save the data to the sp file.
                    SharedPreferences.Editor editor = config.edit();
                    editor.putString("name", msg.obj.toString());
                    editor.apply();
                    name.setText(msg.obj.toString());
                    break;
                case STATUS_ERROR:
                    text.displayToast(AccountActivity.this,"Network Error!\n"+"Try again!");
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the personal information page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.account_security);

        ImageView back = findViewById(R.id.ib_title_back);
        back.setOnClickListener(v -> thisActivity.toNextActivityWithParameter(MyActivity.class, phoneNumber));

        // Get the phone number from previous activity.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        // Display the phone number on the page.
        TextView showPhone = findViewById(R.id.show_id);
        showPhone.setText(phoneNumber);

        name = findViewById(R.id.show_name);
        restoreInfo();
    }

    /**
     * This method will retrieve the user name once the page has been created.
     */
    private void restoreInfo() {
        config = this.getSharedPreferences("config", this.MODE_PRIVATE);
        String user = config.getString("name", "");
        if (user.equals("")) {
            try{

                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Encoder encode = new Encoder();
                            String encryptedPhone = encode.Base64Encode(phoneNumber);
                            String urlPath = "http://192.168.0.67:8080/WebLogin/NameChecker?phone="
                                    + encryptedPhone;
                            URL url = new URL(urlPath);
                            Log.e("start","开始连接");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setConnectTimeout(10000);
                            int code = conn.getResponseCode();
                            InputStream is = conn.getInputStream();
                            String result = StreamUtils.readStream(is);
                            Message msg = Message.obtain();

                            if (code == 200) {
                                msg.what = STATUS_SUCCESS;
                                msg.obj = result;
                            } else msg.what = STATUS_ERROR;
                            nameHandler.sendMessage(msg);

                        } catch (Exception e) {
                            Message msg = Message.obtain();
                            msg.what = STATUS_ERROR;
                        }
                    }
                }.start();

            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            name.setText(user);
        }
    }

    /**
     * This function could help user turn to the set password page.
     * @param view the current context.
     */
    public void setPassword(View view) {
        thisActivity.toNextActivityWithParameter(SetPasswordActivity.class, phoneNumber);
    }

    /**
     * This function could pop up a alert window if the user wants to delete TA's account.
     * @param view the current context.
     */
    public void deleteAccount(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setMessage(getString(R.string.delete_account_warning));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.Continue), (dialog, which) -> {
            new Thread(){
                @Override
                public void run() {
                    try {
                        Encoder encode = new Encoder();
                        String encryptedPhone = encode.Base64Encode(phoneNumber);
                        String urlPath = "http://192.168.0.67:8080/WebLogin/DeleteAccount?phone=" + encryptedPhone;
                        Log.e("phone",encryptedPhone);
                        URL url = new URL(urlPath);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        int code = conn.getResponseCode();
                        Message msg = Message.obtain();
                        if (code == 200) {
                            msg.what = STATUS_SUCCESS;
                            handler.sendMessage(msg);
                        }
                        else {
                            msg.what = STATUS_ERROR;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        Message msg = Message.obtain();
                        msg.what = STATUS_ERROR;
                        handler.sendMessage(msg);
                    }

                    try {
                        Encoder encode = new Encoder();
                        String encryptedPhone = encode.Base64Encode(phoneNumber);
                        String urlPath = "http://192.168.0.67:8080/WebPicStream/DeleteAccount?phone=" + encryptedPhone;
                        URL url = new URL(urlPath);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        int code = conn.getResponseCode();
                        Message msg = Message.obtain();
                        if (code == 200) {
                            msg.what = STATUS_SUCCESS;
                            handler.sendMessage(msg);
                        }
                        else {
                            msg.what = STATUS_ERROR;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        Message msg = Message.obtain();
                        msg.what = STATUS_ERROR;
                        handler.sendMessage(msg);
                    }
                }
            }.start();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            // do nothing
        });
        builder.create().show();
    }

    /**
     * This function could help the user turn to the profie page.
     * @param view the current context.
     */
    public void changeProfile(View view) {
        thisActivity.toNextActivityWithParameter(ChangePhotoActivity.class, phoneNumber);
    }

    /**
     * This function could help the user turn to the user name page.
     * @param view view the current context.
     */
    public void changeUserName(View view) {
        thisActivity.toNextActivityWithParameter(ChangeUserNameActivity.class, phoneNumber);
    }
}
