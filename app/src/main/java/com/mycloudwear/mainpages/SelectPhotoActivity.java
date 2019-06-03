package com.mycloudwear.mainpages;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mycloudwear.R;
import com.mycloudwear.adapter.Adapter;
import com.mycloudwear.basicpage.BasicActivity;
import com.mycloudwear.bean.FolderBean;
import com.mycloudwear.library.GoToNextActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.mycloudwear.library.StatusBar.changeStatusColor;

/**
 * @author kongkongdaren
 * @version 1.0.1
 * @since 15/5/2019
 * Created by Android on 26/6/2018
 * The original code was provided by kongkongdaren (https://github.com/kongkongdaren), but in our app
 * we only use part of his code to achieve our function.
 * This class creates a multi-choose activity when the user choosing the uploading image.
 */
public class SelectPhotoActivity extends BasicActivity {
    private GridView mGridView;
    private List<String> mImgs;
    private RelativeLayout mbottomLayout;
    private TextView mTvDirName,mTvDirCount;
    private TextView mBtnSelect;
    private String phoneNumber;
    private String label;
    private File mCurrentDir;
    private  int mMaxCount;
    private List<String> photoSelect = new ArrayList<>();
    private List<String> fileNameList = new ArrayList<>();
    private ListImageDirPopupWindow mImageDirPopupWindow;
    private List<FolderBean> mFolderBeans=new ArrayList<>();
    private GoToNextActivity thisActivity = new GoToNextActivity(this);

    private ProgressDialog mProgressDialog;
    private Adapter adapter;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==0x110){
                mProgressDialog.dismiss();
                dataToView();
                initPopupWindow();
            }
        }
    };

    /**
     * This function creates the activity at the beginning.
     * @Override
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusColor(this.getWindow());
        setContentView(R.layout.activity_select_main);

        // 获取界面的电话号码数据
        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
        photoSelect = (List<String>) intent.getSerializableExtra("photo");
        fileNameList = (List<String>) intent.getSerializableExtra("name");
        label = intent.getStringExtra("label");
        initView();
        initDatas();
        initEvent();
    }

    /**
     * This function initializes the pop up window which displays the different folders.
     */
    private void initPopupWindow() {
        mImageDirPopupWindow=new ListImageDirPopupWindow(this,mFolderBeans);
        mImageDirPopupWindow.setOnDismissListener(() -> lightOn());
        mImageDirPopupWindow.setOnDirSelectedListener(folderBean -> {
            mCurrentDir=new File(folderBean.getDir());

            mImgs= Arrays.asList(mCurrentDir.list((dir, name) -> {
                if (name.endsWith(".jpg")||name.endsWith("jpeg")||name.endsWith("png")){
                    return  true;
                }
                return false;
            }));
            adapter=new Adapter(SelectPhotoActivity.this,mImgs,mCurrentDir.getAbsolutePath());
            mGridView.setAdapter(adapter);
            mTvDirCount.setText(mImgs.size()+"");
            mTvDirName.setText(folderBean.getName());
            mImageDirPopupWindow.dismiss();
        });
    }

    /**
     * This function initializes the click events on this interface.
     */
    private void initEvent() {
        mbottomLayout.setOnClickListener(v -> {
            mImageDirPopupWindow.setAnimationStyle(R.style.dir_popupwindow_anim);
            mImageDirPopupWindow.showAsDropDown(mbottomLayout,0,0);
            lightOff();
        });

        mBtnSelect.setOnClickListener(v -> {
            for (String filename: adapter.selectPhoto())
            {
                filename = UUID.randomUUID().toString();
                fileNameList.add(filename + ".jpg");
            }
            photoSelect.addAll(adapter.selectPhoto());
            switch(label){
                case "top":
                    thisActivity.toNextActivityWithList(TopActivity.class,phoneNumber,photoSelect,fileNameList);
                    break;
                case "skirt":
                    thisActivity.toNextActivityWithList(SkirtActivity.class,phoneNumber,photoSelect,fileNameList);
                    break;
                case "pant":
                    thisActivity.toNextActivityWithList(PantActivity.class,phoneNumber,photoSelect,fileNameList);
                    break;
            }
            finish();
            overridePendingTransition(0, 0);
        });
    }

    /**
     * This function loads the image data on the photo management view.
     */
    private void dataToView() {
        if (mCurrentDir==null){
            Toast.makeText(this,getString(R.string.no_image_scanned), Toast.LENGTH_LONG).show();
            return;
        }
        mImgs= Arrays.asList(mCurrentDir.list());
        adapter = new Adapter(this,mImgs,mCurrentDir.getAbsolutePath());
        mGridView.setAdapter(adapter);
        adapter.setClear();
        mTvDirName.setText(mCurrentDir.getName());
        mTvDirCount.setText(mMaxCount+"");
    }
    /**
     * This function makes the content light on.
     */
    private void lightOn() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha=1.0f;
        getWindow().setAttributes(lp);
    }

    /**
     * This function makes the content light off.
     */
    private void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha=0.3f;
        getWindow().setAttributes(lp);
    }
    /**
     * This function uses Contentprovider to scan the whole images on the user's phone.
     */
    private void initDatas() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(SelectPhotoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SelectPhotoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else{
                aboutScanPhoto();
            }
        }else{
            aboutScanPhoto();
        }

    }

    /**
     * This function is scanning the images on the user's phone.
     */
    private void aboutScanPhoto() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this,getString(R.string.sd_card_not_available), Toast.LENGTH_LONG);
            return;
        }
        mProgressDialog= ProgressDialog.show(this,null,getString(R.string.loading));
        new Thread(){
            @Override
            public void run() {
                Uri mImgUri= MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver cr = SelectPhotoActivity.this.getContentResolver();
                Cursor mCursor = cr.query(mImgUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[] { "image/jpeg", "image/png" },
                        MediaStore.Images.Media.DATE_MODIFIED);
                Set<String> mDirPaths=new HashSet<String>();
                while (mCursor.moveToNext()){
                    String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile=new File(path).getParentFile();
                    if (parentFile==null) {
                        continue;
                    }
                    String dirPath = parentFile.getAbsolutePath();
                    FolderBean folderBean = null;
                    if (mDirPaths.contains(dirPath)){
                        continue;
                    }else{
                        mDirPaths.add(dirPath);
                        folderBean=new FolderBean();
                        folderBean.setDir(dirPath);
                        folderBean.setFirstImamgPath(path);
                    }

                    if (parentFile.list()==null){
                        continue;
                    }
                    int picSize=parentFile.list((dir, name) -> {
                        if (name.endsWith(".jpg")||name.endsWith("jpeg")||name.endsWith("png")){
                            return  true;
                        }
                        return false;
                    }).length;
                    folderBean.setCount(picSize);
                    mFolderBeans.add(folderBean);

                    if (picSize>mMaxCount){
                        mMaxCount=picSize;
                        mCurrentDir=parentFile;
                    }
                }
                mCursor.close();
                handler.sendEmptyMessage(0x110);

            }
        }.start();
    }

    /**
     * This function initializes kinds of view on the activity.
     */
    private void initView() {
        mGridView= findViewById(R.id.id_gridView);
        mbottomLayout= findViewById(R.id.rl_bottom_layout);
        mTvDirName= findViewById(R.id.tv_dir_name);
        mTvDirCount= findViewById(R.id.tv_dir_count);
        mBtnSelect= findViewById(R.id.btn_sure);
    }

    /**
     * This function checks the permission of whether allowes the app visit the user's phone.
     * @param requestCode The request code passed in requestPermissions
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either
     *                     PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    aboutScanPhoto();
                }else {
                    Toast.makeText(this, getString(R.string.please_open_permission), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    /**
     * This function allows the app go back to the previous view.
     * @param view The view plans to go back.
     */
    public void goBack(View view) {
        thisActivity.toNextActivityWithList(TopActivity.class,phoneNumber,photoSelect,fileNameList);
        overridePendingTransition(0, 0);
        finish();
    }
}
