package com.mycloudwear.library;

import android.util.Base64;
import java.io.UnsupportedEncodingException;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class could is used to encoder string which contains special characters.
 */
public class Encoder {

    // Construction.
    public Encoder(){}

    /**
     * This function is used to encode a string by base 64.
     * @param info the given string.
     * @return encoded string.
     */
    public String Base64Encode(String info){
        try{
            byte[] encryptedData = info.getBytes("utf-8");
            String encodeBase64 = Base64.encodeToString(encryptedData,Base64.NO_WRAP);
            String safeBase64Str = encodeBase64.replace('+', '-');
            safeBase64Str = safeBase64Str.replace('/', '_');
            safeBase64Str = safeBase64Str.replaceAll("=", "");
            return safeBase64Str;
        }catch(UnsupportedEncodingException e){
            return info;
        }

    }
}
