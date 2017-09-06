package com.magi.base.ui.function;

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

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-23-2017
 */
public class FunctionLazyLoad {

    public interface OnLazyLoadListener {

        void loadData();

        FunctionLazyLoad getLazyLoadFunction();
    }

    private OnLazyLoadListener mOnLazyLoadListener;

    //Fragment的View加载完毕的标记
    private boolean isViewCreated = false;
    //Fragment对用户可见的标记
    private boolean isUIVisible = false;

    public static FunctionLazyLoad openFunctionLazyLoad(OnLazyLoadListener onLazyLoadListener) {
        return new FunctionLazyLoad(onLazyLoadListener);
    }

    private FunctionLazyLoad(OnLazyLoadListener onLazyLoadListener) {
        this.mOnLazyLoadListener = onLazyLoadListener;
    }

    public void setViewCreated(boolean viewCreated) {
        isViewCreated = viewCreated;
    }

    public void lazyLoad() {
        if (mOnLazyLoadListener == null)
            return;

        //这里进行双重标记判断,是因为setUserVisibleHint会多次回调,并且会在onCreateView执行前回调,必须确保onCreateView加载完毕且页面可见,才加载数据
        if (isLoaded()) {
            mOnLazyLoadListener.loadData();
            //数据加载完毕,恢复标记,防止重复加载
            isViewCreated = false;
            isUIVisible = false;
        }
    }

    public void setUIVisible(boolean UIVisible) {
        isUIVisible = UIVisible;
    }

    /**
     * 是否已经开始加载数据
     */
    public boolean isLoaded() {
        return isViewCreated && isUIVisible;
    }
}
