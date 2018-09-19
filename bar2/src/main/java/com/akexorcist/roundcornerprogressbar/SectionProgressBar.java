/*

Copyright 2015 Akexorcist

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package com.akexorcist.roundcornerprogressbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.akexorcist.roundcornerprogressbar.common.BaseRoundCornerProgressBar;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by zjerry on 9/18/18 AD.
 */
public class SectionProgressBar extends BaseRoundCornerProgressBar {

    public SectionProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public SectionProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int initLayout() {
        return R.layout.layout_round_corner_progress_bar;
    }

    @Override
    protected void initStyleable(Context context, AttributeSet attrs) {

    }

    @Override
    protected void initView() {
        initWidget();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void drawProgress(LinearLayout layoutProgress, float max, float progress, float totalWidth,
                                int radius, int padding, int colorProgress, boolean isReverse) {
        GradientDrawable backgroundDrawable = createGradientDrawable(colorProgress);
        int newRadius = radius - (padding / 2);
        backgroundDrawable.setCornerRadii(new float[]{newRadius, newRadius, newRadius, newRadius, newRadius, newRadius, newRadius, newRadius});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            layoutProgress.setBackground(backgroundDrawable);
        } else {
            layoutProgress.setBackgroundDrawable(backgroundDrawable);
        }

        float ratio = max / progress;
        int progressWidth = (int) ((totalWidth - (padding * 2)) / ratio);
        ViewGroup.LayoutParams progressParams = layoutProgress.getLayoutParams();
        progressParams.width = progressWidth;
        layoutProgress.setLayoutParams(progressParams);
    }

    @Override
    protected void onViewDraw() {

    }

    private Paint mFirstPointPaint;
    private Paint mBreakPointPaint;
    private void initWidget(){
        this.setMax(DEFAULT_MAX_PROGRESS);
        this.setRadius((int) dp2px(6f));
        this.setProgressBackgroundColor(Color.parseColor("#4DFFFFFF"));
        this.setBackgroundColor(Color.parseColor("#00000000"));
        this.setProgressColor(Color.parseColor("#FFFF2D55"));

        mFirstPointPaint = new Paint();
        mFirstPointPaint.setStyle(Paint.Style.FILL);
        mFirstPointPaint.setColor(Color.parseColor("#E6FFCC00"));

        mBreakPointPaint = new Paint();
        mBreakPointPaint.setStyle(Paint.Style.FILL);
        mBreakPointPaint.setColor(Color.parseColor("#ffffff"));

        if(mBreakPointInfoList==null)mBreakPointInfoList=new LinkedList<>();
    }
    private static final float DEFAULT_BREAK_POINT_WIDTH = 4f;

    private float mFirstPointProgress = -1f;
    private void drawBreak(Canvas canvas){
        float viewWidth=getLayoutWidth() - (getPadding() * 2);
        synchronized (this) {
            if(mBreakPointInfoList==null)mBreakPointInfoList=new LinkedList<>();
            if (!mBreakPointInfoList.isEmpty()) {
                for (BreakPointInfo info : mBreakPointInfoList) {
                    float ratio = getMax() / info.getProgress();
                    int progressWidth = (int) (viewWidth / ratio);
                    mBreakPointPaint.setColor(info.getColor());
                    drawRect(progressWidth, 0,
                            progressWidth + DEFAULT_BREAK_POINT_WIDTH, getMeasuredHeight(),
                            mBreakPointPaint, canvas);
                }
            }

            if(mFirstPointProgress>0){
                float ratio = getMax() / mFirstPointProgress;
                int progressWidth = (int) (viewWidth / ratio);
                drawRect(progressWidth, 0,
                        progressWidth + DEFAULT_BREAK_POINT_WIDTH, getMeasuredHeight(),
                        mFirstPointPaint, canvas);
            }

        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawBreak(canvas);
    }

    void drawRect(float left, float top, float right, float bottom, Paint paint,
                  Canvas canvas) {
        RectF r1 = new RectF();
        r1.left = left;
        r1.top = top;
        r1.right = right;
        r1.bottom = bottom;
        canvas.drawRoundRect(r1, 0f, 0f, paint);
    }
    private class BreakPointInfo {
        public float getProgress() {
            return mProgress;
        }

        public void setProgress(float progress) {
            mProgress = progress;
        }

        public int getColor() {
            return mColor;
        }

        public void setColor(int color) {
            mColor = color;
        }

        private float mProgress;
        private int mColor;
        public BreakPointInfo(float progress, int color) {
            mProgress = progress;
            mColor = color;
        }
    }


    public void setFirstPointProgress(float firstPointProgress) {
        mFirstPointProgress = firstPointProgress;
        invalidate();
    }
    public void setTotalProgress(int allProgress){
        this.setMax(allProgress);
        invalidate();
    }

    private LinkedList<BreakPointInfo> mBreakPointInfoList ;
    public synchronized void addBreakPoint() {
        BreakPointInfo info = new BreakPointInfo(getProgress(), mBreakPointPaint.getColor());
        mBreakPointInfoList.add(info);
        invalidate();
    }
    public synchronized void reset() {
        mBreakPointInfoList.clear();
        setProgress(0f);
        mCurProgress=0f;
        invalidate();
    }
    public synchronized void doDelete() {
        if(mBreakPointInfoList.size()>0)
            mBreakPointInfoList.removeLast();
        if(mBreakPointInfoList.size()>0)
        {
            float progress=mBreakPointInfoList.peekLast().getProgress();
            setProgress(progress);
            mCurProgress=progress;
        }else{
            reset();
            return;
        }
        invalidate();
    }
    private float mCurProgress=0.f;
    public static final float MAX_TIME=30;
    public static final float DEFAULT_MAX_PROGRESS=MAX_TIME*10;
    static final float progressSpan=1f;
    public static final int TIME_MULTI=10;

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public float getSpeed() {
        return mSpeed;
    }

    float mSpeed=1f;

    final int STATE_PAUSE=0;
    final int STATE_PLAY=1;
    int mState=STATE_PAUSE;

    private Timer mRecordTimer;
    public void doPlay() {
        if (mRecordTimer == null) {
            mRecordTimer = new Timer();
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                SectionProgressBar.this.post(new Runnable() {
                    @Override
                    public void run() {
                        updateProgressBar();
                    }
                });
            }
        };
        mRecordTimer.schedule(task, 0, 100);
        mState=STATE_PLAY;
    }
    private void updateProgressBar(){
        mCurProgress += progressSpan / mSpeed;
        setProgress(mCurProgress);
    }

    public void doPause() {
        if (mRecordTimer != null) {
            mRecordTimer.cancel();
            mRecordTimer = null;
        }
        this.addBreakPoint();
        mState=STATE_PAUSE;
    }
    public void togglePlayPause(){
        if(mState==STATE_PAUSE){
            doPlay();
        }else{
            doPause();
        }
    }

}
