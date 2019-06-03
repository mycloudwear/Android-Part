package com.mycloudwear.library;

import com.github.promeg.pinyinhelper.Pinyin;

/**
 * @author linqianyue
 * @version 1.0.1
 * @since 14/5/2019
 * Created by android on 17/10/17.
 * The original code was provided by linqianyue (https://github.com/sahooz) but in our app we
 * only use part of his code to achieve our function.
 */
public class PinyinUtil {
    /**
     * This function could translate other-language string into simplified Chinese.
     * @param inputString the given string.
     * @return the Chinese characters.
     */
    public static String getPingYin(String inputString) {
        try {
            return Pinyin.toPinyin(inputString, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
