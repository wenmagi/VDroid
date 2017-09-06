package com.magi.videomodule.video;

import android.view.View;

import com.magi.videomodule.base.ZHVideoPlayer;


/**
 * Created by chenhaohua on 23/3/2017.
 */

public interface IVideoPlayView extends IVideoPlayControllerView, OnVideoSizeChangedListener, VideoPlayPresenter.OnActivityFinishListener {

    void onUriNull();

    void onSurfaceViewNull();

    ZHVideoPlayer provideZHVideoPlayer();

    View provideVideoView();

}
