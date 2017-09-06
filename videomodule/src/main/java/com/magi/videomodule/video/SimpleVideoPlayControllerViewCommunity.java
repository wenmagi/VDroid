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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.android.exoplayer2.util.Util;
import com.zhihu.android.R;
import com.zhihu.android.app.util.NetworkUtils;
import com.zhihu.android.app.util.ScreenBrightnessUtils;
import com.zhihu.android.base.util.DisplayUtils;
import com.zhihu.android.base.widget.ZHFrameLayout;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class SimpleVideoPlayControllerViewCommunity extends SimpleVideoPlayControllerView implements View.OnTouchListener {

    private static final int UNKNOWN = 0;
    private static final int BRIGHTNESS = 1;
    private static final int VOLUME = 2;
    private static final int SEEK = 3;

    private int mGestureType = UNKNOWN;
    private boolean enableGesture;

    private PointF mActionDownPointF = new PointF();

    private static final int OK = 0;
    private static final int DELETED = 1;
    private static final int ERROR = 2;

    private AudioManager mAudioManager;

    private static final String TAG = "SimpleVideoPlayController";

    private int videoState = OK;

    private String mTitleString;
    private int mCurrentOrientation = Configuration.ORIENTATION_UNDEFINED;//update each time onOrientation change called

    private Disposable mAutoFadeOutDisposable;

    private GestureControlView mGestureControlView;
    private ImageView mScreenSwitchButton;
    private ImageView closeImageView;
    private FrameLayout errorFrameLayout;
    private FrameLayout containerFrameLayout;
    private ImageView coverImageView;
    private TextView errorTextView;
    private View errorMask;

    private TextView mVideoTitleTextView;
    private LinearLayout topBannerLinearLayout;

    public static final int FADING_DURATION = 500;

    private int mMaxVolume;
    private int rangePerVolume;
    private float rangePerBrightness;
    private static final int gestureRange = 300;
    private static final float MAX_BRIGHTNESS = 1.0f;
    private static final int GESTURE_THRESHOLD = 30;
    private int mOriginalVolume;
    private long mOriginalPosition;
    private long mDuration;
    private float mOriginalBrightness;
    private GestureDetector mGestureDetector;

    public SimpleVideoPlayControllerViewCommunity(@NonNull Context pContext) {
        super(pContext);
    }

    public SimpleVideoPlayControllerViewCommunity(@NonNull Context pContext, boolean enableGesture) {
        super(pContext);
        this.enableGesture = enableGesture;
    }

    public SimpleVideoPlayControllerViewCommunity(Context pContext, AttributeSet pAttributeSet) {
        super(pContext, pAttributeSet);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        initViews();

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        rangePerVolume = gestureRange / mMaxVolume;
        rangePerBrightness = gestureRange / MAX_BRIGHTNESS;

        changeScreenSwitchButton(getContext().getResources().getConfiguration().orientation);

        closeImageView.setOnClickListener(this);

        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (videoControllerListener != null) {
                    videoControllerListener.onPlayOrStop();
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (videoState == ERROR) {
                    if (videoControllerListener != null) {
                        videoControllerListener.onPlayOrStop();
                    }
                } else {
                    changeVisibility();
                }
                return true;
            }
        });

        errorFrameLayout.setOnTouchListener(this);
        containerFrameLayout.setOnTouchListener(this);
        setOnClickListener(this);
    }

    @Override
    public void onStartPlay() {
        super.onStartPlay();

        videoState = OK;

        mSmallPlayButton.setImageResource(R.drawable.ic_video_pause_small_community);

        autoFadeOutWidgets();

        errorFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onStopPlay() {
        super.onStopPlay();
        mSmallPlayButton.setImageResource(R.drawable.ic_video_play_small_community);

        showControllerArea();
        if (mAutoFadeOutDisposable != null && !mAutoFadeOutDisposable.isDisposed())
            mAutoFadeOutDisposable.dispose();
    }

    protected int getLayoutId() {
        return R.layout.simple_video_play_controller_view_with_title;
    }

    @Override
    public void onClick(View v) {

        if (mAutoFadeOutDisposable != null && !mAutoFadeOutDisposable.isDisposed())
            mAutoFadeOutDisposable.dispose();

        if (videoState == DELETED)
            return;

        switch (v.getId()) {
            case R.id.close_video_player_button:
                videoControllerListener.onCloseVideoPlayer();
                break;

//            case R.id.error_holder:
//
//                if (videoState == ERROR) {
//                    if (videoControllerListener != null) {
//                        if (!NetworkUtils.isNetworkAvailable(getContext()))
//                            break;
//                        videoControllerListener.onPlayOrStop();
//                    }
//                } else {
//                    changeVisibility();
//                }
//
//                break;

            default:
                break;
        }

        super.onClick(v);
    }

    public void changeVisibility() {
        if (topBannerLinearLayout.getVisibility() == View.INVISIBLE && mBottomPanel.getVisibility() == View.INVISIBLE) {
            showControllerArea();
        } else {
            widgetFadeOut(topBannerLinearLayout, mBottomPanel);
        }
    }

    private void showControllerArea() {
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            widgetFadeIn(mBottomPanel);
        } else {
            widgetFadeIn(mBottomPanel, topBannerLinearLayout);
        }
    }

    private void widgetFadeOut(View... views) {
        videoControllerListener.onControllerVisibilityChange(false);
        for (View view : views) {
            if (view != null)
                view.animate().alpha(0f)
                        .setListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                view.setAlpha(1f);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                view.setVisibility(INVISIBLE);
                            }
                        })
                        .setDuration(FADING_DURATION).start();
        }
    }

    private void widgetFadeIn(View... views) {
        videoControllerListener.onControllerVisibilityChange(true);

        for (View view : views) {
            if (view != null)
                view.animate().alpha(1f)
                        .setListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                view.setVisibility(VISIBLE);
                                view.setAlpha(0f);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                autoFadeOutWidgets();
                            }
                        })
                        .setDuration(FADING_DURATION).start();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mGestureDetector.onTouchEvent(event))
            return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (enableGesture) {
                    mActionDownPointF.x = event.getX();
                    mActionDownPointF.y = event.getY();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (enableGesture) {

                    float dX = event.getX() - mActionDownPointF.x;
                    float dY = event.getY() - mActionDownPointF.y;

                    //一开始的时候有可能会 dX , dY 同时为 0
                    if (mGestureType == UNKNOWN && (Math.abs(dX) > GESTURE_THRESHOLD || Math.abs(dY) > GESTURE_THRESHOLD)) {

                        //需要重新设置手指的落点，否则会有 GESTURE_THRESHOLD 的距离影响
                        mActionDownPointF.x = event.getX();
                        mActionDownPointF.y = event.getY();

                        //确认手势模式，横向进度，左边纵向亮度，右边纵向声音
                        float width = getWidth() / 2;
                        mGestureType = Math.abs(dX) > Math.abs(dY) ? SEEK : (mActionDownPointF.x < width ? BRIGHTNESS : VOLUME);

                        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        mOriginalBrightness = ScreenBrightnessUtils.getAppBrightness(getContext());
                        mOriginalPosition = videoControllerListener.getCurrentPosition();

                        mDuration = videoControllerListener.getDuration();

                        onGestureStart();
                        break;
                    }

                    if (mGestureType != UNKNOWN) {
                        onGestureMove(dX, dY);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                if (enableGesture) {
                    onGestureEnd();
                }
                break;
        }
        return true;
    }

    private void onGestureMove(float dX, float dY) {

        switch (mGestureType) {
            case VOLUME:
                int renewedVolume = mOriginalVolume - (int) dY / rangePerVolume;

                if (renewedVolume < 0)
                    renewedVolume = 0;

                if (renewedVolume > mMaxVolume)
                    renewedVolume = mMaxVolume;

                //isVolumeFixed 是 api 21 加入的
                if (Util.SDK_INT >= 21 && mAudioManager.isVolumeFixed()) {
                    return;
                }

                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, renewedVolume, 0);
                mGestureControlView.changeVolume(renewedVolume, mMaxVolume);
                break;

            case BRIGHTNESS:
                float brightness = mOriginalBrightness - dY / rangePerBrightness;

                ScreenBrightnessUtils.setAppBrightness(getContext(), brightness);
                mGestureControlView.changeBrightness(brightness, MAX_BRIGHTNESS);
                break;

            case SEEK:
                int movingPixelInDp = DisplayUtils.pixelToDp(getContext(), dX);
                long pendingPosition = mOriginalPosition + movingPixelInDp / 2 * 1000;

                if (dX > 0) {
                    mGestureControlView.positionForward(pendingPosition, mDuration);
                } else {
                    mGestureControlView.positionBackward(pendingPosition, mDuration);
                }

                int pendingProgress = (int) ((float) pendingPosition / (float) mDuration * 100);
                if (pendingProgress < 0)
                    pendingProgress = 0;
                if (pendingProgress > 100)
                    pendingProgress = 100;

                mSeekBar.setProgress(pendingProgress);
                break;
        }
    }

    private void onGestureStart() {
        mMiddlePlayButton.animate().alpha(0).setDuration(FADING_DURATION).start();
        mGestureControlView.animate().alpha(1f)
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mGestureControlView.resize();
                        mGestureControlView.setAlpha(0f);
                        mGestureControlView.changeMode(mGestureType);
                        mGestureControlView.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                })
                .setDuration(FADING_DURATION).start();

        if (mGestureType == SEEK)
            onStartTrackingTouch(mSeekBar);
    }

    private void onGestureEnd() {
        mMiddlePlayButton.animate().alpha(1).setDuration(FADING_DURATION).start();

        if (mGestureType == SEEK)
            onStopTrackingTouch(mSeekBar);

        mGestureControlView.animate().alpha(0f)
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mGestureControlView.setAlpha(1f);
                        mGestureType = UNKNOWN;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mGestureControlView.setVisibility(GONE);
                        mGestureControlView.changeMode(UNKNOWN);
                    }
                })
                .setDuration(FADING_DURATION).start();
    }

    @Override
    public void onOrientationChange(int orientation) {
        mCurrentOrientation = orientation;
        changeScreenSwitchButton(orientation);

        refreshButtonSize(mCurrentOrientation);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mVideoTitleTextView.setText("");
            topBannerLinearLayout.setVisibility(INVISIBLE);
        } else {
            if (mTitleString != null)
                mVideoTitleTextView.setText(mTitleString);
        }

        showControllerArea();
    }

    private void refreshButtonSize(int mCurrentOrientation) {
        boolean isLandscape = mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE;

        int width = DisplayUtils.dpToPixel(getContext(), isLandscape ? 56 : 52);
        int height = DisplayUtils.dpToPixel(getContext(), isLandscape ? 24 : 20);
        LinearLayout.LayoutParams smallPlayButtonLayoutParams = (LinearLayout.LayoutParams) mSmallPlayButton.getLayoutParams();
        smallPlayButtonLayoutParams.width = width;
        smallPlayButtonLayoutParams.height = height;
        mSmallPlayButton.setLayoutParams(smallPlayButtonLayoutParams);

        LinearLayout.LayoutParams screenButtonLayoutParams = (LinearLayout.LayoutParams) mScreenSwitchButton.getLayoutParams();
        screenButtonLayoutParams.width = width;
        screenButtonLayoutParams.height = height;
        mScreenSwitchButton.setLayoutParams(smallPlayButtonLayoutParams);

        mCurrentPositionTextView.setTextSize(isLandscape ? 14 : 12);
        mDurationTextView.setTextSize(isLandscape ? 14 : 12);
    }

    private void changeScreenSwitchButton(int orientation) {

        mScreenSwitchButton.setImageResource(orientation == Configuration.ORIENTATION_LANDSCAPE ?
                R.drawable.ic_video_smallscreen : R.drawable.ic_video_fullscreen);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        super.onStartTrackingTouch(seekBar);
        if (mAutoFadeOutDisposable != null)
            mAutoFadeOutDisposable.dispose();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        super.onStopTrackingTouch(seekBar);
        autoFadeOutWidgets();
    }

    private void autoFadeOutWidgets() {
        if (mAutoFadeOutDisposable != null && !mAutoFadeOutDisposable.isDisposed())
            mAutoFadeOutDisposable.dispose();

        mAutoFadeOutDisposable = Observable.just(1)
                .delay(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (isPlaying())
                        widgetFadeOut(topBannerLinearLayout, mBottomPanel);
                }, throwable -> {

                });
    }

    @Override
    public void onVideoSizeChanged(int width, int height, boolean showControllerView) {
        super.onVideoSizeChanged(width, height, showControllerView);

        if (width > 0 && height > 0) {
            float videoRatio = (float) width / (float) height;

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //landscape
                setLayoutParams(new RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout
                        .LayoutParams.MATCH_PARENT));

            } else {
                //portrait

                if (getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    p.addRule(RelativeLayout.ALIGN_TOP, R.id.texture_view);
                    p.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.texture_view);
                    p.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    setLayoutParams(p);
                }
                setAspectRatio(videoRatio);
            }

            if (mScreenSwitchButton != null) {

                if (videoRatio < 1) {//一条高比宽更长的视频
                    mScreenSwitchButton.setVisibility(GONE);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mDurationTextView.getLayoutParams();
                    layoutParams.setMargins(0, 0, DisplayUtils.dpToPixel(getContext(), 16), 0);

                    mDurationTextView.setLayoutParams(layoutParams);

                } else {
                    mScreenSwitchButton.setVisibility(VISIBLE);
                }
            }
        }

        if (showControllerView) {
            showControllerArea();
        }
    }

    @Override
    public void onError(Throwable error) {
        super.onError(error);

        videoState = ERROR;

        if (NetworkUtils.isNetworkAvailable(getContext())) {
            String errorMsg = error.getMessage();
            errorTextView.setText(errorMsg);

            if (errorMsg != null && errorMsg.equals(getContext().getString(R.string.video_is_deleted)))
                videoState = DELETED;

        } else {
            //如果没有网络的话就让用户检查网络
            errorTextView.setText(getResources().getText(R.string.no_network_pls_check_connection));
        }

        coverImageView.setVisibility(VISIBLE);
        errorFrameLayout.setVisibility(VISIBLE);
        errorMask.setVisibility(VISIBLE);
        mMiddlePlayButton.setVisibility(GONE);
    }

    @Override
    public void setTitle(String title) {
        mTitleString = title;
    }

    @Override
    public void setThumbnail(String thumbnail) {

        if (TextUtils.isEmpty(thumbnail))
            return;

        ImageRequest imageRequest = ImageRequest.fromUri(thumbnail);

        DataSource<CloseableReference<CloseableImage>>
                dataSource = Fresco.getImagePipeline().fetchDecodedImage(imageRequest, getContext());

        dataSource.subscribe(new BaseDataSubscriber<CloseableReference<CloseableImage>>() {
            @Override
            protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                CloseableReference<CloseableImage> imageDataSource = dataSource.getResult();
                if (imageDataSource != null) {
                    CloseableImage image = imageDataSource.get();
                    if (image instanceof CloseableBitmap) {
                        Bitmap bitmap = ((CloseableBitmap) image).getUnderlyingBitmap();
                        coverImageView.setImageBitmap(bitmap);
                    }
                }
                dataSource.close();
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                if (dataSource != null) {
                    dataSource.close();
                }
            }
        }, UiThreadImmediateExecutorService.getInstance());

    }

    @Override
    public void onComplete() {
        super.onComplete();
        coverImageView.setVisibility(VISIBLE);
        errorFrameLayout.setVisibility(VISIBLE);
        errorTextView.setText("");
        errorMask.setVisibility(GONE);
    }

    private void initViews() {
        mGestureControlView = findViewById(R.id.gesture_control_view);
        mScreenSwitchButton = findViewById(R.id.screen_switch_button);
        mVideoTitleTextView = findViewById(R.id.video_title_textview);
        topBannerLinearLayout = findViewById(R.id.video_top_banner);
        coverImageView = findViewById(R.id.cover_imageview);
        errorFrameLayout = findViewById(R.id.error_holder);
        closeImageView = findViewById(R.id.close_video_player_button);
        errorMask = findViewById(R.id.error_mask);
        errorTextView = findViewById(R.id.error_msg);
        containerFrameLayout = findViewById(R.id.video_controller_widgets_container);

        mScreenSwitchButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mScreenSwitchButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.color_ff0f88eb));
                return false;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                mScreenSwitchButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.color_ffffffff));
                return false;
            }
            return false;
        });

        mScreenSwitchButton.setOnClickListener(view -> {
            if (videoControllerListener != null) {
                videoControllerListener.onSwitchScreenMode();
            }
        });
    }
}