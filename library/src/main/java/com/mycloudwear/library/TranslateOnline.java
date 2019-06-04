package com.mycloudwear.library;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This interface contains our app_id and key.
 */
public interface TranslateOnline {

    String APP_ID = "Your app id";
    String SECURITY_KEY = "Your security key";

    static String translateToEnglish(String input){
        TransApi api = new TransApi(APP_ID, SECURITY_KEY);

        String result = api.getTransResult(input, "zh", "en");
        int startPoint = result.indexOf("dst") + 6;
        int endPoint = result.indexOf("\"}]}");
        return result.substring(startPoint, endPoint);
    }
}
