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

package com.magi.videomodule.util;

import com.google.android.exoplayer2.C;

import java.util.Locale;

/**
 * Created by chenyang on 2017/7/5.
 */

public final class VideoPlayUtils {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    public static boolean SHOW_LOG = false;

    private static final String[] NUMBER_STR_ARRAY = new String[100];

    static {
        for (int i = 0; i < 100; ++i) {
            NUMBER_STR_ARRAY[i] = String.format(DEFAULT_LOCALE, "%02d", i);
        }
    }

    public static String stringForTime(long timeMs) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        int seconds = (int) (totalSeconds % 60);
        int minutes = (int) ((totalSeconds / 60) % 60);
        long hours = totalSeconds / 3600;

        if (minutes < 0 || seconds < 0 || minutes > 100 || seconds > 100)
            return "0:00";

        return hours > 0 ? hours + ":" + NUMBER_STR_ARRAY[minutes] + ":" + NUMBER_STR_ARRAY[seconds]
                : NUMBER_STR_ARRAY[minutes] + ":" + NUMBER_STR_ARRAY[seconds];
    }

}
