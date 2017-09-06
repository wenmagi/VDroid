/*
 * Copyright (c) 2017 Zhihu Inc.
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
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by chenhaohua on 26/6/2017.
 */

public class GestureControlView extends LinearLayout implements IGestureView {

    private static final String TAG = "GestureControlView";

    private static final int UNKNOWN = 0;
    private static final int BRIGHTNESS = 1;
    private static final int VOLUME = 2;
    private static final int SEEK = 3;

    private static final int LARGE_TEXT_SIZE = 16;
    private static final int SMALL_TEXT_SIZE = 14;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private boolean inForward = false;
    private int mCurrentImgResId;

    private LinearLayout mContainer;
    private AppCompatImageView mImageView;
    private HorizontalProgressBar mHorizontalProgressBar;
    private TextView mPositionTextView;
    private int mLargeWidth;
    private int mLargeHeight;
    private int mSmallWidth;
    private int mSmallHeight;
    private int m40dpInPix;
    private int m32dpInPix;
    private int m124dpInPix;
    private int m100dpInPix;
    private int m16dpInPix;
    private int m14dpInPix;
    private int m18dpInPix;

    public GestureControlView(Context context) {
        super(context);
        init();
    }

    public GestureControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        initDimenValues();
        LayoutInflater.from(getContext()).inflate(R.layout.layout_gesture_control, this);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mContainer = findViewById(R.id.gesture_container);
        mImageView = findViewById(R.id.gesture_control_icon);
        mHorizontalProgressBar = findViewById(R.id.gesture_control_progressbar);
        mHorizontalProgressBar.enableFullHeight(true);
        mPositionTextView = findViewById(R.id.seek_position_text_view);
    }

    private void initDimenValues() {
        mLargeWidth = DisplayUtils.dpToPixel(getContext(), 156);
        mLargeHeight = DisplayUtils.dpToPixel(getContext(), 96);
        mSmallWidth = DisplayUtils.dpToPixel(getContext(), 132);
        mSmallHeight = DisplayUtils.dpToPixel(getContext(), 80);
        m40dpInPix = DisplayUtils.dpToPixel(getContext(), 40);
        m32dpInPix = DisplayUtils.dpToPixel(getContext(), 32);
        m124dpInPix = DisplayUtils.dpToPixel(getContext(), 124);
        m100dpInPix = DisplayUtils.dpToPixel(getContext(), 100);
        m16dpInPix = DisplayUtils.dpToPixel(getContext(), 16);
        m14dpInPix = DisplayUtils.dpToPixel(getContext(), 14);
        m18dpInPix = DisplayUtils.dpToPixel(getContext(), 18);
    }

    @Override
    public void changeVolume(int volume, int max) {
        mHorizontalProgressBar.setMax(max);
        mHorizontalProgressBar.setProgress(volume);
    }

    @Override
    public void changeBrightness(float volume, float max) {
        mHorizontalProgressBar.setMax(100);
        mHorizontalProgressBar.setProgress((int) (100 * volume));
    }

    @Override
    public void positionForward(long position, long duration) {
//        if (!inForward)
        if (mCurrentImgResId != R.drawable.ic_icon_forward) {
            mImageView.setImageResource(R.drawable.ic_icon_forward);
            mCurrentImgResId = R.drawable.ic_icon_forward;
        }
        inForward = true;
        updateTime(position, duration);
    }

    private void updateTime(long position, long duration) {
        if (position < 0)
            position = 0;

        if (position > duration)
            position = duration;

        String pendingTime = stringForTime(position);
        String durationTime = stringForTime(duration);
        SpannableString spannableString = new SpannableString(pendingTime + " / " + durationTime);
        spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, pendingTime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mPositionTextView.setText(spannableString);
    }

    @Override
    public void positionBackward(long position, long duration) {
        if (mCurrentImgResId != R.drawable.ic_icon_backward) {
            mImageView.setImageResource(R.drawable.ic_icon_backward);
            mCurrentImgResId = R.drawable.ic_icon_backward;
        }
        inForward = false;
        updateTime(position, duration);
    }

    @Override
    public void changeMode(int type) {
        switch (type) {
            case UNKNOWN:
                mPositionTextView.setVisibility(GONE);
                mHorizontalProgressBar.setVisibility(GONE);
                break;

            case BRIGHTNESS:
                mImageView.setImageResource(R.drawable.ic_icon_brightness);
                mCurrentImgResId = R.drawable.ic_icon_brightness;
                mHorizontalProgressBar.setVisibility(VISIBLE);
                break;

            case VOLUME:
                mImageView.setImageResource(R.drawable.ic_icon_volume);
                mCurrentImgResId = R.drawable.ic_icon_volume;
                mHorizontalProgressBar.setVisibility(VISIBLE);
                break;

            case SEEK:
                mPositionTextView.setVisibility(VISIBLE);
                break;
        }
    }

    private String stringForTime(long timeMs) {

        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        return hours > 0 ? mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : mFormatter.format("%02d:%02d", minutes, seconds).toString();
    }

    //在展示前调用，横屏模式下会比较大
    public void resize() {

        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        ViewGroup.LayoutParams containerLayoutParams = mContainer.getLayoutParams();
        containerLayoutParams.width = isLandscape ? mLargeWidth : mSmallWidth;
        containerLayoutParams.height = isLandscape ? mLargeHeight : mSmallHeight;
        mContainer.setLayoutParams(containerLayoutParams);

        LayoutParams imageViewLayoutParams = (LayoutParams) mImageView.getLayoutParams();
        imageViewLayoutParams.height = imageViewLayoutParams.width = isLandscape ? m40dpInPix : m32dpInPix;
        imageViewLayoutParams.setMargins(0, isLandscape ? m18dpInPix : m16dpInPix, 0, 0);
        mImageView.setLayoutParams(imageViewLayoutParams);

        mPositionTextView.setTextSize(isLandscape ? LARGE_TEXT_SIZE : SMALL_TEXT_SIZE);

        LayoutParams progressBarLayoutParams = (LayoutParams) mHorizontalProgressBar.getLayoutParams();
        progressBarLayoutParams.width = isLandscape ? m124dpInPix : m100dpInPix;
        progressBarLayoutParams.setMargins(0, isLandscape ? m16dpInPix : m14dpInPix, 0, 0);
        mHorizontalProgressBar.setLayoutParams(progressBarLayoutParams);
    }
}