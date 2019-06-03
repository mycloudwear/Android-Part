package com.mycloudwear.library;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author linqianyue
 * @version 1.0.1
 * @since 14/5/2019
 * Created by android on 17/10/17.
 * The original code was provided by linqianyue (https://github.com/sahooz) but in our app we
 * only use part of his code to achieve our function.
 */
public class Country implements PyEntity {

    private static final String TAG = Country.class.getSimpleName();
    public int code;
    public String name, locale, pinyin;
    public int flag;
    private static ArrayList<Country> countries = null;

    /**
     * This construction is used to declare a object by its country code, name, location and flag.
     * @param code the country code.
     * @param name the country name.
     * @param locale the country location.
     * @param flag the country flag.
     */
    public Country(int code, String name, String locale, int flag) {
        this.code = code;
        this.name = name;
        this.flag = flag;
        this.locale = locale;
    }

    /**
     * This function is used to make a json-like string.
     * @return a string in json format.
     */
    @Override
    public String toString() {
        return "Country{" +
                "code='" + code + '\'' +
                "flag='" + flag + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    /**
     * This function is used to get all countries.
     * @param ctx the current context.
     * @param callback the call back exception.
     * @return an ArrayList with countries.
     */
    public static ArrayList<Country> getAll(@NonNull Context ctx, @Nullable ExceptionCallback callback) {
        if(countries != null) return countries;
        countries = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getResources().getAssets().open("code.json")));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null)
                sb.append(line);
            br.close();
            JSONArray ja = new JSONArray(sb.toString());
            String key = getKey(ctx);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                int flag = 0;
                String locale = jo.getString("locale");
                if(!TextUtils.isEmpty(locale)) {
                    flag = ctx.getResources().getIdentifier("flag_" + locale.toLowerCase(), "drawable", ctx.getPackageName());
                }
                countries.add(new Country(jo.getInt("code"), jo.getString(key), locale, flag));
            }

            Log.i(TAG, countries.toString());
        } catch (IOException e) {
            if(callback != null) callback.onIOException(e);
            e.printStackTrace();
        } catch (JSONException e) {
            if(callback != null) callback.onJSONException(e);
            e.printStackTrace();
        }
        return countries;
    }

    /**
     * Make ArrayList empty.
     */
    public static void destroy(){ countries = null; }

    /**
     * This function is used to get the key value from configuration.
     * @param ctx the current context.
     * @return a string which is a abbreviation of countries.
     */
    private static String getKey(Context ctx) {
        String country = ctx.getResources().getConfiguration().locale.getCountry();
        return "CN".equalsIgnoreCase(country)? "zh"
                : "TW".equalsIgnoreCase(country)? "tw"
                : "HK".equalsIgnoreCase(country)? "tw"
                : "en";
    }

    /**
     * This function return a hash code.
     * @return integer format hash code.
     */
    @Override
    public int hashCode() {
        return code;
    }

    /**
     * This function is used to get the PinYIn.
     * @return a string in PinYin format.
     */
    @NonNull @Override
    public String getPinyin() {
        if(pinyin == null) {
            pinyin = PinyinUtil.getPingYin(name);
        }
        return pinyin;
    }
}
