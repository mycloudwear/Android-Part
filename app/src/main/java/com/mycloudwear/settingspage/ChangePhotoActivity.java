package com.mycloudwear.settingspage;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;
import com.loopj.android.http.RequestParams;
import com.mycloudwear.R;
import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.library.Encoder;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.library.ToastCenterText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.http.Header;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to change the user's portrait.
 */
public class ChangePhotoActivity extends BasicActivity {

    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    protected static Uri tempUri;
    private ImageView mImage;
    private Bitmap mBitmap;
    private String phoneNumber;
    private String encryptedPhone;
    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    private ToastCenterText text = new ToastCenterText();

    /*
     * This handler could set a new image view.
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            File file = (File)msg.obj;
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            mImage.setImageBitmap(bitmap);
        }
    };

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the change photo page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_change_photo);

        ImageView back = findViewById(R.id.ib_title_back);
        back.setOnClickListener(v -> {
            thisActivity.toNextActivityWithParameter(AccountActivity.class, phoneNumber);
            finish();
        });

        mImage = findViewById(R.id.iv_image);

        // Get the phone number from previous activity.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
        Encoder encode = new Encoder();
        encryptedPhone = encode.Base64Encode(phoneNumber);

        new Thread(){
            @Override
            public void run() {
                Message message = handler.obtainMessage();
                message.obj = downloadImg("http://192.168.0.67:8080/WebPicStream/GetPortrait?phone=" + encryptedPhone);
                message.sendToTarget();
            }
        }.start();
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
            urlConnection.setConnectTimeout(5*1000);
            InputStream input = urlConnection.getInputStream();
            file = File.createTempFile("temp_head", "jpg");
            OutputStream output = new FileOutputStream(file) ;
            byte[] byt = new byte[1024];
            int length = 0;
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
     * This function could invoke another funtion called showChoosePicDialog() to display a dialog.
     * @param view the current context.
     */
    public void selectAPhoto(View view) {
        showChoosePicDialog();
    }

    /**
     * This function is used to display a dialog for the user to choose a photo.
     */
    protected void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_a_photo));
        String[] items = { getString(R.string.select_from_album), getString(R.string.camera) };
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setItems(items, (dialog, which) -> {
            switch (which) {
                case CHOOSE_PICTURE: // // Select a local photo.
                    Intent openAlbumIntent = new Intent(
                            Intent.ACTION_GET_CONTENT);
                    openAlbumIntent.setType("image/*");
                    /*
                     * Use the startActivityForResult method and rewrite the onActivityResult() method
                     * then get the image to do the cropping operation.
                     */
                    startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                    break;
                case TAKE_PICTURE: // take a photo.
                    Intent openCameraIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/test/" + System.currentTimeMillis() + ".jpg");
                    file.getParentFile().mkdirs();
                    tempUri = FileProvider.getUriForFile(this, "com.mycloudwear.settingspage.fileprovider", file);
                    // Add permissions.
                    openCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                    startActivityForResult(openCameraIntent, TAKE_PICTURE);
                    break;
            }
        });
        builder.show();
    }

    /**
     * This function is used to respond to the photo crop request.
     * @param requestCode the request code.
     * @param resultCode the result code.
     * @param data the metadata
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                setImageToView(result.getUri());
                return;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        switch (requestCode) {
            case TAKE_PICTURE:
                break;
            case CHOOSE_PICTURE:
                // Original resource address of the photo.
                tempUri = data.getData();
                break;
        }
        if(resultCode == ChangePhotoActivity.RESULT_OK){
            CropImage.activity(tempUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

    }

    /**
     * This function is used to set image view.
     * @param uri the given url address.
     */
    protected void setImageToView(Uri uri) {

        ContentResolver resolver = getContentResolver();
        try {
            mBitmap = MediaStore.Images.Media.getBitmap(resolver, uri);
            File file = File.createTempFile("temp_head", "jpg");
            OutputStream output = new FileOutputStream(file) ;
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 40,output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImage.setImageBitmap(mBitmap);
        sendImage(mBitmap);

    }

    /**
     * This function is used to send a request to upload a photo to the web server.
     * @param bmap the given bitmap.
     */
    private void sendImage(Bitmap bmap)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
        byte[] bytes = stream.toByteArray();
        String img = Base64.encodeToString(bytes, Base64.DEFAULT);
        Encoder encode = new Encoder();
        String encryptedPhone = encode.Base64Encode(phoneNumber);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("phone", encryptedPhone);
        params.add("img", img);
        client.post("http://192.168.0.67:8080/WebPicStream/UploadPortrait", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Toast toast = Toast.makeText(ChangePhotoActivity.this,getString(R.string.upload_success),Toast.LENGTH_SHORT);
                toast.show();
            }
            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast toast = Toast.makeText(ChangePhotoActivity.this,Integer.toString(i),Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

}
