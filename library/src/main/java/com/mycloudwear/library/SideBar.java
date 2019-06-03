package com.mycloudwear.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author linqianyue
 * @version 1.0.1
 * @since 14/5/2019
 * Created by android on 17/10/17.
 * The original code was provided by linqianyue (https://github.com/sahooz) but in our app we
 * only use part of his code to achieve our function.
 */
public class SideBar extends View {

    public final ArrayList<String> indexes = new ArrayList<>();
    private OnLetterChangeListener onLetterChangeListener;
    private Paint paint;
    private float textHeight;
    private int cellWidth;
    private int cellHeight;
    private int currentIndex = -1;
    private int letterColor;
    private int selectColor;
    private int letterSize;

    // Construction.
    public SideBar(Context context) { this(context, null); }

    // Construction.
    public SideBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Construction.
    public SideBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SideBar, defStyleAttr, 0);
        letterColor = ta.getColor(R.styleable.SideBar_letterColor, Color.BLACK);
        selectColor = ta.getColor(R.styleable.SideBar_selectColor, Color.CYAN);
        letterSize = ta.getDimensionPixelSize(R.styleable.SideBar_letterSize, 24);
        ta.recycle();
        paint = new Paint();
        // Anti-aliasing.
        paint.setAntiAlias(true);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        textHeight = (float) Math.ceil(fontMetrics.descent - fontMetrics.ascent);  //1.1---2   2.1--3
        String[] letters = {"A", "B", "C", "D",
                "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
                "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        indexes.addAll(Arrays.asList(letters));
    }

    /**
     * This function is used to make size change.
     * @param w current width.
     * @param h current height.
     * @param oldw previous width.
     * @param oldh previous height.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellWidth = getMeasuredWidth();
        cellHeight = getMeasuredHeight() / indexes.size();
    }

    /**
     * This function is used to draw the texts on canvas.
     * @param canvas an object of Canvas.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setTextSize(letterSize);
        for (int i = 0; i < indexes.size(); i++) {
            String letter = indexes.get(i);
            float textWidth = paint.measureText(letter);
            float x = (cellWidth - textWidth) * 0.5f;
            float y = (cellHeight + textHeight) * 0.5f + cellHeight * i;

            if (i == currentIndex) {
                paint.setColor(selectColor);
            } else {
                paint.setColor(letterColor);
            }

            canvas.drawText(letter, x, y, paint);
        }
    }

    /**
     * This interface is used as listener.
     */
    public interface OnLetterChangeListener {

        void onLetterChange(String letter);
        // Lift finger.
        void onReset();
    }

    /**
     * This function is used to handle the finger movements.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int downY = (int) event.getY();
                // Get current index.
                currentIndex = downY / cellHeight;
                if (currentIndex < 0 || currentIndex > indexes.size() - 1) {

                } else {
                    if (onLetterChangeListener != null) {
                        onLetterChangeListener.onLetterChange(indexes.get(currentIndex));
                    }
                }
                // Repaint.
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) event.getY();
                // Get current index.
                currentIndex = moveY / cellHeight;
                if (currentIndex < 0 || currentIndex > indexes.size() - 1) {

                } else {
                    if (onLetterChangeListener != null) {
                        onLetterChangeListener.onLetterChange(indexes.get(currentIndex));
                    }
                }
                // Repaint.
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                currentIndex = -1;
                // Manual refresh
                invalidate();
                // button release.
                if (onLetterChangeListener != null) {
                    onLetterChangeListener.onReset();
                }
                break;
        }
        return true;
    }
}
