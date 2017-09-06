package com.magi.videomodule.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class AspectSurfaceView extends SurfaceView {

    private static final float MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0.01f;

    private float mAspectRatio;

    public AspectSurfaceView(Context context) {
        super(context);
    }

    public AspectSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setVideoWidthHeightRatio(float widthHeightRatio) {
        if (this.mAspectRatio != widthHeightRatio) {
            this.mAspectRatio = widthHeightRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (mAspectRatio != 0) {
            float viewAspectRatio = (float) width / height;
            float aspectDeformation = mAspectRatio / viewAspectRatio - 1;
            if (aspectDeformation > MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
                height = (int) (width / mAspectRatio);
            } else if (aspectDeformation < -MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
                width = (int) (height * mAspectRatio);
            }
        }
        setMeasuredDimension(width, height);
    }

}