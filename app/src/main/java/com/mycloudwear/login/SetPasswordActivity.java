package com.mycloudwear.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mycloudwear.R;
import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.library.Encoder;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.library.ToastCenterText;
import com.mycloudwear.library.ImageCodeGenerator;
import com.mycloudwear.mainpages.HomepageActivity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is mainly used to set or reset the password. The user needs to confirm TA's password
 * and enter verification code if TA would like to set it or just cancel and turn to homepage.
 */
public class SetPasswordActivity extends BasicActivity {

    private static final int STATUS_ERROR = -1;
    private static final int STATUS_SUCCESS = 1;

    private String encryptedPhone;
    private String password;
    private String phoneNumber;
    private String testImageCode;
    private Encoder encode;
    private EditText edtPwd;
    private EditText edtConfirm;
    private TextView edtImgCode;
    private ImageView showImageCode;
    private ToastCenterText text = new ToastCenterText();
    private GoToNextActivity thisActivity;

    /*
     * This handler could help user go to homepage or display a warning.
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATUS_SUCCESS:
                    if (msg.obj != null){
                        text.displayToast(SetPasswordActivity.this,(String)msg.obj);
                    }
                    thisActivity.toNextActivityWithParameter(HomepageActivity.class, phoneNumber);
                    finish();
                    break;
                case STATUS_ERROR:
                    text.displayToast(SetPasswordActivity.this,getString(R.string.network_error));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the set password page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_set_password);

        edtPwd = findViewById(R.id.edt_set_pwd);
        edtConfirm = findViewById(R.id.edt_confirm);
        edtImgCode = findViewById(R.id.edt_image_code);
        showImageCode = findViewById(R.id.image_code);
        encode = new Encoder();

        thisActivity = new GoToNextActivity(this);

        // Retrieve phone number from another intent.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
        encryptedPhone = encode.Base64Encode(phoneNumber);

        // Display phone number on the current page.
        TextView showPhone = findViewById(R.id.show_phone);
        showPhone.setText(phoneNumber);

        // Display captcha.
        changeCode(showImageCode);
    }

    /**
     * This function is used to respond the "go to next" request.
     * @param view the current context.
     */
    public void clickToNext(View view) {
        password = edtPwd.getText().toString().trim();
        String confirmPwd = edtConfirm.getText().toString().trim();
        String userEnterCode = edtImgCode.getText().toString().trim().toLowerCase();
        /*
         * If the length of password or confirm password is empty, give the user a warning.
         * Else if the password length is less than 8 or larger than 16, give the user a warning.
         * Else if the password do not contain any special character, give the user a warning.
         * Else access the database to add new account.
         */
        if(TextUtils.isEmpty(password)||TextUtils.isEmpty(confirmPwd)){
            text.displayToast(this,getString(R.string.pwd_cannot_empty));
        } else if(password.length()<8 ||password.length()>16){
            text.displayToast(this,getString(R.string.password_length_incorrect));
        } else if(!stringFilter(password)){
            text.displayToast(this,getString(R.string.pwd_require_special_character));
        } else if (password.equals(confirmPwd)){
            if (userEnterCode.equals(testImageCode)){
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            String encryptedPwd = encode.Base64Encode(password);
                            String urlPath = "http://192.168.0.67:8080/WebLogin/SetPasswordServlet?username=" + encryptedPhone + "&password=" + encryptedPwd;
                            URL url = new URL(urlPath);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setConnectTimeout(5000);
                            int code = conn.getResponseCode();
                            Message msg = Message.obtain();
                            if (code == 200) {
                                msg.what = STATUS_SUCCESS;
                                msg.obj = getString(R.string.pwd_set_successfully);
                                mHandler.sendMessage(msg);
                            } else {
                                msg.what = STATUS_ERROR;
                                mHandler.sendMessage(msg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Message msg = Message.obtain();
                            msg.what = STATUS_ERROR;
                            mHandler.sendMessage(msg);
                        }
                    }
                }.start();
            }
            else{
                text.displayToast(SetPasswordActivity.this,getString(R.string.sms_code_incorrect));
            }
        }
        else{
            text.displayToast(SetPasswordActivity.this,getString(R.string.confirm_pwd_incorrect));
        }


    }

    /**
     * This function is used to find whether password contains any special character.
     * @param str the given password.
     * @return the match result, true or false.
     */
    public boolean stringFilter(String str) {
        String regex = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~!@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern pattern = Pattern.compile(regex );
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    /**
     * This function is used to simulate the android's back control button.
     * This back button could take the user to the homepage.
     */
    @Override
    public void onBackPressed(){
        new Thread(){
            @Override
            public void run() {
                try {
                    String urlPath = "http://192.168.0.67:8080/WebLogin/SetPasswordServlet?username=" + encryptedPhone;
                    URL url = new URL(urlPath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        Message msg = Message.obtain();
                        msg.what = STATUS_SUCCESS;
                        mHandler.sendMessage(msg);
                    } else {
                        Message msg = Message.obtain();
                        msg.what = STATUS_ERROR;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = STATUS_ERROR;
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * This function is used to go back to the homepage.
     * @param view the current context.
     */
    public void cancel(View view){
        new Thread(){
            @Override
            public void run() {
                try {
                    String urlPath = "http://192.168.0.67:8080/WebLogin/SetPasswordServlet?username=" + encryptedPhone;
                    URL url = new URL(urlPath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        Message msg = Message.obtain();
                        msg.what = STATUS_SUCCESS;
                        mHandler.sendMessage(msg);
                    } else {
                        Message msg = Message.obtain();
                        msg.what = STATUS_ERROR;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = STATUS_ERROR;
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * This function is used to change the captcha if the user finds the code is hard to tell the differences.
     * @param view the current context.
     */
    public void changeCode(View view){
        showImageCode.setImageBitmap(ImageCodeGenerator.getInstance().createBitmap());
        testImageCode = ImageCodeGenerator.getInstance().getCode().toLowerCase();
    }

}

