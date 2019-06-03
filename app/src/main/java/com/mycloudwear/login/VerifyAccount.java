package com.mycloudwear.login;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mycloudwear.R;
import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.library.CountDownTime;
import com.mycloudwear.library.Country;
import com.mycloudwear.library.CountryPicker;
import com.mycloudwear.library.Encoder;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.library.StreamUtils;
import com.mycloudwear.library.ToastCenterText;

import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to display the verify account page, the user could enter phone number and
 * receive verification code.
 */
public class VerifyAccount extends BasicActivity {

    private static final int STATUS_SUCCESS = 1;
    private static final int ACCOUNT_ERROR = -1;
    private static final int STATUS_ERROR = 2;

    private CountDownTime onMin;
    private Button btnCountryCode;
    private EditText edtPhone;
    private EditText edtCode;
    private Button btnSend;
    private String phoneNumber;
    private String receiveCode;
    private String countryCode;
    private TextView terms;
    private StringBuilder account;
    private boolean flag = true;
    private ToastCenterText text = new ToastCenterText();
    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    EventHandler eventHandler;

    /*
     * This handler could handle different verification status and give the corresponding response.
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATUS_SUCCESS:
                    SMSSDK.getVerificationCode(countryCode,phoneNumber);
                    onMin.start(); // Start timer.
                    edtCode.requestFocus();
                    break;
                case ACCOUNT_ERROR:
                    text.displayToast(VerifyAccount.this,getString(R.string.account_does_not_exist));
                    break;
                case STATUS_ERROR:
                    text.displayToast(VerifyAccount.this,getString(R.string.network_error));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the verify account page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_verify_phone);

        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("");
        ImageView back = findViewById(R.id.ib_title_back);
        back.setOnClickListener(v -> thisActivity.toNextActivityWithoutParameter(LoginActivity.class));

        edtPhone = findViewById(R.id.reg_phone);
        edtCode = findViewById(R.id.edt_register_code);
        btnSend = findViewById(R.id.btn_register_send);
        onMin = new CountDownTime(60000, 1000,btnSend);//初始化对象
        btnCountryCode = findViewById(R.id.btn_code);
        terms = findViewById(R.id.txt_register_term);
        terms.setText("");
        // Operation callback.
        EventHandler eventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        // Register callback listener interface.
        SMSSDK.registerEventHandler(eventHandler);
    }



    public void requestRegCode(View view){
        phoneNumber = edtPhone.getText().toString().trim();
        if(TextUtils.isEmpty(phoneNumber))
        {
            text.displayToast(this,getString(R.string.please_enter_phone_number));
            edtPhone.requestFocus();
        }
        else if(phoneNumber.length() < 10)
        {
            text.displayToast(this,getString(R.string.phone_digit_incorrect));
            edtPhone.requestFocus();
        }
        else
        {
            countryCode = btnCountryCode.getText().toString();
            account = new StringBuilder(countryCode);
            account.append(phoneNumber);
            new Thread(){
                @Override
                public void run() {
                    try {
                        Encoder encode = new Encoder();
                        String encryptedPhone = encode.Base64Encode(account.toString());
                        String urlPath = "http://192.168.0.67:8080/WebLogin/CheckAccount?username=" + encryptedPhone;
                        URL url = new URL(urlPath);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        int code = conn.getResponseCode();
                        if (code == 200) {
                            InputStream is = conn.getInputStream();
                            String result = StreamUtils.readStream(is);
                            Message msg = Message.obtain();
                            if (result.equals("pass")) msg.what = STATUS_SUCCESS;
                            else msg.what = ACCOUNT_ERROR;
                            mHandler.sendMessage(msg);
                        } else {
                            Message msg = Message.obtain();
                            msg.what = STATUS_ERROR;
                            mHandler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        Message msg = Message.obtain();
                        msg.what = STATUS_ERROR;
                        mHandler.sendMessage(msg);
                    }
                }
            }.start();
        }
    }

    public void requestCountryCode(View view) {
        if(view.getId() == R.id.btn_code)
            CountryPicker.newInstance(null, country -> {
                if (country.flag != 0) btnCountryCode.setText("+" + country.code);
            }).show(getSupportFragmentManager(), "country");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Country.destroy();
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    public void nextToSetPwd (View view){
        if(secondVerify()) {
            SMSSDK.submitVerificationCode(countryCode,phoneNumber,receiveCode);
            flag = false;
        }

    }


    private boolean secondVerify()
    {
        receiveCode = edtCode.getText().toString().trim();
        if(TextUtils.isEmpty(receiveCode))
        {
            text.displayToast(this,getString(R.string.please_enter_sms_code));
            edtCode.requestFocus();
            return false;
        } else if(edtCode.getText().toString().trim().length()!=4) {
            text.displayToast(this,getString(R.string.sms_code_incorrect));
            edtCode.requestFocus();

            return false;
        } else {
            return true;
        }

    }

    /**
     * Use Handler to distribute the Message object to the main thread and handle the event
     */
    Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event= msg.arg1;
            int result= msg.arg2;
            Object data= msg.obj;


            if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                if(result == SMSSDK.RESULT_COMPLETE) {
                    text.displayToast(VerifyAccount.this,getString(R.string.code_has_sent));
                    edtPhone.requestFocus();
                }
            }

            if(result==SMSSDK.RESULT_COMPLETE)
            {

                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    text.displayToast(VerifyAccount.this, getString(R.string.verification_successfully));
                    thisActivity.toNextActivityWithParameter(SetPasswordActivity.class, account.toString());
                    finish();
                }
            } else {
                if(flag)
                {
                    btnSend.setVisibility(View.VISIBLE);
                    text.displayToast(VerifyAccount.this,getString(R.string.msg_sent_fail));
                    edtPhone.requestFocus();
                } else {
                    text.displayToast(VerifyAccount.this,getString(R.string.verification_fail));
                }
            }
        }

    };
}
