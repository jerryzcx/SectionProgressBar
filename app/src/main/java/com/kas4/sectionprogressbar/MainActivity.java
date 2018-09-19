package com.kas4.sectionprogressbar;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.akexorcist.roundcornerprogressbar.SectionProgressBar;


public class MainActivity extends AppCompatActivity {
    Handler mHandler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initSectionProgressBar();
            }
        },200);

        initOtherSectionProgressBar();
        initNew();
    }

    private String[] mLevels = {"铜卡", "银卡", "金卡", "白金卡", "钻卡"};
    private int[] mLevelValues = {0, 1000, 2000, 4000, 8000};
    com.kas4.bar.SectionProgressBar mSectionBar1;
    com.kas4.bar.SectionProgressBar mSectionBar2;
    private void initOtherSectionProgressBar() {
        mSectionBar1 = findViewById(R.id.section_1);
        mSectionBar2 =  findViewById(R.id.section_2);
        mSectionBar2.setLevels(mLevels);
        mSectionBar2.setLevelValues(mLevelValues);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSectionBar1.setCurrent(3000);
                mSectionBar2.setCurrent(3000);
            }
        }, 2000);
    }



    com.kas4.tinybox.widget.SectionProgressBar progressView;
    void initSectionProgressBar() {
        progressView = findViewById(R.id.progressView);
        progressView.setFirstPointTime(5 * 1000);
        progressView.setTotalTime(this, 30 * 1000);
        progressView.reset();


//                progressView.setCurrentState(SectionProgressBar.State.START);

//                progressView.setCurrentState(SectionProgressBar.State.PAUSE);
//                progressView.addBreakPointTime((long) (pauseTime * 1000));

//                progressView.removeLastBreakPoint();

    }


    public void onClick(View view) {
        progressView.setCurrentState(com.kas4.tinybox.widget.SectionProgressBar.State.START);
    }


    SectionProgressBar progressBarTest;
    void initNew(){
        progressBarTest= (SectionProgressBar) findViewById(R.id.progressBarTest);
        progressBarTest.setFirstPointProgress(SectionProgressBar.TIME_MULTI*5);
        progressBarTest.setTotalProgress(SectionProgressBar.TIME_MULTI*30);
    }

    public void onClickTest(View view) {
        progressBarTest.togglePlayPause();
    }

    public void onClickSpeed(View view) {
        if (progressBarTest.getSpeed() == 1.f) progressBarTest.setSpeed(0.33f);
        else progressBarTest.setSpeed(1.f);
    }

    public void onClickDelete(View view) {
        progressBarTest.doDelete();
    }
}
