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

import android.net.Uri;
import android.support.annotation.NonNull;

import com.magi.videomodule.base.ZHVideoPlayer;
import com.magi.videomodule.base.listener.OnVideoListener;
import com.magi.videomodule.base.listener.OnVideoPlayErrorListener;


public class VideoPlayPresenter implements IVideoPlayPresenter, OnVideoControllerListener, OnVideoListener, OnVideoPlayErrorListener {

    private long mVideoPosition = 0;

    //标记是否需要重新播放，用于播放[发生错误]和[播放结束]
    private boolean mNeedReplay;
    private boolean mInError = false;

    public interface OnActivityFinishListener {
        void onActivityFinish();

    }

    private IVideoPlayView mVideoPlayView;
    private ZHVideoPlayer mZHVideoPlayer;

    private Uri mUri;

    //通过依赖注入将 view 和 model 注入进来
    public VideoPlayPresenter(ZHVideoPlayer zhVideoPlayer, @NonNull IVideoPlayView iVideoPlayView) {

        this.mZHVideoPlayer = zhVideoPlayer;
        this.mVideoPlayView = iVideoPlayView;
    }

    public void setZHVideoPlayer(final ZHVideoPlayer pZHVideoPlayer) {
        mZHVideoPlayer = pZHVideoPlayer;
    }

    public void initializePlayer() {
        initializePlayer(true);
    }

    /**
     * 在初始化播放器的过程中注入 ZhExoPlayer
     *
     * @param autoPlay 是否自动播放
     */
    public void initializePlayer(boolean autoPlay) {
        if (mUri == null) {
            mVideoPlayView.onUriNull();
            return;
        }
        if (mVideoPlayView.provideVideoView() == null) {
            mVideoPlayView.onSurfaceViewNull();
            return;
        }
        if (mZHVideoPlayer == null) {
            mZHVideoPlayer = mVideoPlayView.provideZHVideoPlayer();
        }

        updateListener();
        mZHVideoPlayer.prepare(mUri);
        mZHVideoPlayer.setVideoView(mVideoPlayView.provideVideoView());

        //有可能是由于熄屏或者点击了 home button 退出了，重新加载的时候需要回到那时候的位置
        if (mVideoPosition <= 0)
            mVideoPosition = 0;

        if (mVideoPosition == 0) {
            mZHVideoPlayer.replay();
        } else {
            mZHVideoPlayer.seekTo(mVideoPosition);
            if (autoPlay) {
                mZHVideoPlayer.play();
            }
        }

        if (autoPlay) {
            mVideoPlayView.showLoading(true);
        }
    }

    public void updateListener() {
        mZHVideoPlayer.setOnVideoListener(this);
        mZHVideoPlayer.setOnVideoPlayErrorListener(this);
    }

    @Override
    public void recordCurrentPosition() {
        mVideoPosition = getCurrentPosition();
    }

    public void setVideoPosition(final long pVideoPosition) {
        mVideoPosition = pVideoPosition;
    }

    @Override
    public void releasePlayer() {

        if (mZHVideoPlayer != null) {
            mZHVideoPlayer.removeOnVideoListener();
            mZHVideoPlayer.removeOnVideoPlayErrorListener();
            mZHVideoPlayer.release();
            mZHVideoPlayer = null;
        }
    }

    @Override
    public void playUri(String uri) {
        playUri(uri, true);
    }

    public void playUri(String uri, boolean autoPlay) {
        this.mUri = Uri.parse(uri);
        if (uri != null) {
            initializePlayer(autoPlay);
        }
    }

    public void setUri(final String pUri) {
        mUri = Uri.parse(pUri);
    }

    @Override
    public void onClickFinished() {
        if (mVideoPlayView != null) {
            mVideoPlayView.onActivityFinish();
        }
    }

    //用于让 view 控制 presenter 的播放和暂停
    @Override
    public void onPlayOrStop() {
        if (mZHVideoPlayer != null && mVideoPlayView != null) {

            if (mInError) {
                reloadVideo();
                return;
            }

            if (mNeedReplay) {

                mNeedReplay = false;

                if (isInitialized()) {
                    setPosition(0);
                    if (mZHVideoPlayer != null) {
                        mZHVideoPlayer.seekTo(0);
                        mZHVideoPlayer.play();
                    }
                } else {
                    reloadVideo();
                }

            } else {

                if (mZHVideoPlayer.isPlaying()) {
                    mZHVideoPlayer.pause();
                    mVideoPlayView.onStopPlay();
                } else {

                    mZHVideoPlayer.play();
                    mVideoPlayView.onStartPlay();

                }
            }

        }
    }

    //视频 player，重新加载视频
    private void reloadVideo() {
        mInError = false;

        if (mZHVideoPlayer != null)
            mZHVideoPlayer.release();

        playUri(mUri.toString());
    }

    @Override
    public void onPositionChanged(float progress) {
        if (mZHVideoPlayer == null) {
            return;
        }
        long duration = mZHVideoPlayer.getDuration();
        mZHVideoPlayer.seekTo((long) (progress * duration));

        if (mInError)
            onPlayOrStop();

        mZHVideoPlayer.play();
        mVideoPlayView.onStartPlay();
    }

    @Override
    public long getDuration() {
        return mZHVideoPlayer == null ? 0 : mZHVideoPlayer.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return mZHVideoPlayer == null ? 0 : mZHVideoPlayer.getCurrentPosition();
    }

    @Override
    public void setPosition(long position) {
        this.mVideoPosition = position;
    }

    @Override
    public boolean isInitialized() {
        return mZHVideoPlayer != null;
    }

    @Override
    public void onSwitchScreenMode() {

    }

    @Override
    public void onCloseVideoPlayer() {

    }

    @Override
    public void onControllerVisibilityChange(boolean show) {

    }

    @Override
    public boolean isPlaying() {
        return mZHVideoPlayer != null && mZHVideoPlayer.isPlaying();
    }

    @Override
    public void onLoadingStateChanged(boolean isLoading) {
        mVideoPlayView.showLoading(isLoading);

        if (!isLoading) {
            mVideoPlayView.onStartPlay();
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if (mVideoPlayView != null) {
            mVideoPlayView.onVideoSizeChanged(width, height);
        }
    }

    @Override
    public void onComplete() {
        mNeedReplay = true;
        mVideoPlayView.onComplete();

        setPosition(0);
        if (mZHVideoPlayer != null) {
            mZHVideoPlayer.seekTo(0);
            mZHVideoPlayer.pause();
        }
    }

    @Override
    public void onError(Throwable error) {
        mInError = true;
        mNeedReplay = true;
        mVideoPlayView.onError(error);

        mVideoPosition = getCurrentPosition();

    }
}