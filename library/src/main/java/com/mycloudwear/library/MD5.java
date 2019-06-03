package com.mycloudwear.library;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author wangjingtao
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used for MD5 encryption algorithm and be given by Baidu.
 */

public class MD5 {
    // Firstly initialize an array of characters to hold each hexadecimal character.
    private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    /**
     * Get the MD5 value of a string
     * 
     * @param input the input string.
     * @return MD5 value
     * 
     */
    public static String md5(String input) {
        if (input == null)
            return null;

        try {
            // Get an MD5 converter.
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // Convert the input string to a byte array.
            byte[] inputByteArray = input.getBytes("utf-8");
            // inputByteArray is an array of bytes from the input string conversion.
            messageDigest.update(inputByteArray);
            // Convert and return the result, also a byte array containing 16 elements.
            byte[] resultByteArray = messageDigest.digest();
            // Convert character array to string return.
            return byteArrayToHex(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e){
            return null;
        }
    }

    /**
     * This function is transform byte array into hex digits.
     * @param byteArray the given array.
     * @return a string.
     */
    private static String byteArrayToHex(byte[] byteArray) {
        // create a new array.
        char[] resultCharArray = new char[byteArray.length * 2];
        /*
         * Traversing a byte array, passing a bitwise operation,
         * and converting it into a character array
         */
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }

        // Combine the characters as a string.
        return new String(resultCharArray);

    }

}
