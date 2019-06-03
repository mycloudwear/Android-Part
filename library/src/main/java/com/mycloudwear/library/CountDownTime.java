package com.mycloudwear.library;

import android.os.CountDownTimer;
import android.widget.Button;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class is used to count down time.
 */
public class CountDownTime extends CountDownTimer {

    private Button timeBtn;
    /**
     * Constructor
     * @param millisInFuture the total timing duration
     * @param countDownInterval the timing interval
     * @param timeBtn the button.
     */
    public CountDownTime(long millisInFuture, long countDownInterval, Button timeBtn) {
        super(millisInFuture, countDownInterval);
        this.timeBtn = timeBtn;
    }

    /**
     * This function is used to display the remain seconds once it is invoked.
     * @param l milliseconds.
     */
    @Override
    public void onTick(long l) {
        timeBtn.setClickable(false);
        timeBtn.setText("Resend after " + l/1000 + " s");
        timeBtn.setEms(5);
    }

    /**
     * This function is used to recover the text when finish.
     */
    @Override
    public void onFinish() {
        timeBtn.setClickable(true);
        timeBtn.setText("Send");
    }
}

