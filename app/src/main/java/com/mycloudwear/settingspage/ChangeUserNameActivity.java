package com.mycloudwear.settingspage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.mycloudwear.R;
import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.library.Encoder;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.library.StreamUtils;
import com.mycloudwear.library.ToastCenterText;
import com.mycloudwear.mainpages.MyActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to display the change user name page, the user could change TA's name.
 */
public class ChangeUserNameActivity extends BasicActivity {

    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_ERROR = -1;

    private String phoneNumber;
    private EditText edtName;
    private String name;
    private SharedPreferences sp;
    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    private ToastCenterText text = new ToastCenterText();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATUS_SUCCESS:
                    edtName.setText(msg.obj.toString());
                    text.displayToast(ChangeUserNameActivity.this,getString(R.string.profile_update_successfully));
                    break;
                case STATUS_ERROR:
                    text.displayToast(ChangeUserNameActivity.this,getString(R.string.network_error));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the change user name page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_user_name);

        edtName = findViewById(R.id.txt_user_name);

        // Get the phone number from previous activity.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        ImageView back = findViewById(R.id.ib_title_back);
        back.setOnClickListener(v -> thisActivity.toNextActivityWithParameter(MyActivity.class, phoneNumber));


        sp = this.getSharedPreferences("config", this.MODE_PRIVATE);
        restoreInfo();
    }

    /**
     * This method will retrieve the user name once the page has been created.
     */
    private void restoreInfo() {

        name = sp.getString("name", "");
        if (name.equals("")) {
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
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setConnectTimeout(5000);
                            int code = conn.getResponseCode();
                            InputStream is = conn.getInputStream();
                            String result = StreamUtils.readStream(is);
                            Message msg = Message.obtain();

                            if (code == 200) {
                                name = result;
                            }
                            else msg.what = STATUS_ERROR;
                            handler.sendMessage(msg);

                        } catch (Exception e) {
                            Message msg = Message.obtain();
                            msg.what = STATUS_ERROR;
                            handler.sendMessage(msg);
                        }
                    }
                }.start();

            } catch (Exception e){
                e.printStackTrace();
            }
        }
        else edtName.setText(name);
        edtName.setSelection(name.length());

    }

    /**
     * This function is used to update the user name to the database.
     * @param view the current view.
     */
    public void updateName(View view) {
        name = edtName.getText().toString();
        edtName.clearFocus();
        if (name.length()>20) text.displayToast(this,getString(R.string.name_length));
        else{
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("name", name);
            editor.apply();
            try{

                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Encoder encode = new Encoder();
                            String encryptedPhone = encode.Base64Encode(phoneNumber);
                            String encryptedName = encode.Base64Encode(name);
                            String urlPath = "http://192.168.0.67:8080/WebLogin/UpdateName?phone="
                                    + encryptedPhone + "&name=" + encryptedName;
                            URL url = new URL(urlPath);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setConnectTimeout(5000);
                            int code = conn.getResponseCode();
                            Message msg = Message.obtain();

                            if (code == 200) {
                                msg.what = STATUS_SUCCESS;
                                msg.obj = name;
                            }
                            else msg.what = STATUS_ERROR;
                            handler.sendMessage(msg);

                        } catch (Exception e) {
                            Message msg = Message.obtain();
                            msg.what = STATUS_ERROR;
                            handler.sendMessage(msg);
                        }
                    }
                }.start();

            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }

}
