package com.kas4.tinybox.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.LinkedList;

/**
 * author: zjerry created on: 2018/7/2 下午3:07 description:
 */
public class SectionProgressBar extends View {

    private static final long DEFAULT_DRAW_CUSOR_INTERNAL = 500;

    private static final float DEFAULT_CURSOR_WIDTH = 1f;

    private static final float DEFAULT_BREAK_POINT_WIDTH = 4f;

    private static final long DEFAULT_FIRST_POINT_TIME = 5 * 1000;

    private static final long DEFAULT_TOTAL_TIME = 30 * 1000;

    private final LinkedList<BreakPointInfo> mBreakPointInfoList = new LinkedList<>();

    private Paint mBackgroundPaint;

    private Paint mCursorPaint;

    private Paint mProgressBarPaint;

    private Paint mFirstPointPaint;

    private Paint mBreakPointPaint;

    private boolean mIsCursorVisible = true;

    private float mPixelUnit;

    private float mFirstPointTime = DEFAULT_FIRST_POINT_TIME;

    private float mTotalTime = DEFAULT_TOTAL_TIME;

    private volatile State mCurrentState = State.PAUSE;

    private float mPixelsPerMilliSecond;

    private float mProgressWidth;

    private long mLastUpdateTime;

    private long mLastCursorUpdateTime;

    private boolean mNeedDrawCursor=false;

    /**
     * The enum State.
     */
    public enum State {
        /**
         * Start state.
         */
        START,
        /**
         * Pause state.
         */
        PAUSE
    }

    /**
     * Set the progress bar's color.
     */
    public void setBarColor(int color) {
        mProgressBarPaint.setColor(color);
    }

    /**
     * Instantiates a new Progress view.
     *
     * @param context the context
     */
    public SectionProgressBar(Context context) {
        super(context);
        init(context);
    }

    /**
     * Instantiates a new Progress view.
     *
     * @param paramContext the param context
     * @param paramAttributeSet the param attribute set
     */
    public SectionProgressBar(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init(paramContext);

    }

    /**
     * Instantiates a new Progress view.
     *
     * @param paramContext the param context
     * @param paramAttributeSet the param attribute set
     * @param paramInt the param int
     */
    public SectionProgressBar(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init(paramContext);
    }

    private void init(Context paramContext) {
        mCursorPaint = new Paint();
        mProgressBarPaint = new Paint();
        mFirstPointPaint = new Paint();
        mBreakPointPaint = new Paint();

        // setBackgroundColor(Color.parseColor("#4DFFFFFF"));

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBackgroundPaint.setColor(Color.parseColor("#4DFFFFFF"));

        mProgressBarPaint.setStyle(Paint.Style.FILL);
        mProgressBarPaint.setColor(Color.parseColor("#FFFF2D55"));

        mCursorPaint.setStyle(Paint.Style.FILL);
        mCursorPaint.setColor(Color.parseColor("#FFFF2D55"));

        mFirstPointPaint.setStyle(Paint.Style.FILL);
        mFirstPointPaint.setColor(Color.parseColor("#E6FFCC00"));

        mBreakPointPaint.setStyle(Paint.Style.FILL);
        mBreakPointPaint.setColor(Color.parseColor("#ffffff"));

        setTotalTime(paramContext, DEFAULT_TOTAL_TIME);
    }

    /**
     * Reset.
     */
    public synchronized void reset() {
        setCurrentState(State.PAUSE);
        mBreakPointInfoList.clear();
    }

    /**
     * Sets first point time.
     *
     * @param millisecond the millisecond
     */
    public void setFirstPointTime(long millisecond) {
        mFirstPointTime = millisecond;
    }

    /**
     * Sets total time in millisecond
     *
     * @param context the context
     * @param millisecond the millisecond
     */
    public void setTotalTime(Context context, long millisecond) {
        mTotalTime = millisecond;

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mPixelUnit = dm.widthPixels / mTotalTime;

        mPixelsPerMilliSecond = mPixelUnit;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthPixels=getWidth();
        mPixelUnit = widthPixels / mTotalTime;
        mPixelsPerMilliSecond = mPixelUnit;
    }

    /**
     * Sets current state
     *
     * @param state the state
     */
    public void setCurrentState(State state) {
        mCurrentState = state;
        if (state == State.PAUSE) {
            mProgressWidth = mPixelsPerMilliSecond;
        }
    }

    /**
     * Add break point time.
     *
     * @param millisecond the millisecond
     */
    public synchronized void addBreakPointTime(long millisecond) {
        BreakPointInfo info = new BreakPointInfo(millisecond, mProgressBarPaint.getColor());
        mBreakPointInfoList.add(info);
    }

    /**
     * Remove last break point.
     */
    public synchronized void removeLastBreakPoint() {
        mBreakPointInfoList.removeLast();
    }

    public synchronized boolean isRecorded() {
        return !mBreakPointInfoList.isEmpty();
    }

    private void drawBackground(Canvas canvas) {
        float mBarHeight = getMeasuredHeight();
        RectF rectF = new RectF();
        rectF.left = getPaddingLeft();
        rectF.top = 0;
        rectF.right = getWidth() - getPaddingRight();
        rectF.bottom = mBarHeight;
        canvas.drawRoundRect(rectF, mBarHeight / 2.f, mBarHeight / 2.f, mBackgroundPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawBreak(canvas);

        long curTime = System.currentTimeMillis();

        // redraw all the break point
        int startPoint = 0;
        if (!mBreakPointInfoList.isEmpty()) {
            startPoint = (int) (mBreakPointInfoList.getLast().getTime() * mPixelUnit);
        }

        // increase the progress bar in start state
        if (mCurrentState == State.START) {
            mProgressWidth += mPixelsPerMilliSecond * (curTime - mLastUpdateTime);
            if (startPoint + mProgressWidth <= getMeasuredWidth()) {
                // drawRoundRect(startPoint, 0, startPoint + mProgressWidth,
                // getMeasuredHeight(),
                // mProgressBarPaint,canvas);
                drawRoundRect(0, 0, startPoint + mProgressWidth, getMeasuredHeight(),
                        mProgressBarPaint, canvas);
            } else {
                // drawRoundRect(startPoint, 0, getMeasuredWidth(), getMeasuredHeight(),
                // mProgressBarPaint,canvas);
                drawRoundRect(0, 0, startPoint + mProgressWidth, getMeasuredHeight(),
                        mProgressBarPaint, canvas);
            }
        } else {
            // by zj
            drawRoundRect(0, 0, startPoint - DEFAULT_BREAK_POINT_WIDTH, getMeasuredHeight(),
                    mProgressBarPaint, canvas);
        }

        if(mNeedDrawCursor)
            drawCursor(canvas,curTime,startPoint);
        drawBreak(canvas);
        mLastUpdateTime = System.currentTimeMillis();

        invalidate();
    }

    private void drawCursor(Canvas canvas, long curTime, int startPoint) {
        // Draw cursor every 500ms
        if (mLastCursorUpdateTime == 0
                || curTime - mLastCursorUpdateTime >= DEFAULT_DRAW_CUSOR_INTERNAL) {
            mIsCursorVisible = !mIsCursorVisible;
            mLastCursorUpdateTime = System.currentTimeMillis();
        }
        if (mIsCursorVisible) {
            if (mCurrentState == State.START) {
                drawRect(startPoint + mProgressWidth, 0,
                        startPoint + DEFAULT_CURSOR_WIDTH + mProgressWidth, getMeasuredHeight(),
                        mCursorPaint, canvas);
            } else {
                drawRect(startPoint, 0, startPoint + DEFAULT_CURSOR_WIDTH, getMeasuredHeight(),
                        mCursorPaint, canvas);
            }
        }
    }

    private void drawBreak(Canvas canvas) {
        int startPoint = 0;
        synchronized (this) {
            if (!mBreakPointInfoList.isEmpty()) {
                float lastTime = 0;
                int color = mProgressBarPaint.getColor();
                for (BreakPointInfo info : mBreakPointInfoList) {
                    mProgressBarPaint.setColor(info.getColor());
                    float left = startPoint;
                    startPoint += (info.getTime() - lastTime) * mPixelUnit;
                    // drawRect(left, 0, startPoint, getMeasuredHeight(), mProgressBarPaint,canvas);
                    drawRect(startPoint-DEFAULT_BREAK_POINT_WIDTH, 0, startPoint ,
                            getMeasuredHeight(), mBreakPointPaint, canvas);
//                    startPoint += DEFAULT_BREAK_POINT_WIDTH;
                    lastTime = info.getTime();
                }
                mProgressBarPaint.setColor(color);
            }

            // draw the first point
//            if (mBreakPointInfoList.isEmpty()
//                    || mBreakPointInfoList.getLast().getTime() <= mFirstPointTime) {

            drawRect(mPixelUnit * mFirstPointTime, 0,
                    mPixelUnit * mFirstPointTime + DEFAULT_BREAK_POINT_WIDTH, getMeasuredHeight(),
                    mFirstPointPaint, canvas);

        }
    }

    void drawRoundRect(float left, float top, float right, float bottom, @NonNull Paint paint,
                       Canvas canvas) {
        RectF r1 = new RectF();
        r1.left = left;
        r1.top = top;
        r1.right = right;
        r1.bottom = bottom;
        canvas.drawRoundRect(r1, getMeasuredHeight() / 2, getMeasuredHeight() / 2, paint);
    }

    void drawRect(float left, float top, float right, float bottom, @NonNull Paint paint,
                  Canvas canvas) {
        RectF r1 = new RectF();
        r1.left = left;
        r1.top = top;
        r1.right = right;
        r1.bottom = bottom;
        canvas.drawRoundRect(r1, 0f, 0f, paint);
    }

    private class BreakPointInfo {
        private long mTime;

        private int mColor;

        public BreakPointInfo(long time, int color) {
            mTime = time;
            mColor = color;
        }

        public void setTime(long mTime) {
            this.mTime = mTime;
        }

        public void setColor(int mColor) {
            this.mColor = mColor;
        }

        public long getTime() {
            return mTime;
        }

        public int getColor() {
            return mColor;
        }
    }
}
