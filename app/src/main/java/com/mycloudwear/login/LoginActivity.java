package com.mycloudwear.login;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.mycloudwear.R;
import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.documentpages.TermsActivity;
import com.mycloudwear.library.CountryPicker;
import com.mycloudwear.library.Encoder;
import com.mycloudwear.library.StreamUtils;
import com.mycloudwear.library.ToastCenterText;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.mainpages.HomepageActivity;

import static com.mycloudwear.basicpage.Language.getLanguageLocal;
import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is mainly used to achieve login operation. It also welcomes new users to sign up and
 * password reset. Besides, users could feel free to read our terms of service.
 */
public class LoginActivity extends BasicActivity {

    private static final int STATUS_SUCCESS = 1;
    private static final int PWD_ERROR = 0;
    private static final int STATUS_ERROR = -1;
    private static final int requestStorageCode = 7;

    private EditText userPhoneNum;
    private EditText userPwd;
    private ImageView imgEye;
    private ImageView mImage;
    private String language;
    private SharedPreferences sp;
    private Switch remember_me;
    private StringBuilder account;
    private Button btnCountryCode;
    private Bitmap bitmap;
    private boolean isOpenEye = false;

    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    private ToastCenterText text = new ToastCenterText();

    /*
     * This handler could make a bitmap if the file exists.
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            File file = (File)msg.obj;
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                mImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                mImage.setImageResource(R.mipmap.icon);
            } catch (NullPointerException e) {
                mImage.setImageResource(R.mipmap.icon);
            }
        }
    };

    /*
     * This handler could handle different login status and give the corresponding response.
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATUS_SUCCESS:
                    break;
                case PWD_ERROR:
                    text.displayToast(LoginActivity.this,getString(R.string.login_fail));
                    break;
                case STATUS_ERROR:
                    text.displayToast(LoginActivity.this,getString(R.string.network_error));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the login page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_login);
        language = getLanguageLocal(this);
        if(language.equals("简体中文")) language = "简体中文";
        else language = "English";

        // Find the widgets on the page.
        userPhoneNum = findViewById(R.id.et_phone);
        userPwd = findViewById(R.id.et_pwd);
        remember_me = findViewById(R.id.re_me);
        imgEye = findViewById(R.id.btn_close);
        mImage = findViewById(R.id.profile);
        btnCountryCode = findViewById(R.id.btn_country_code);

        // Find the SharedPreferences.
        sp = this.getSharedPreferences("config", this.MODE_PRIVATE);

        // Add "hide password" button listener.
        imgEye.setOnClickListener(v -> {
            if (!isOpenEye) {
                imgEye.setSelected(true);
                isOpenEye = true;
                // Make password visible.
                userPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                imgEye.setSelected(false);
                isOpenEye = false;
                //Make password invisible.
                userPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        // Check whether external storage permission has been allowed or not.
        checkStoragePermission();

        // Store the password and phone number.
        restoreInfo();
    }

    /**
     * This function could help to remind users to open external storage permission.
     */
    private void checkStoragePermission() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestStorageCode);
    }

    /**
     * This function is used to download the user's portrait from our web server.
     * @param urlStr the address where stores the user's portrait.
     * @return a temp file which contains the user's portrait.
     */
    public File downloadImg(String urlStr) {
        File file = null ;
        try {
            URL url = new URL(urlStr);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(3000);
            InputStream input = urlConnection.getInputStream();
            file = File.createTempFile("temp_head", "jpg");
            OutputStream output = new FileOutputStream(file) ;
            byte[] byt = new byte[1024];
            int length = 0;
            // 开始读取
            while ((length = input.read(byt)) != -1) {
                output.write(byt, 0, length);
            }
            input.close();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * The function is used to remember the user if TA clicks "remember me" button.
     */
    public void restoreInfo(){
        if (sp.getBoolean("remember",false)) remember_me.setChecked(true);
        String countryCode = btnCountryCode.getText().toString();
        account = new StringBuilder(countryCode);
        String number = sp.getString("number", "");
        account.append(number);
        String password = sp.getString("password", "");
        userPhoneNum.setText(number);
        userPwd.setText(password);
        // Once we open the login page, MyCloudwear requests web server to return the user's portrait.
        new Thread(){
            @Override
            public void run() {
                Message message = handler.obtainMessage();
                Encoder encode = new Encoder();
                String encryptedPhone = encode.Base64Encode(account.toString());
                message.obj = downloadImg("http://192.168.0.67:8080/WebPicStream/GetPortrait?phone=" + encryptedPhone);
                message.sendToTarget();
            }
        }.start();
    }

    /**
     * This function is used to check the user's input data and make a decision whether let user in
     * or not.
     * @param view the current context.
     */
    public void login(View view) {
        // Get the user's phone number and country code.
        String countryCode = btnCountryCode.getText().toString();
        account = new StringBuilder(countryCode);
        String phoneNum = userPhoneNum.getText().toString().trim();
        account.append(phoneNum);

        // Get the user's password.
        String password = userPwd.getText().toString().trim();

        // If user enters any empty data entry, alert the user with a warning.
        if(TextUtils.isEmpty(phoneNum)||TextUtils.isEmpty(password)){
            text.displayToast(this,getString(R.string.phone_pwd_cannot_empty));
        } else{
            // Ready to cache
            if(remember_me.isChecked()){
                // Need to store the phone number and password if user clicks "remember" button.
                // Save these data to the SharedPreferences file.
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("remember",true);
                editor.putString("number", phoneNum);
                editor.putString("password", password);
                // Submit data and close the transaction stream.
                editor.apply();
            }
            // Access web server to check whether user's login information are correct ot not.
            new Thread(){
                @Override
                public void run() {
                    try {
                        Encoder encode = new Encoder();
                        String encryptedPhone = encode.Base64Encode(account.toString());
                        String encryptedPwd = encode.Base64Encode(password);
                        String urlPath = "http://192.168.0.67:8080/WebLogin/LoginServlet?username=" + encryptedPhone + "&password=" + encryptedPwd;
                        URL url = new URL(urlPath);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        int code = conn.getResponseCode();
                        if (code == 200) {
                            InputStream is = conn.getInputStream();
                            String result = StreamUtils.readStream(is);
                            Message msg = Message.obtain();
                            if (result.equals("pass")) {
                                msg.what = STATUS_SUCCESS;
                                mHandler.sendMessage(msg);
                                thisActivity.toNextActivityWithParameter(HomepageActivity.class, account.toString());
                            }
                            else {
                                msg.what = PWD_ERROR;
                                mHandler.sendMessage(msg);
                            }
                        }
                        else {
                            Message msg = Message.obtain();
                            msg.what = STATUS_ERROR;
                            msg.obj = code;
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
    }

    /**
     * This function extends super method.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * This function is used to click "forget" button then app turn to another page.
     * @param view the current context.
     */
    public void forget(View view) {
        thisActivity.toNextActivityWithoutParameter(VerifyAccount.class);
    }

    /**
     * This function is used to click "sign up" button then app turn to another page.
     * @param view the current context.
     */
    public void signUp(View view) {
        thisActivity.toNextActivityWithoutParameter(VerifyPhoneActivity.class);
    }

    /**
     * This function is used to click "send" button to receive the verification code.
     * @param view the current context.
     */
    public void requestCountryCode(View view) {
        if(view.getId() == R.id.btn_country_code)
            CountryPicker.newInstance(null, country -> {
                if (country.flag != 0) btnCountryCode.setText("+" + country.code);
            }).show(getSupportFragmentManager(), "country");
    }

    /**
     * This function is used to click terms of service.
     * @param view the current context.
     */
    public void gotoTerms(View view) {
        thisActivity.toNextActivityWithParameter(TermsActivity.class, language);
    }
}
