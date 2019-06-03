package com.mycloudwear.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mycloudwear.R;
import com.mycloudwear.util.ImageLoader;

import java.util.List;

/**
 * @author kongkongdaren
 * @version 1.0.1
 * @since 15/5/2019
 * Created by Android on 26/6/2018
 * The original code was provided by kongkongdaren (https://github.com/kongkongdaren), but in our app
 * we only use part of his code to achieve our function.
 * This class is an easy adapter to map static data to views defined in an XML file.
 */
public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.MyViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<String> mDatas;

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);

    }
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    /**
     * This constructor declares this simple adapter.
     * @param mContext The context where the View associated with this SimpleAdapter is running
     * @param mDatas A List of Maps. Each entry in the List corresponds to one row in the list.
     */
    public SimpleAdapter(Context mContext, List<String> mDatas) {
        this.mContext = mContext;
        this.mDatas = mDatas;
        mInflater= LayoutInflater.from(mContext);
    }

    /**
     * This method is called right when the adapter is created and is used to initialize your ViewHolder.
     * @param parent A special view that can contain other views
     * @param viewType The type of the view.
     * @return the viewHolder.
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item, parent, false);
        MyViewHolder viewHolder=new MyViewHolder(view);
        return viewHolder;
    }

    /**
     * This function called by RecyclerView to display the data at the specified position.
     * @param holder  The ViewHolder which should be updated to represent the contents of the item
     *                at the given position in the data set
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        ImageLoader.getInStance(3, ImageLoader.Type.LIFO).loadImage(mDatas.get(position),holder.iv);
        setUpOnClicl(holder);
    }

    /**
     * This function is used to set up onClick listener.
     * @param holder an object of the MyViewHolder.
     */
    public void setUpOnClicl(final MyViewHolder holder) {
        if (mOnItemClickListener!=null){
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getLayoutPosition();
                mOnItemClickListener.onItemClick(holder.itemView,pos);
            });
            holder.itemView.setOnLongClickListener(v -> {
                int pos = holder.getLayoutPosition();
                mOnItemClickListener.onItemLongClick(holder.itemView,pos);
                return false;
            });
        }
    }

    /**
     * This function can get the path of the image.
     * @param pos the current position.
     * @return the path of the current image.
     */
    public String getPath(int pos){
        return mDatas.get(pos);
    }

    /**
     * This function is used to delete the image.
     * @param pos the position of the image.
     */
    public void deleteData(int pos) {
        notifyItemRemoved(pos);
    }

    /**
     * This function is used to get the total number of the photos.
     * @return the size of the images.
     */
    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    /**
     * This class is used to achieve the recycle view.
     */
    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        public MyViewHolder(View itemView) {
            super(itemView);
            iv=  itemView.findViewById(R.id.iv_photo);
        }
    }
}
