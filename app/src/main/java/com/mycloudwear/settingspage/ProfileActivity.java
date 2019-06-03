package com.mycloudwear.settingspage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.library.Encoder;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.library.ToastCenterText;
import com.mycloudwear.R;
import com.mycloudwear.mainpages.MyActivity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to display the profile page, the user could change TA's profile.
 */
public class ProfileActivity extends BasicActivity {

    private static int UNDERWEIGHT = 1;
    private static int NORMAL_WEIGHT = 2;
    private static int OVERWEIGHT = 3;
    private static int OBESITY = 4;

    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_ERROR = -1;

    private String phoneNumber;
    private RadioButton btnMale;
    private RadioButton btnFemale;
    private RadioButton btnOthers;
    private EditText edtHeight;
    private EditText edtWeight;
    private TextView showBMI;
    private SharedPreferences sp;
    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    private ToastCenterText text = new ToastCenterText();
    /*
     * This handler could set a new BMI value.
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATUS_SUCCESS:
                    showBMI.setText(msg.obj.toString());
                    text.displayToast(ProfileActivity.this,getString(R.string.profile_update_successfully));
                    break;
                case STATUS_ERROR:
                    text.displayToast(ProfileActivity.this,getString(R.string.network_error));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the profile page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_profile);

        // Get the phone number from previous activity.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        ImageView back = findViewById(R.id.ib_title_back);
        back.setOnClickListener(v -> thisActivity.toNextActivityWithParameter(MyActivity.class, phoneNumber));

        btnMale = findViewById(R.id.btn_male);
        btnFemale = findViewById(R.id.btn_female);
        btnOthers = findViewById(R.id.btn_others);
        edtHeight = findViewById(R.id.edt_height);
        edtWeight = findViewById(R.id.edt_weight);
        showBMI = findViewById(R.id.show_BMI);

        sp = this.getSharedPreferences("config", this.MODE_PRIVATE);
        restoreInfo();
    }

    /**
     * This function will retrieve the profile once the page has been created.
     */
    private void restoreInfo() {

        String gender = sp.getString("gender", "");
        if (gender.equals(getString(R.string.male)) || gender.equals("")) btnMale.setChecked(true);
        else if (gender.equals(getString(R.string.female))) btnFemale.setChecked(true);
        else btnOthers.setChecked(true);
        String height = sp.getString("height", "");
        String weight = sp.getString("weight", "");
        String bmi = sp.getString("bmi", "");
        edtHeight.setText(height);
        edtWeight.setText(weight);
        showBMI.setText(bmi);


    }

    /**
     * This function is used to update the profile information.
     * @param view the current context.
     */
    public void updateBMI(View view) {
        String gender;
        if(btnMale.isChecked()) gender = btnMale.getText().toString();
        else if (btnFemale.isChecked()) gender = btnFemale.getText().toString();
        else gender = btnOthers.getText().toString();



        Double height = Double.parseDouble(edtHeight.getText().toString());
        Double weight = Double.parseDouble(edtWeight.getText().toString());

        int BMI;

        try{

            Double bmi = weight/(height * height/10000);
            DecimalFormat df = new DecimalFormat("#.00");

            if(bmi <= 18.5) BMI = UNDERWEIGHT;
            else if (bmi <= 24.9) BMI = NORMAL_WEIGHT;
            else if (bmi <= 29.9) BMI = OVERWEIGHT;
            else BMI = OBESITY;

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("height", height.toString());
            editor.putString("weight", weight.toString());
            editor.putString("bmi", df.format(bmi));
            editor.putString("gender", gender);
            editor.apply();

            new Thread(){
                @Override
                public void run() {
                    try {
                        Encoder encode = new Encoder();
                        String encryptedPhone = encode.Base64Encode(phoneNumber);
                        String urlPath = "http://192.168.0.67:8080/WebLogin/UpdateProfile?phone="
                                + encryptedPhone + "&height=" + height + "&weight=" + weight + "&gender=" + gender + "&bmi=" + BMI;
                        URL url = new URL(urlPath);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        int code = conn.getResponseCode();
                        Message msg = Message.obtain();

                        if (code == 200) {
                            msg.what = STATUS_SUCCESS;
                            msg.obj = df.format(bmi);
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
            text.displayToast(this, getString(R.string.height_and_weight_cannot_empty));
        }


    }

}
