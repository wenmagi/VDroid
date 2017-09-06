/*
* Copyright (c) 2016 Zhihu Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License
* is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing permissions and limitations under
* the License.
*/
package com.magi.videomodule.video;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class SimpleVideoPlayControllerView extends AbstractVideoPlayControllerView {

    private static final float TRACKING_SENSE_DELTA = 0.02f;

    protected ImageView mSmallPlayButton;
    private View mProgressView;
    protected SeekBar mSeekBar;
    protected TextView mDurationTextView;
    protected TextView mCurrentPositionTextView;
    protected View mMiddlePlayButton;
    protected View mBottomPanel;

    private Disposable mSubscription;
    private Observable mObservable;

    private boolean mIsTouchTracking = false;

    private float mCurrentTrackingProgress;

    public SimpleVideoPlayControllerView(@NonNull Context pContext) {
        super(pContext);
        init(pContext);
    }

    public SimpleVideoPlayControllerView(Context pContext, AttributeSet pAttributeSet) {
        super(pContext, pAttributeSet);
        init(pContext);
    }

    public SimpleVideoPlayControllerView(Context pContext, AttributeSet pAttributeSet, int pDefaultStyle) {
        super(pContext, pAttributeSet, pDefaultStyle);
        init(pContext);
    }

    protected void init(Context context) {
        LayoutInflater.from(context).inflate(getLayoutId(), this);
        mMiddlePlayButton = findViewById(R.id.middle_play_button);
        mProgressView = findViewById(R.id.progress);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mBottomPanel = findViewById(R.id.bottom_panel);
        mDurationTextView = (TextView) findViewById(R.id.duration);
        mCurrentPositionTextView = (TextView) findViewById(R.id.current_position);

        //optional widgets
        mSmallPlayButton = (ImageView) findViewById(R.id.small_play_button);

        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setEnabled(false); // 刚进来时先禁用，防止视频信息未读取到就拖动出现异常

        RxClicks.onClick(this, this);
        mMiddlePlayButton.setOnClickListener(this);
        mSmallPlayButton.setOnClickListener(this);
    }

    public void show() {
        mBottomPanel.animate().cancel();
        mBottomPanel.animate().translationY(0).setDuration(250).start();
    }

    public void hide() {
        mBottomPanel.animate().cancel();
        mBottomPanel.animate().translationY(mBottomPanel.getHeight()).setDuration(250).start();
    }

    private void updateTime() {
        if (videoControllerListener != null && !mIsTouchTracking) {
            long currentPosition = videoControllerListener.getCurrentPosition();
            long duration = videoControllerListener.getDuration();
            float progress = duration == 0f ? 0f : currentPosition * 1f / duration;
            mSeekBar.setProgress((int) (progress * mSeekBar.getMax()));
            mCurrentPositionTextView.setText(VideoPlayUtils.stringForTime(currentPosition));
            mDurationTextView.setText(VideoPlayUtils.stringForTime(duration));
            if (duration >= 1000) {
                mSeekBar.setEnabled(true);
            }
        }
    }

    protected boolean isPlaying() {
        return videoControllerListener != null && videoControllerListener.isPlaying();
    }

    @Override
    public void onClick(View v) {
        if (v == this) {
            if (videoControllerListener != null) {
                videoControllerListener.onClickFinished();
            }
        } else {
            switch (v.getId()) {
                case R.id.middle_play_button:
                case R.id.small_play_button:
                    if (videoControllerListener != null)
                        videoControllerListener.onPlayOrStop();
                    break;
                default:
                    break;
            }
        }
    }

    public void showLoading(boolean isLoading) {
        mProgressView.setVisibility(isLoading ? VISIBLE : GONE);
        mMiddlePlayButton.setVisibility(isLoading ? GONE : VISIBLE);
    }

    @Override
    public void onComplete() {
        mSeekBar.setProgress(0);
        mCurrentPositionTextView.setText(VideoPlayUtils.stringForTime(0L));
        onStopPlay();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mIsTouchTracking) {
            long duration = videoControllerListener.getDuration();
            float percent = seekBar.getMax() == 0L ? 0f : progress * 1f / seekBar.getMax();
            mCurrentPositionTextView.setText(VideoPlayUtils.stringForTime((long) (duration * percent)));

            if (Math.abs(percent - mCurrentTrackingProgress) > TRACKING_SENSE_DELTA) {
                mCurrentTrackingProgress = percent;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsTouchTracking = true;
        mCurrentTrackingProgress = 0f;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mIsTouchTracking = false;
        if (videoControllerListener != null) {
            videoControllerListener.onPositionChanged(getSeekBarProgress(seekBar));
        }
    }

    protected float getSeekBarProgress(@NonNull SeekBar seekBar) {
        return seekBar.getProgress() * 1f / seekBar.getMax();
    }

    public void setOnVideoControllerListener(OnVideoControllerListener onVideoControllerListener) {
        videoControllerListener = onVideoControllerListener;
    }

    @Override
    public void onStartPlay() {
        mSmallPlayButton.setImageResource(R.drawable.ic_video_pause_small);

        mMiddlePlayButton.setVisibility(GONE);
        if (mSubscription != null) {
            mSubscription.dispose();
        }
        if (mObservable == null) {
            mObservable = Observable.interval(0L, 50L, TimeUnit.MILLISECONDS, Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .retry();
        }
        mObservable.subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                mSubscription = d;
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Long aLong) {
                updateTime();
            }
        });
    }

    @Override
    public void onStopPlay() {
        mSmallPlayButton.setImageResource(R.drawable.ic_video_play_small);

        updateTime();

        mProgressView.setVisibility(GONE);
        mMiddlePlayButton.setVisibility(VISIBLE);
        if (mSubscription != null) {
            mSubscription.dispose();
        }
    }

    @Override
    public void onError(Throwable error) {
        mSeekBar.setProgress(0);
        mCurrentPositionTextView.setText(VideoPlayUtils.stringForTime(0L));
        onStopPlay();
    }

    protected int getLayoutId() {
        return R.layout.simple_video_play_controller_view;
    }

    public void changeVisibility() {
    }


    public void onOrientationChange(int orientation) {

    }

    public void onVideoSizeChanged(int width, int height, boolean showControllerView) {

    }

    @Override
    public void showMiddlePlayButton(boolean show) {
        mMiddlePlayButton.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void setThumbnail(String thumbnail) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
