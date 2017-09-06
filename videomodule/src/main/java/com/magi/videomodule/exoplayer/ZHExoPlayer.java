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
package com.magi.videomodule.exoplayer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.magi.videomodule.base.ZHVideoPlayer;
import com.magi.videomodule.base.listener.OnVideoListener;
import com.magi.videomodule.base.listener.OnVideoPlayErrorListener;
import com.magi.videomodule.util.VideoPlayUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ZHExoPlayer implements ZHVideoPlayer, Player.EventListener, SimpleExoPlayer.VideoListener {
    public static final String HEADER_HOST_KEY = "actual-host";

    private static final Executor EXECUTOR = new ThreadPoolExecutor(3, 10,
            1L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private boolean isPlaying = false;

    private Context mContext;

    private SimpleExoPlayer mExoPlayer;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private Handler mainHandler = new Handler();

    private OnVideoListener mOnVideoListener;

    private OnVideoPlayErrorListener mOnVideoPlayErrorListener;

    private boolean mIsBuffering;

    private int mWidth, mHeight;

    private Surface mSurface;
    private SurfaceTexture mTexture;
    protected Uri mActualUri;

    public ZHExoPlayer(@NonNull Context context) {
        this.mContext = context;
    }

    private void initialize() {
        if (mContext == null) {
            return;
        }

        mExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(mContext, null,
                        DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER),
                new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(BANDWIDTH_METER)),
                new DefaultLoadControl(new DefaultAllocator(true, 65536), 3000, 5000, 1500, 3000));
        mExoPlayer.setVideoListener(this);
        mExoPlayer.addListener(this);
        mExoPlayer.setPlayWhenReady(false);
        refreshPlayerState();
    }

    public float getVolume() {
        return mExoPlayer == null ? 0f : mExoPlayer.getVolume();
    }

    public void setVolume(float pVolume) {
        if (mExoPlayer != null) {
            mExoPlayer.setVolume(pVolume);
        }
    }

    private void refreshPlayerState() {
        if (mExoPlayer != null)
            onPlayerStateChanged(mExoPlayer.getPlayWhenReady(), mExoPlayer.getPlaybackState());
    }

    @Override
    public void prepare(String uri) {
        if (mExoPlayer == null) {
            initialize();
        }
        mExoPlayer.prepare(buildMediaSource(Uri.parse(uri)), true, true);
    }

    @Override
    public void prepare(final Uri uri) {
        if (mExoPlayer == null) {
            initialize();
        }
        mExoPlayer.prepare(buildMediaSource(uri), true, true);
    }

    @Override
    public void seekTo(long positionMs) {
        if (mExoPlayer != null)
            mExoPlayer.seekTo(positionMs);
    }

    @Override
    public void play() {
//        requestAudioFocus();

        if (mExoPlayer != null)
            mExoPlayer.setPlayWhenReady(true);

        refreshPlayerState();
    }

    @Override
    public void replay() {
//        requestAudioFocus();

        if (mExoPlayer != null) {
            mExoPlayer.seekTo(0);
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void pause() {
        if (mExoPlayer != null)
            mExoPlayer.setPlayWhenReady(false);

        isPlaying = false;

        refreshPlayerState();
    }

    public SurfaceTexture getSavedSurface() {
        if (mExoPlayer == null) {
            return null;
        } else {
            mExoPlayer.setVideoSurface(mSurface);
            return mTexture;
        }
    }

    @Override
    public void resume() {
//        requestAudioFocus();

        if (mExoPlayer != null)
            mExoPlayer.setPlayWhenReady(true);

        refreshPlayerState();
    }

    @Override
    public void stop() {
        isPlaying = false;
        if (mExoPlayer != null)
            mExoPlayer.setPlayWhenReady(false);

        if (mExoPlayer != null)
            mExoPlayer.stop();

        refreshPlayerState();
    }

    @Override
    public void release() {
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
            mExoPlayer.stop();
        }
        isPlaying = false;

        if (mExoPlayer != null) {
            final ExoPlayer player = mExoPlayer;
            EXECUTOR.execute(player::release);
        }
        mExoPlayer = null;
    }

    @Override
    public long getDuration() {

        if (mExoPlayer != null)
            return mExoPlayer.getDuration();

        return 0;
    }

    public ExoPlayer getPlayer() {
        return mExoPlayer;
    }

    public boolean isBuffering() {
        return mIsBuffering;
    }

    public boolean isPause() {
        return mExoPlayer != null && !mExoPlayer.getPlayWhenReady();
    }

    @Override
    public long getCurrentPosition() {
        if (mExoPlayer != null)
            return mExoPlayer.getCurrentPosition();
        return 0;
    }

    @Override
    public void setVideoView(View view) {
        if (mExoPlayer == null)
            return;

        if (view == null) {
            if (VideoPlayUtils.SHOW_LOG) {
                System.out.println("set Surface null");
            }
            mExoPlayer.setVideoSurface(null);
            return;
        }
        if (view instanceof SurfaceView) {
            mExoPlayer.setVideoSurfaceView((SurfaceView) view);
            return;
        }

        if (view instanceof TextureView) {
            TextureView textureView = (TextureView) view;
            if (textureView.getSurfaceTexture() != null) {
                if (VideoPlayUtils.SHOW_LOG) {
                    System.out.println("set Surface direct");
                }
                mTexture = textureView.getSurfaceTexture();
                mSurface = new Surface(mTexture);
                mExoPlayer.setVideoSurface(mSurface);
            }
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
                    if (mExoPlayer != null) {
                        if (VideoPlayUtils.SHOW_LOG) {
                            System.out.println("set Surface in callback");
                        }
                        mTexture = surface;
                        mSurface = new Surface(mTexture);
                        mExoPlayer.setVideoSurface(mSurface);
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int
                        height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
                }
            });
        }
    }

    @Override
    public boolean checkIsInitialized() {
        return mExoPlayer != null;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playWhenReady && mOnVideoListener != null) {
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    isPlaying = false;
                    mIsBuffering = true;
                    mOnVideoListener.onLoadingStateChanged(true);
                    break;

                case Player.STATE_READY:
                    isPlaying = true;
                    mIsBuffering = false;
                    mOnVideoListener.onLoadingStateChanged(false);
                    break;

                case Player.STATE_ENDED:
                    isPlaying = false;
                    mIsBuffering = false;
                    mOnVideoListener.onComplete();
                    break;

                default:
                    mIsBuffering = false;
                    break;
            }
        } else {
            isPlaying = false;
            mIsBuffering = false;

        }
    }

    @Override
    public void onRepeatModeChanged(int i) {

    }

    public void setOnVideoListener(OnVideoListener listener) {
        mOnVideoListener = listener;
    }

    @Override
    public void removeOnVideoListener() {
        mOnVideoListener = null;
    }

    @Override
    public void setOnVideoPlayErrorListener(OnVideoPlayErrorListener listener) {
        mOnVideoPlayErrorListener = listener;
    }

    @Override
    public void removeOnVideoPlayErrorListener() {
        mOnVideoPlayErrorListener = null;
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (mOnVideoPlayErrorListener != null) {
            mOnVideoPlayErrorListener.onError(error);
        }
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(final PlaybackParameters pPlaybackParameters) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        mWidth = width;
        mHeight = height;
        if (mOnVideoListener != null)
            mOnVideoListener.onVideoSizeChanged(width, height);
    }

    @Override
    public void onRenderedFirstFrame() {

    }

    private MediaSource buildMediaSource(Uri uri) {
        DefaultDataSourceFactory sourceFactory;
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        HttpDataSource.Factory factory = new DefaultHttpDataSourceFactory(getUserAgent(mContext), bandwidthMeter);
        factory.setDefaultRequestProperty(HEADER_HOST_KEY, mActualUri.toString());
        sourceFactory = new DefaultDataSourceFactory(mContext, bandwidthMeter, factory);

        int type = Util.inferContentType(uri.getLastPathSegment());
        switch (Util.inferContentType(uri.getLastPathSegment())) {
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, sourceFactory, mainHandler, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, sourceFactory, new DefaultExtractorsFactory(), mainHandler, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    public static String getUserAgent(Context context) {
        String versionName;
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
        }
        return "VDroid/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE
                + ") " + "ExoPlayerLib/" + ExoPlayerLibraryInfo.VERSION;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}