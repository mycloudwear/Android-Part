package com.mycloudwear.mainpages;

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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;
import com.loopj.android.http.RequestParams;
import com.mycloudwear.R;
import com.mycloudwear.adapter.SimpleAdapter;
import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.library.Encoder;
import com.mycloudwear.library.GoToNextActivity;
import com.mycloudwear.library.ToastCenterText;
import com.mycloudwear.settingspage.ChangePhotoActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mycloudwear.library.FileOperation.writeFile;
import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to display the skirt page, the user could use following function:
 * 1. Add photos in bulk.
 * 2. Add a photo.
 * 3. Delete a photo.
 */
public class SkirtActivity extends BasicActivity {
    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    private static final String SEPARATOR = ",";
    private static final int LOAD_ERROR = 2;
    private static final int LOAD_SUCCESS = 1;

    private URL imgAccessAddr;
    private Uri tempUri;
    private File tempFile;
    private String line;
    private String phoneNumber;
    private String encryptedPhone;
    private String filename;
    private Bitmap mBitmap;
    private RecyclerView mListView;
    private SimpleAdapter mAdapter;
    private List<String> photoSelect = new ArrayList<>();
    private List<String> fileNameList = new ArrayList<>();
    private GoToNextActivity thisActivity = new GoToNextActivity(this);
    private ToastCenterText text = new ToastCenterText();

    /*
     * This handler could add a new photo address into the list and display the photo as well.
     */
    private Handler handler = new Handler(msg -> {
        switch (msg.what){
            case LOAD_SUCCESS:
                photoSelect.add(msg.obj.toString());
                showPicture();
                break;
            case LOAD_ERROR:
                text.displayToast(SkirtActivity.this, msg.obj.toString());
                photoSelect = new ArrayList<>();
                break;
        }
        return false;
    });

    /**
     * This method implements the super method.
     * The main purpose of this method is to show the skirt page.
     * @param savedInstanceState current state of the instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_skirt);

        mListView= findViewById(R.id.rlv_list);

        // Get the phone number from previous activity.
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
        Encoder encode = new Encoder();
        encryptedPhone = encode.Base64Encode(phoneNumber);

        mAdapter = new SimpleAdapter(this, photoSelect);
        mAdapter.setOnItemClickListener(new SimpleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {

            }

            @Override
            public void onItemLongClick(View view, final int position) {
                initAlertDialog(position);
            }
        });

        List<String> tempList = (List<String>) intent.getSerializableExtra("photo");
        List<String> tempName = (List<String>) intent.getSerializableExtra("name");

        if (tempList != null) {
            fileNameList.addAll(tempName);
            photoSelect.addAll(tempList);
            showPicture();
        } else {
            loadAllImagePath();
        }

        ImageView back = findViewById(R.id.ib_title_back);
        back.setOnClickListener(v -> {
            thisActivity.toNextActivityWithParameter(PhotoActivity.class, phoneNumber);
            finish();
        });
    }

    /**
     * This function is used to load all images by their address.
     */
    private void loadAllImagePath() {
        new Thread(){
            @Override
            public void run() {

                try {
                    imgAccessAddr = new URL("http://192.168.0.67:8080/WebPicStream/GetSkirtTXT?phone=" + encryptedPhone);
                    HttpURLConnection conn = (HttpURLConnection) imgAccessAddr.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);

                    int code = conn.getResponseCode();
                    Message msg = Message.obtain();

                    if(code == 200){
                        writeFile(conn, SkirtActivity.this,"skirtList.txt");
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
                    Message msg = Message.obtain();
                    msg.what = LOAD_ERROR;
                    msg.obj = getString(R.string.url_error);
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    Message msg = Message.obtain();
                    msg.what = LOAD_ERROR;
                    msg.obj = getString(R.string.io_error);
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * This function is used to open the file and read the address one line by one line.
     */
    private void beginLoadImage() {
        try {
            tempFile = new File(getCacheDir(), "skirtList.txt");
            FileInputStream fis = new FileInputStream(tempFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            while((line = br.readLine())!= null){
                if(line.length() == 0) return;
                fileNameList.add(line);
                loadImageByPath(line);
            }
            fis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function is used to load a image from the responding url address.
     * @param line the responding url address.
     */
    private void loadImageByPath(String line) {
        new Thread(){
            @Override
            public void run(){
                File file = new File(getCacheDir(),line);
                    try{
                        imgAccessAddr = new URL("http://192.168.0.67:8080/WebPicStream/GetSkirtPhoto?phone=" + encryptedPhone + "&path=" + line);
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
                            msg.obj = file.getAbsolutePath();
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
        }.start();
    }

    /**
     * This function could display all selected photos on the page.
     */
    private void showPicture() {
        if (photoSelect!= null) {
            mListView.setAdapter(mAdapter);
            mListView.setLayoutManager(new GridLayoutManager(this,2));
            updateSkirtList();
            findBitmap();
        }
    }

    /**
     * This function could update the list and send the update request to the web server.
     */
    private void updateSkirtList() {
        Encoder encode = new Encoder();
        String encryptedPhone = encode.Base64Encode(phoneNumber);
        String fileList = "";
        if(fileNameList.size()!=0){
            StringBuilder listBuilder = new StringBuilder();
            for(String name : fileNameList){
                listBuilder.append(name);
                listBuilder.append(SEPARATOR);
            }
            fileList = listBuilder.toString();
            if(fileList.length() == 0) return;
            fileList = fileList.substring(0, fileList.length() - 1);
        }
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("phone", encryptedPhone);
        params.add("list", fileList);
        client.post("http://192.168.0.67:8080/WebPicStream/UpdateSkirtList", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
            }
            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            }
        });
    }

    /**
     * This function is used to add new photos by choosing from albums or taking a photo.
     * @param view the current context.
     */
    public void addPhoto(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_a_photo));
        String[] items = { getString(R.string.select_from_album), getString(R.string.camera) };
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setItems(items, (dialog, which) -> {
            switch (which) {
                case CHOOSE_PICTURE:
                    thisActivity.toNextActivityWithList(SelectPhotoActivity.class, phoneNumber, photoSelect,fileNameList,"skirt");
                    break;
                case TAKE_PICTURE:
                    Intent openCameraIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String name = System.currentTimeMillis() + ".jpg";
                    tempFile = new File(rootPath + "/test/" + name);
                    tempFile.getParentFile().mkdirs();
                    tempUri = FileProvider.getUriForFile(this, "com.mycloudwear.settingspage.fileprovider", tempFile);
                    // Add permission.
                    openCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                    openCameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
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
        System.out.println(data);
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
    private void setImageToView(Uri uri) {
        ContentResolver resolver = getContentResolver();
        try {
            mBitmap = MediaStore.Images.Media.getBitmap(resolver, uri);
            OutputStream output = new FileOutputStream(tempFile) ;
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100,output);
            output.close();
            photoSelect.add(uri.toString().replace("file://",""));
            fileNameList.add(UUID.randomUUID().toString() + ".jpg");
            showPicture();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function is used to give a alert window if user want to delete a photo.
     * @param position the current index of the photo list.
     */
    private void initAlertDialog(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SkirtActivity.this);
        dialog.setMessage(getString(R.string.really_delete_photo));
        dialog.setCancelable(false);
        dialog.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
            String targetPath = mAdapter.getPath(position);
            int index = photoSelect.indexOf(targetPath);
            String targetFileName = fileNameList.get(index);
            fileNameList.remove(index);
            photoSelect.remove(index);
            mAdapter.deleteData(position);
            deleteBitmap(targetFileName);
            updateSkirtList();
            Toast.makeText(SkirtActivity.this,getString(R.string.delete_successfully), Toast.LENGTH_SHORT).show();
        });
        dialog.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> Toast.makeText(SkirtActivity.this, getString(R.string.delete_cancel), Toast.LENGTH_SHORT).show());
        dialog.show();
    }

    /**
     * This function is used to send a request to delete a photo from the web server.
     * @param targetPath the url address where the photo is stored.
     */
    private void deleteBitmap(String targetPath) {
        Encoder encode = new Encoder();
        String encryptedPhone = encode.Base64Encode(phoneNumber);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("phone", encryptedPhone);
        params.add("target", targetPath);
        client.post("http://192.168.0.67:8080/WebPicStream/DeleteSkirtBitmap", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
            }
            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(SkirtActivity.this, Integer.toString(i), Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * This function is used to translate a url address to a bitmap.
     */
    private void findBitmap(){
        for(int i = 0; i < photoSelect.size(); i++){
            tempUri = Uri.fromFile(new File(photoSelect.get(i)));
            ContentResolver resolver = getContentResolver();
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(resolver, tempUri);
                AddImage(mBitmap, fileNameList.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This function is used to send a request to upload a photo to the web server.
     * @param bmap the given bitmap.
     * @param name the file name of the given bitmap.
     */
    private void AddImage(Bitmap bmap, String name)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
        byte[] bytes = stream.toByteArray();
        String img = Base64.encodeToString(bytes, Base64.DEFAULT);
        Encoder encode = new Encoder();
        String encryptedPhone = encode.Base64Encode(phoneNumber);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("phone", encryptedPhone);
        params.add("filename", name);
        params.add("img", img);
        client.post("http://192.168.0.67:8080/WebPicStream/UpdateSkirt", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
            }
            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(SkirtActivity.this, Integer.toString(i), Toast.LENGTH_LONG).show();
            }
        });
    }
}
