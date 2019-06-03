package com.mycloudwear.library;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class could is used to make a thread.
 */
public class MyThread extends Thread{

    public String result = null;
    public String location;

    // Construction.
    public MyThread(String location){
        this.location = location;
    }

    /**
     * Get the translation result.
     */
    public void run() {
        try{
            result = TranslateOnline.translateToEnglish(location);
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
