package com.mycloudwear.mainpages;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mycloudwear.R;
import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.library.Encoder;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.library.PullToRefreshLayout;
import com.mycloudwear.library.StreamUtils;
import com.mycloudwear.library.ToastCenterText;
import com.mycloudwear.library.WeatherChecker;

import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import q.rorbin.badgeview.QBadgeView;

import static com.mycloudwear.basicpage.Language.getLanguageLocal;
import static com.mycloudwear.library.FileOperation.writeFile;
import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to display the homepage, the user could find the weather or system information.
 * And also feel free to check the match results by click "left" or "right", "like" or "dislike" buttons.
 * The user could also turn to another page by the navigation.
 */

public class HomepageActivity extends BasicActivity implements WeatherSearch.OnWeatherSearchListener, AMapLocationListener {

    private static final int LOAD_ERROR = 2;
    private static final int LOAD_SUCCESS = 1;
    private static final int UPLOAD_SUCCESS = 1;
    private static final int UPLOAD_FAIL = 0;
    private static final int requestLocationCode = 6;
    private static final int NOTIFY_ID = 167;
    private static final String CHANNEL_ID = "mycloudwear_channel";

    // The texts related weather.
    private String time;
    private String weather;
    private String temperature;
    private String wind;
    private String humidity;
    private String location;

    private String phoneNumber;
    private String encryptedPhone;
    private String language;
    private String user;
    private WeatherSearchQuery mquery;
    private ImageView imgLike;
    private ImageView imgDislike;
    private ImageView imgLeft;
    private ImageView imgRight;
    private ImageView btnHome;
    private ImageView matchImg;
    private TextView textHome;
    private TextView redPoint;
    private QBadgeView qBadgeView;
    private PullToRefreshLayout pullToRefreshLayout;
    private RecyclerView recyclerView;
    private URL imgAccessAddr;
    private File file;
    private ArrayList<String> imgPath;

    // Declare a SharedPreferenced object
    private SharedPreferences spBadge;
    private SharedPreferences config;
    private int currentPos = 0;
    private int clickPos = 0;
    private boolean refresh = false;
    private boolean hasClikedLike;
    private boolean hasClikedDislike;

    // Declare a AMapLocationClient object
    public AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationClientOption;
    private Encoder encode = new Encoder();
    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    private ToastCenterText text = new ToastCenterText();
    private WeatherChecker checker = new WeatherChecker();
    private NotificationManager notificationManager;

    /*
     * This handler could set a image by a bitmap.
     */
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case LOAD_SUCCESS:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    matchImg.setImageBitmap(bitmap);
                    break;
                case LOAD_ERROR:
                    text.displayToast(HomepageActivity.this, msg.obj.toString());
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    /*
     * This handler could display the preference update results.
     */
    private Handler sqlHandler = new Handler(msg -> {
        switch (msg.what){
            case UPLOAD_SUCCESS:
                text.displayToast(HomepageActivity.this,getString(R.string.update_preference_successfully));
                break;
            case UPLOAD_FAIL:
                text.displayToast(HomepageActivity.this, getString(R.string.network_error));
                break;
            default:
                break;
        }
        return false;
    });

    /*
     * This handler could give the user a pop-up notification if the match results have been updated.
     */
    private Handler signalHandler = new Handler(msg -> {
        pullToRefreshLayout.finishRefresh();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        switch (msg.what){
            case UPLOAD_SUCCESS:
                refresh = false;
                imgLike.setClickable(true);
                imgDislike.setClickable(true);
                imgLeft.setClickable(true);
                imgRight.setClickable(true);
                loadAllImagePath();
                if (!cn.getClassName().equals("com.mycloudwear.mainpages.HomepageActivity")) notifyUser();
                break;
            default:
                break;
        }
        return false;
    });

    /**
     * This function has described what kind of notification will be informed.
     */
    private void notifyUser() {
        // Create an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, HomepageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("phoneNumber", phoneNumber);
        resultIntent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.cloud)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.icon))
                .setContentTitle(getString(R.string.hello) + " " + user + ",")
                .setContentText(getString(R.string.update_result))
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pendingIntent, false)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFY_ID, builder.build());
    }

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the homepage.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_mainpage);
        language = getLanguageLocal(this);

        if(language.equals("简体中文")) language = "简体中文";
        else language = "English";

        redPoint = findViewById(R.id.info_count);
        btnHome = findViewById(R.id.img_home);
        textHome = findViewById(R.id.text_home);
        imgLike = findViewById(R.id.img_like);
        imgDislike = findViewById(R.id.img_dislike);
        imgLeft = findViewById(R.id.img_left);
        imgRight = findViewById(R.id.img_right);
        matchImg = findViewById(R.id.match_pic);

        // A refresh animation.
        pullToRefreshLayout = findViewById(R.id.activity_recycler_view);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pullToRefreshLayout.setRefreshListener(() -> {
            if(!refresh) {
                sendSignal();
                refresh = true;
            }
        });

        // Initialization
        btnHome.setSelected(true);
        textHome.setSelected(true);
        hasClikedLike = false;
        hasClikedDislike = false;

        // Get the phone number from previous activity.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
        Encoder encode = new Encoder();
        encryptedPhone = encode.Base64Encode(phoneNumber);
        qBadgeView = new QBadgeView(getBaseContext());

        // Declare all listeners.
        init();
        restoreInfo();
        newMessageDialog();

        checkLocationPermission();
        initLocation();

        loadAllImagePath();
    }

    /**
     * Add listeners.
     */
    private void init() {
        imgLike.setOnClickListener(v -> {
            if(!hasClikedLike && !hasClikedDislike){
                hasClikedLike = true;
                imgLike.setSelected(true);
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            String encryptedPath = encode.Base64Encode(imgPath.get(currentPos));
                            String urlPath = "http://192.168.0.67:8080/WebLogin/UploadPreference?username=" + encryptedPhone + "&imgpath=" + encryptedPath + "&preference=Y";
                            URL url = new URL(urlPath);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setConnectTimeout(5000);
                            int code = conn.getResponseCode();
                            Message msg = Message.obtain();
                            if (code == 200) msg.what = UPLOAD_SUCCESS;
                            else msg.what = UPLOAD_FAIL;
                            sqlHandler.sendMessage(msg);

                        } catch (Exception e) {
                            Message msg = Message.obtain();
                            msg.what = UPLOAD_FAIL;
                            sqlHandler.sendMessage(msg);
                        }
                    }
                }.start();
            }
            else{
                hasClikedLike = false;
                imgLike.setSelected(false);
            }
        });

        imgDislike.setOnClickListener(v -> {
            if(!hasClikedDislike && !hasClikedLike){
                hasClikedDislike = true;
                imgDislike.setSelected(true);
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            String encryptedPhone = encode.Base64Encode(phoneNumber);
                            String encryptedPath = encode.Base64Encode(imgPath.get(currentPos));
                            String urlPath = "http://192.168.0.67:8080/WebLogin/UploadPreference?username=" + encryptedPhone + "&imgpath=" + encryptedPath + "&preference=N";
                            URL url = new URL(urlPath);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setConnectTimeout(5000);
                            int code = conn.getResponseCode();
                            Message msg = Message.obtain();
                            if (code == 200) msg.what = UPLOAD_SUCCESS;
                            else msg.what = UPLOAD_FAIL;
                            sqlHandler.sendMessage(msg);

                        } catch (Exception e) {
                            Message msg = Message.obtain();
                            msg.what = UPLOAD_FAIL;
                            sqlHandler.sendMessage(msg);
                        }
                    }
                }.start();

                if(imgPath.size() == 1){

                    Bitmap empty = BitmapFactory.decodeResource(getResources(),R.mipmap.empty);
                    matchImg.setImageBitmap(empty);
                    setControl();

                } else {

                    imgPath.remove(currentPos);
                    if(currentPos == imgPath.size()){
                        currentPos = 0;
                    }

                    loadImageByPath(imgPath.get(currentPos));
                }
                clearSelection();
            }
            else{
                hasClikedDislike = false;
                imgDislike.setSelected(false);
            }
        });

        imgLeft.setOnClickListener(v -> {
            currentPos --;

            if(currentPos < 0){
                currentPos = imgPath.size() - 1;
            }

            checkCurrentPos();
            loadImageByPath(imgPath.get(currentPos));
        });

        imgRight.setOnClickListener(v -> {
            currentPos ++;

            if(currentPos == imgPath.size()){
                currentPos = 0;
            }

            checkCurrentPos();
            loadImageByPath(imgPath.get(currentPos));
        });

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "mycloudwear_channel";
            String description = "This is used to notify match results.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setShowBadge(false);

            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * This function is used to store and retrieve the user name so that our app could display the name
     * when giving a notification.
     */
    private void restoreInfo() {
        config = this.getSharedPreferences("config", this.MODE_PRIVATE);
        user = config.getString("name", "");
        // If the user name is empty, we need to find the name from database.
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
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setConnectTimeout(10000);
                            int code = conn.getResponseCode();
                            InputStream is = conn.getInputStream();
                            String result = StreamUtils.readStream(is);
                            if (code == 200) {
                                user = result;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * This function could initialize the badge.
     */
    private void initBadge(){
        qBadgeView.bindTarget(redPoint);
        qBadgeView.setBadgeText("");
        qBadgeView.setBadgeTextColor(Color.parseColor("#ff123564"));  //设置文本颜色
        qBadgeView.setExactMode(true);
        // Set to drag and disappear.
        qBadgeView.setOnDragStateChangedListener((dragState, badge, targetView) -> {
        });
    }

    /**
     * This function could display dialog with a red dot.
     * @param result the request code.
     */
    private void initDialogWithRedPoint(String result) {
        redPoint.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomepageActivity.this);
            builder.setTitle(getString(R.string.notification));
            builder.setMessage(getString(R.string.no_message));
            // Hide badge.
            if(result == "true") qBadgeView.hide(true);
            SharedPreferences.Editor editor = spBadge.edit();
            editor.putString("Badge", "true");
            editor.apply();
            builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                qBadgeView.hide(true);
                dialog.dismiss();
            });
            builder.show();
        });
    }

    /**
     * This function is used to check out whether the user has allow the location permission
     * and ask for permission if not.
     */
    private void checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    requestLocationCode);
    }

    /**
     * This function is used to send a update match results request once user drog and refresh the page.
     */
    private void sendSignal() {

        // Send a request to the remote server.
        new Thread() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                try {
                    URL signalUrl = new URL("http://192.168.0.143:1300/" + encryptedPhone);
                    HttpURLConnection conn = (HttpURLConnection) signalUrl.openConnection();
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(300000);
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        msg.what = UPLOAD_SUCCESS;
                    }
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                } finally {
                    signalHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * Initialize the location settings.
     */
    public void initLocation(){
        // Initialize position.
        mLocationClient = new AMapLocationClient(this);

        mLocationClientOption = new AMapLocationClientOption();
        mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClientOption.setOnceLocation(true);
        mLocationClientOption.setHttpTimeOut(8000);

        // Set position's callback listener.
        mLocationClient.setLocationListener(this);

        // Start positioning.
        mLocationClient.startLocation();
    }

    /**
     * This function is used for weather queries.
     * @param district the current location.
     */
    public void query(String district){
        /*
         * Search parameters are city and weather type, live weather is WEATHER_TYPE_LIVE, weather forecast is WEATHER_TYPE_FORECAST.
         */
        mquery = new WeatherSearchQuery(district, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        WeatherSearch mweathersearch = new WeatherSearch(this);
        mweathersearch.setOnWeatherSearchListener(this);
        mweathersearch.setQuery(mquery);
        mweathersearch.searchWeatherAsyn(); // Start asynchronous search.
    }

    /**
     * This function is used to load all images by their address.
     */
    private void loadAllImagePath() {
        new Thread(){
            @Override
            public void run() {

                try {
                    imgAccessAddr = new URL("http://192.168.0.67:8080/WebPicStream/GetMatchResult?phone=" + encryptedPhone);
                    HttpURLConnection conn = (HttpURLConnection) imgAccessAddr.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);

                    int code = conn.getResponseCode();
                    Message msg = Message.obtain();

                    if(code == 200){
                        writeFile(conn, HomepageActivity.this,"matchResult.txt");
                        beginLoadImage();
                    }
                    else if (code == 404){
                        msg.what = LOAD_ERROR;
                        msg.obj = getString(R.string.HTTP_404);
                        handler.sendMessage(msg);
                    }
                    else{
                        msg.what = LOAD_ERROR;
                        msg.obj = getString(R.string.network_error);
                        handler.sendMessage(msg);
                    }
                } catch (MalformedURLException e) {
                    text.displayToast(HomepageActivity.this,getString(R.string.url_error));
                } catch (IOException e) {
                    text.displayToast(HomepageActivity.this,getString(R.string.io_error));
                }
            }
        }.start();
    }

    /**
     * This function is used to open the file and read the address one line by one line.
     */
    private void beginLoadImage() {
        try {
            imgPath = new ArrayList<>();
            file = new File(getCacheDir(), "matchResult.txt");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            String word = br.readLine();
            if(word == null ){
                setControl();
            } else {
                imgPath.add(word);
                while((line = br.readLine())!= null){
                    imgPath.add(line);
                }

                loadImageByPath(imgPath.get(currentPos));
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This function is used to reset the button properties.
     */
    public void setControl(){
        imgLike.setClickable(false);
        imgDislike.setClickable(false);
        imgLeft.setClickable(false);
        imgRight.setClickable(false);
    }

    /**
     * This function is used to load a image from the responding url address.
     * @param path the responding url address.
     */
    private void loadImageByPath(String path) {
        new Thread(){
            @Override
            public void run(){
                file = new File(getCacheDir(),path);

                if(file.exists() && file.length() > 0){
                    Message msg = Message.obtain();
                    msg.what = LOAD_SUCCESS;
                    msg.obj = BitmapFactory.decodeFile(file.getAbsolutePath());
                    handler.sendMessage(msg);
                } else{

                    try{
                        imgAccessAddr = new URL("http://192.168.0.67:8080/WebPicStream/GetResultPhoto?phone=" + encryptedPhone + "&path=" + path);
                        HttpURLConnection conn = (HttpURLConnection) imgAccessAddr.openConnection();
                        conn.setRequestMethod("GET");

                        int code = conn.getResponseCode();
                        Message msg = Message.obtain();
                        if (code == 200){

                            InputStream is = conn.getInputStream();

                            Bitmap bitmap = BitmapFactory.decodeStream(is);

                            FileOutputStream fos = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                            fos.close();
                            is.close();

                            msg.what = LOAD_SUCCESS;
                            msg.obj = BitmapFactory.decodeFile(file.getAbsolutePath());
                            handler.sendMessage(msg);
                        }
                        else{
                            msg.what = LOAD_ERROR;
                            msg.obj = getString(R.string.cannot_open_pic);
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e){
                        Message msg = Message.obtain();
                        msg.what = LOAD_ERROR;
                        msg.obj = getString(R.string.network_error);
                        handler.sendMessage(msg);
                    }
                }

            }
        }.start();
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
     * This function is used to display the detail weather information when user click the cloud icon.
     * @param view the current context.
     * @throws InterruptedException Interrupted Exception.
     */
    public void checkWeather(View view) throws InterruptedException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(location == null){
            builder.setMessage(getString(R.string.network_error));
            // Start positioning.
            initLocation();
        } else {
            Thread.sleep(100);
            builder.setMessage(getString(R.string.set_location) + " " + location + "\n" +
                    time + "\n" +
                    getString(R.string.weather) + " " + weather + " " + temperature + "\n" +
                    humidity + "\n" +
                    wind);
        }
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            // do nothing
        });
        builder.create().show();

    }

    /**
     * This function is used to respond the "go to photo management" request.
     * @param view the current context.
     */
    public void clickPhotos(View view) {
        btnHome.setSelected(false);
        textHome.setSelected(false);
        thisActivity.toNextActivityWithParameter(PhotoActivity.class, phoneNumber);
        this.overridePendingTransition(0, 0);
        // Destroy the location client and destroy the local location service.
        mLocationClient.onDestroy();

    }

    /**
     * This function is used to respond the "go to settings" request.
     * @param view the current context.
     */
    public void clickMe(View view) {
        btnHome.setSelected(false);
        textHome.setSelected(false);
        thisActivity.toNextActivityWithParameter(MyActivity.class, phoneNumber);
        this.overridePendingTransition(0, 0);
        // Destroy the location client and destroy the local location service.
        mLocationClient.onDestroy();
    }

    /**
     * This function is used to check the current index of the photo list.
     */
    public void checkCurrentPos(){

        if(clickPos != currentPos){
            clearSelection();
        }
    }

    /**
     * This function is used to clear the current selection.
     */
    public void clearSelection(){
        hasClikedLike = false;
        hasClikedDislike = false;
        imgLike.setSelected(false);
        imgDislike.setSelected(false);
        clickPos = currentPos;
    }

    /**
     * This function is used to receive the location information and store the information if it receive successfully.
     * @param aMapLocation the AMapLocation object.
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                System.out.println("定位成功");
                // Analyze the positioning result.
                String city = aMapLocation.getCity();
                String district = aMapLocation.getDistrict();
                if(district.equals("")){
                    System.out.println("销毁定位");
                    // Destroy the location client and destroy the local location service.
                    mLocationClient.onDestroy();
                } else if(!city.equals("") || !district.equals("")){
                    switch (city){
                        case "":
                            location = district;
                            break;
                        default:
                            location = city + " " + district;
                            break;
                    }
                    checker.locationChecker(language, location);
                    location = checker.locate;
                    // Destroy the location client and destroy the local location service.
                    mLocationClient.onDestroy();
                    query(district);
                }
            }else{
                System.out.println(aMapLocation.getLocationDetail());
            }
        }else{
            System.out.println("empty location");
        }
    }

    /**
     * Real-time weather query callback.
     */
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int rCode) {
        if (rCode == 1000) {
            if (weatherLiveResult != null && weatherLiveResult.getLiveResult() != null) {
                LocalWeatherLive weatherlive = weatherLiveResult.getLiveResult();
                checker.weatherChecker(language, weatherlive.getWeather(),weatherlive.getWindDirection());
                time = getString(R.string.weather_update) + " " + weatherlive.getReportTime();
                weather = checker.weather;
                temperature = weatherlive.getTemperature() + "°C";
                wind = getString(R.string.wind_direction) + " " + checker.windDirection + " " + weatherlive.getWindPower() + " " +getString(R.string.wind_level);
                humidity = getString(R.string.humidity) + " " + weatherlive.getHumidity() + "%";
                checker.weatherChecker("English",weatherlive.getWeather(),weatherlive.getWindDirection());
                // Update these weather information to database.
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.add("phone", encryptedPhone);
                params.add("weather", checker.weather);
                params.add("temperature", weatherlive.getTemperature());
                params.add("wind", weatherlive.getWindPower().substring(1));
                params.add("humidity", weatherlive.getHumidity());
                client.post("http://192.168.0.67:8080/WebLogin/UpdateWeather", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    }
                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        Toast.makeText(HomepageActivity.this, Integer.toString(i), Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            System.out.println(rCode);
        }
    }

    /**
     * The function implements the abstract onWeatherForecastSearched function.
     * @param localWeatherForecastResult the local weather forecast result.
     * @param i the request code.
     */
    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {
        // do nothing.
    }

    /**
     * This function is used to receive the system notifications.
     */
    public void newMessageDialog() {
        // Determine whether a red dot needs to be popped up.
        // Open SharedPreferences file with the name spBadge
        spBadge = getSharedPreferences("spBadge", 0);
        // Make this file editable.
        SharedPreferences.Editor editor = spBadge.edit();
        String whetherBadge = spBadge.getString("Badge", "Null");
        if (whetherBadge.equals("true")) {
            initDialogWithRedPoint("true");
        } else {
            editor.putString("Badge", "false");
            editor.commit();
            initBadge();
            initDialogWithRedPoint("false");
        }
    }
}
