package com.mycloudwear.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mycloudwear.R;
import com.mycloudwear.util.ImageLoader;

import java.util.LinkedList;
import java.util.List;

/**
 * @author kongkongdaren
 * @version 1.0.1
 * @since 15/5/2019
 * Created by Andriod on 26/6/2018
 * The original code was provided by kongkongdaren (https://github.com/kongkongdaren), but in our app
 * we only use part of his code to achieve our function.
 */
public class Adapter extends BaseAdapter {
    private String mDirPath;
    private List<String> mImgPaths;
    private LayoutInflater mInflater;
    private static List<String> mSelectImg= new LinkedList<>();

    /**
     * This construction is used to declare an adapter to contains the information of the images
     * in a folder.
     * @param context  The layout XML file into its corresponding View objects.
     * @param mDatas  The list string stores the paths of the whole inages.
     * @param dirPath  The path of a image.
     */
    public Adapter(Context context, List<String> mDatas, String dirPath) {
        this.mDirPath=dirPath;
        this.mImgPaths=mDatas;
        mInflater= LayoutInflater.from(context);
    }

    /**
     * This function gets the size of the image path list.
     * @return  a integer of its size.
     * @Override
     */
    @Override
    public int getCount() {
        return mImgPaths.size();
    }

    /**
     * This function gets the item from the path string list accouding to its own postion.
     * @param position postion of a specified image item.
     * @return an object of its image item.
     * @Override
     */
    @Override
    public Object getItem(int position) {
        return mImgPaths.get(position);
    }

    /**
     * This function gets the image item ID through its postion in the list.
     * @param position position of a specified image item.
     * @return a ID of its image item.
     * @Override
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * This function sets the opertion in the select view.
     * @param position  the postion of a specified image item.
     * @param convertView the view of whether the image has been choosed.
     * @param parent the container of the views.
     * @return the view contains the selected images.
     * @Override
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Adapter.ViewHolder vh=null;
        if (convertView==null){
            convertView=  mInflater.inflate(R.layout.item,parent,false);
            vh=new Adapter.ViewHolder();
            vh.mImg=convertView.findViewById(R.id.iv_item);
            vh.mSelect=convertView.findViewById(R.id.ib_select);
            convertView.setTag(vh);
        }
        else {
            vh = (Adapter.ViewHolder) convertView.getTag();
        }
        vh.mImg.setImageResource(R.mipmap.default_error);//default_error
        vh.mSelect.setImageResource(R.mipmap.btn_unselected);
        vh.mImg.setColorFilter(null);
        final String filePath=mDirPath+"/"+mImgPaths.get(position);
        ImageLoader.getInStance(3, ImageLoader.Type.LIFO).loadImage(mDirPath+"/"+mImgPaths.get(position),vh.mImg);
        final Adapter.ViewHolder finalVh = vh;
        vh.mImg.setOnClickListener(v -> {
            //If this image has been selected
            if (mSelectImg.contains(filePath)){
                mSelectImg.remove(filePath);
                finalVh.mImg.setColorFilter(null);
                finalVh.mSelect.setImageResource(R.mipmap.btn_unselected);
            }else{
                //If this image has not been selected.
                mSelectImg.add(filePath);
                finalVh.mImg.setColorFilter(Color.parseColor("#77000000"));
                finalVh.mSelect.setImageResource(R.mipmap.btn_selected);
            }

        });
        return convertView;
    }

    /**
     * This function returns which images are selected.
     * @return a string list of selected images if it is consisted.
     */
    public List<String> selectPhoto(){
        if (!mSelectImg.isEmpty()){
            return mSelectImg;
        }
        return null;
    }

    /**
     * This function create an empty string list of image paths.
     */
    public void setClear() {
        mSelectImg = new LinkedList<>();
    }

    /**
     * This function creates a holder of the view.
     */
    private  class  ViewHolder{
        ImageView mImg;
        ImageButton mSelect;
    }
}
