package com.mycloudwear.library;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class could is used to achieve file operation.
 */
public class FileOperation {

    /**
     * This function is used to write a file.
     * @param conn HTTP connection.
     * @param context current context.
     * @param fileName file name.
     */
    public static void writeFile(HttpURLConnection conn, Context context, String fileName){
        try{
            InputStream is = conn.getInputStream();
            File file = new File(context.getCacheDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);

            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1){
                fos.write(buffer,0,len);
            }

            is.close();
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }

    }
}
