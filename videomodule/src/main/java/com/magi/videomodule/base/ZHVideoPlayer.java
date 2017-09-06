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
package com.magi.videomodule.base;

import android.net.Uri;
import android.view.View;

import com.magi.videomodule.base.listener.OnVideoListener;
import com.magi.videomodule.base.listener.OnVideoPlayErrorListener;


/**
 * @author ff @zhihu.inc
 * @since 1-10-2017
 */
public interface ZHVideoPlayer {

    void prepare(String uri);

    void prepare(Uri uri);

    void seekTo(long positionMs);

    void play();

    void replay();

    void pause();

    void resume();

    void stop();

    void release();

    long getDuration();

    long getCurrentPosition();

    void setVideoView(View surfaceView);

    boolean checkIsInitialized();

    boolean isPlaying();

    void setOnVideoListener(OnVideoListener listener);

    void removeOnVideoListener();

    void setOnVideoPlayErrorListener(OnVideoPlayErrorListener listener);

    void removeOnVideoPlayErrorListener();
}
