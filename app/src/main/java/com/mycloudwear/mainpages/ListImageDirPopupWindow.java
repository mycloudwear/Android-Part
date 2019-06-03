package com.mycloudwear.mainpages;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mycloudwear.R;
import com.mycloudwear.bean.FolderBean;
import com.mycloudwear.util.ImageLoader;


import java.util.List;

/**
 * @author kongkongdaren
 * @version 1.0.1
 * @since 15/5/2019
 * Created by Android on 26/6/2018
 * The original code was provided by kongkongdaren (https://github.com/kongkongdaren), but in our app
 * we only use part of his code to achieve our function.
 * This class creates a interface which allows the user switching to different folders when choosing images.
 */

public class ListImageDirPopupWindow extends PopupWindow {
    private  int mWidth;
    private int mHeight;
    private View mConvertView;
    private List<FolderBean> mDatas;
    private ListView mListView;

    public  interface  OnDirSelectedListener{
        void onSelected(FolderBean folderBean);
    }
    public  OnDirSelectedListener mListener;

    public void setOnDirSelectedListener(OnDirSelectedListener mListener) {
        this.mListener = mListener;
    }

    /**
     * This constructor declares the ListImageDirPopupWindow
     * @param context the current context.
     * @param datas The list of the folders.
     */
    public ListImageDirPopupWindow(Context context, List<FolderBean> datas) {
       calWidthAndHeight(context);
        mConvertView= LayoutInflater.from(context).inflate(R.layout.popup_main,null);
        mDatas=datas;
        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());

        setTouchInterceptor((v, event) -> {

            if (event.getAction()== MotionEvent.ACTION_OUTSIDE){
                dismiss();
                return true;
            }
            return false;
        });
        initViews(context);
        initEvent();
    }

    /**
     * This function initializes the list view of the folder.
     * @param context The pop up list of the name of folders which are existed.
     */
    private void initViews(Context context) {
        mListView=  mConvertView.findViewById(R.id.lv_dir);
        mListView.setAdapter(new ListDirAdapter(context,mDatas));
    }

    /**
     * This function initializes the click event of the pop up window.
     */
    private void initEvent() {
       mListView.setOnItemClickListener((parent, view, position, id) -> {
           if (mListener!=null){
               mListener.onSelected(mDatas.get(position));
           }
       });
    }

    /**
     * This function calculates the width and the height of the pop up window according to its layout.
     * @param context The pop up list of the name of folders which are existed.
     */
    private void calWidthAndHeight(Context context) {

        WindowManager wm= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mWidth= outMetrics.widthPixels;
        mHeight= (int) (outMetrics.heightPixels*0.7);

    }
    /**
     * This function creates a adapter of the list at the pop up window.
     */
    private class  ListDirAdapter extends ArrayAdapter<FolderBean> {

        private LayoutInflater mInflater;
        private List<FolderBean> mDatas;

        public ListDirAdapter(@NonNull Context context, List<FolderBean> datas) {
            super(context, 0, datas);
            mInflater= LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder vh;
            if (convertView==null){
                vh=new ViewHolder();
                convertView=  mInflater.inflate(R.layout.popup_item,parent,false);
                vh.mDirName = convertView.findViewById(R.id.tv_dir_item_name);
                vh.mDirCount = convertView.findViewById(R.id.tv_dir_item_count);
                vh.mImg = convertView.findViewById(R.id.iv_dir_image);
                convertView.setTag(vh);
            }else{
                vh= (ViewHolder) convertView.getTag();
            }
            FolderBean bean = getItem(position);
            //Reset.
            vh.mImg.setImageResource(R.mipmap.default_error);

            ImageLoader.getInStance(3, ImageLoader.Type.LIFO).loadImage(bean.getFirstImamgPath(),vh.mImg);
            vh.mDirName.setText(bean.getName());
            vh.mDirCount.setText(bean.getCount()+"");
            return convertView;
        }

        private  class  ViewHolder{
            ImageView mImg;
            TextView mDirName;
            TextView mDirCount;
        }
    }
}
