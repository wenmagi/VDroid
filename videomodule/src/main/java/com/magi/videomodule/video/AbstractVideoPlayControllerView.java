package com.magi.videomodule.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

/**
 * Created by chenhaohua on 20/4/2017.
 */

public abstract class AbstractVideoPlayControllerView extends FrameLayout
        implements View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener, IVideoPlayControllerView {

    protected OnVideoControllerListener videoControllerListener;

    public AbstractVideoPlayControllerView(Context pContext) {
        super(pContext);
    }

    public AbstractVideoPlayControllerView(Context pContext, AttributeSet pAttributeSet) {
        super(pContext, pAttributeSet);
    }

    public AbstractVideoPlayControllerView(Context pContext, AttributeSet pAttributeSet, int pDefaultStyle) {
        super(pContext, pAttributeSet, pDefaultStyle);
    }

    public void setTitle(String s) {
    }

    public void changeVisibility() {

    }

    public void setThumbnail(String s) {

    }

    public void setOnVideoControllerListener(OnVideoControllerListener videoControllerListener) {
        this.videoControllerListener = videoControllerListener;
    }

    public void onOrientationChange(int orientation) {

    }

    public void onVideoSizeChanged(int width, int height, boolean showControllerView) {

    }

    abstract public void showMiddlePlayButton(boolean show);
}
