package com.mycloudwear.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author kongkongdaren
 * @version 1.0.1
 * @since 15/5/2019
 * Created by Android on 26/6/2018
 * The original code was provided by kongkongdaren (https://github.com/kongkongdaren), but in our app
 * we only use part of his code to achieve our function.
 */
public class ImageLoader {

    private static ImageLoader mInStance;
    //The core object of the picture cache
    private LruCache<String,Bitmap> mLruCache;
    //The thread pool of the image service.
    private ExecutorService mThreadPool;
    private  static  final  int DEAFULT_THREAD_COUNT=1;
    //set the scheduling strategy of queue is LIFO
    private  Type mType= Type.LIFO;
    //The linked list is queue for tasks.
    private LinkedList<Runnable> mTaskQueue;
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    //It is the handler in the thread of UI.
    private Handler mUIHandler;
    private Semaphore mSemaphorePoolThreadHandler=new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;
    public  enum  Type{
        FIFO,LIFO;
    }

    /**
     * This constructor is used to declare a loader of images.
     * @param threadCount The count of different threads.
     * @param type The trpe of the thread.
     */
    public ImageLoader(int threadCount, Type type) {
          init(threadCount,type);
    }

    /**
     * This function initializes the thread.
     * @param threadCount The count of different threads.
     * @param type The type of the thread.
     */
    private void init(int threadCount, Type type) {
        //The backend starts to polling threads.
        mPoolThread=new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler=new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        //take a thread from the thread pool and execute it.
                        mThreadPool.execute(getTask());
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //if this thread is finished, release a signal to the handler.
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();
        //Get the maximum available memory for our service.
        int maxMemory= (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache=new LruCache<String,Bitmap>(cacheMemory){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight();
            }
        };
            mThreadPool= Executors.newFixedThreadPool(threadCount);
            mTaskQueue=new LinkedList<>();
            mType=type==null? Type.LIFO:type;
            mSemaphoreThreadPool=new Semaphore(threadCount);

    }

    /**
     * This function gets a task through the task queue.
     * @return if this task is existed, retuen this specified task.
     */
    private Runnable getTask() {
        if (mType== Type.FIFO){
            return mTaskQueue.removeFirst();
        }else if (mType== Type.LIFO){
            return mTaskQueue.removeLast();
        }
        return  null;
    }

    /**
     * This function instantiates an empty loader of the image.
     * @return an image loader.
     */
    public  static  ImageLoader getInStance(){
        if (mInStance==null){
            synchronized (ImageLoader.class){
                if (mInStance==null){
                    mInStance=new ImageLoader(DEAFULT_THREAD_COUNT, Type.LIFO);
                }
            }
        }
        return  mInStance;
    }

    /**
     * This function instantiates a image loader with a specific thread.
     * @param threadCount The count of different threads.
     * @param type The type of the thread.
     * @return an image loader.
     */
    public  static  ImageLoader getInStance(int threadCount,Type type){
        if (mInStance==null){
            synchronized (ImageLoader.class){
                if (mInStance==null){
                    mInStance=new ImageLoader(threadCount,type);
                }
            }
        }
        return  mInStance;
    }

    /**
     * This function sets the image of each imageView throw the path of the image.
     * @param path  This string is the path of a specific image.
     * @param imageView  This view is a imageView which will be used.
     */
    public  void loadImage(final String path, final ImageView imageView){
        imageView.setTag(path);
        if (mUIHandler==null){
            mUIHandler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //Get the image and set its imageView.
                    ImgBeanHolder holder= (ImgBeanHolder) msg.obj;
                    Bitmap bitmap = holder.bitmap;
                    ImageView imageview= holder.imageView;
                    String path = holder.path;
                    if (imageview.getTag().toString().equals( path)){
                        imageview.setImageBitmap(bitmap);
                    }
                }
            };
        }
        //Get the bitmap through the path in the cache.
        Bitmap bm=getBitmapFromLruCache(path);
        if (bm!=null){
            refreshBitmap(bm, path, imageView);
        }else{
            addTask(() -> {
                //Get the size of the image which wants to be displayed.
                ImageSize imageSize=  getImageViewSize(imageView);
                //Compress the image.
                Bitmap bm1 =decodeSampledBitmapFromPath(imageSize.width,imageSize.height,path);
                //Put the image into the cache.
                addBitmapToLruCache(path, bm1);
                //Refresh the display of the image.
                refreshBitmap(bm1, path, imageView);
                mSemaphoreThreadPool.release();
            });
        }

    }

    /**
     * This function refreshes the bitmap on the imageView.
     * @param bm The bitmap of the image.
     * @param path The stored path of the image.
     * @param imageView The imageView which contains this image.
     */
    private void refreshBitmap(Bitmap bm, String path, ImageView imageView) {
        Message message = Message.obtain();
        ImgBeanHolder holder=new ImgBeanHolder();
        holder.bitmap=bm;
        holder.path=path;
        holder.imageView=imageView;
        message.obj=holder;
        mUIHandler.sendMessage(message);
    }

    /**
     * This function puts the image into the LruCache.
     * @param path The bitmap of the image.
     * @param bm The stored path of the image.
     */
    private void addBitmapToLruCache(String path, Bitmap bm) {
        if (getBitmapFromLruCache(path)==null){
            if (bm!=null){
                mLruCache.put(path,bm);
            }
        }
    }

    /**
     * This function compresses the image according to its width and height when displaying.
     * @param width The width of original image.
     * @param height The height of original image.
     * @param path The stored path of the image.
     * @return The bitmap after compress.
     */
    private Bitmap decodeSampledBitmapFromPath(int width, int height, String path) {
        //Get the width and the height of the image and stored it into the cache.
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(path,options);

        //Use InSampleSize decodes the image again.
        options.inSampleSize=caculateInSampleSize(options,width,height);
        options.inJustDecodeBounds=false;
        Bitmap bitmap= BitmapFactory.decodeFile(path,options);
        return  bitmap;

    }

    /**
     * This function calculate the sample size according to its target and original width and height.
     * @param options This param decodes and creates a new bitmap.
     * @param width The width of original image.
     * @param height The width of original image.
     * @return a new sample size of the image.
     */
    private int caculateInSampleSize(BitmapFactory.Options options, int width, int height) {
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int inSampleSize=1;
        if (outWidth>width||outHeight>height){
            int widthRadio= Math.round(outWidth*1.0f/width);
            int heightRadio= Math.round(outHeight*1.0f/height);
            inSampleSize= Math.max(widthRadio,heightRadio);
        }

        return inSampleSize;
    }

    /**
     * This function gets the suitable width and heught after the compress according the ImageView.
     * @param imageView The imageView of the target image container.
     * @return the suitable ImageSize.
     */
    private ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imageSize=new ImageSize();

        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        //Get the actual with of the imageView.
        int width = imageView.getWidth();
        if (width <=0){
            //Get the imageView and declare the width of the layout.
            width=lp.width;
        }
        if (width<=0){
            //Check the maximum width of the imageView.
            width= getImageViewFieldValue(imageView,"mMaxWidth");
        }
        if (width<=0){
            width=displayMetrics.widthPixels;
        }
        //Get the actual height of the imageView.
        int height = imageView.getHeight();

        if (height <=0){
            //Get the imageView and declare the height of the layout.
            height=lp.height;
        }
        if (height<=0){
            //Check the maximum height of the imageView.
            height=getImageViewFieldValue(imageView,"mMaxHeight");
        }
        if (height<=0){
            height=displayMetrics.heightPixels;
        }
        imageSize.width=width;
        imageSize.height=height;
        return imageSize;
    }

    /**
     * This function gets a specific attribute value of the imageView by reflection.
     * @return The attribute value.
     */
    private  static  int getImageViewFieldValue(Object object, String fieldName){
        int value=0;

        try {
            Field field= ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue>0&&fieldValue< Integer.MAX_VALUE){
                value=fieldValue;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return  value;
    }

    /**
     * This function adds a new task thread into the thread pool.
     * @param runnable A interface of the thread.
     */
    private  synchronized void addTask(Runnable runnable)  {
        mTaskQueue.add(runnable);
        try {
            if (mPoolThreadHandler==null){
                mSemaphorePoolThreadHandler.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    /**
     * This function sets the imageView throw its path.
     * @param key The path of the image.
     * @return  The bitmap of this image in the imageView.
     */
    private Bitmap getBitmapFromLruCache(String key) {
         return mLruCache.get(key);
    }

    /**
     * This constructor declares the size of the image.
     */
    private  class  ImageSize{
       int width;
       int height;
    }
    /**
     * This constructor declares the holder of the image bean.
     */
    private class ImgBeanHolder{
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }
}
