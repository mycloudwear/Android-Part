package com.mycloudwear.library;

import org.json.JSONException;

import java.io.IOException;

/**
 * @author linqianyue
 * @version 1.0.1
 * @since 14/5/2019
 * Created by android on 17/10/17.
 * The original code was provided by linqianyue (https://github.com/sahooz) but in our app we
 * only use part of his code to achieve our function.
 */
public abstract class ExceptionCallback {
    public abstract void onIOException(IOException e);
    public abstract void onJSONException(JSONException e);
}
