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

package com.magi.cache;

import android.util.Log;

/**
 * Created by chenyang on 2017/7/31.
 */

public class Logger {

    public static final boolean DEBUG = false;


    private String tag;

    public Logger(String tag) {
        this.tag = "Cache-" + tag;
    }

    public void trace(String msg) {
        log(2, msg, null);
    }

    public void trace(String format, Object arg) {
        formatAndLog(2, format, arg);
    }

    public void trace(String format, Object arg1, Object arg2) {
        formatAndLog(2, format, arg1, arg2);
    }

    public void trace(String format, Object... argArray) {
        formatAndLog(2, format, argArray);
    }

    public void trace(String msg, Throwable t) {
        log(2, msg, t);
    }


    public void debug(String msg) {
        log(3, msg, null);
    }

    public void debug(String format, Object arg) {
        formatAndLog(3, format, arg);
    }

    public void debug(String format, Object arg1, Object arg2) {
        formatAndLog(3, format, arg1, arg2);
    }

    public void debug(String format, Object... argArray) {
        formatAndLog(3, format, argArray);
    }

    public void debug(String msg, Throwable t) {
        log(2, msg, t);
    }

    public void info(String msg) {
        log(4, msg, null);
    }

    public void info(String format, Object arg) {
        formatAndLog(4, format, arg);
    }

    public void info(String format, Object arg1, Object arg2) {
        formatAndLog(4, format, arg1, arg2);
    }

    public void info(String format, Object... argArray) {
        formatAndLog(4, format, argArray);
    }

    public void info(String msg, Throwable t) {
        log(4, msg, t);
    }

    public void warn(String msg) {
        log(5, msg, null);
    }

    public void warn(String format, Object arg) {
        formatAndLog(5, format, arg);
    }

    public void warn(String format, Object arg1, Object arg2) {
        formatAndLog(5, format, arg1, arg2);
    }

    public void warn(String format, Object... argArray) {
        formatAndLog(5, format, argArray);
    }

    public void warn(String msg, Throwable t) {
        log(5, msg, t);
    }

    public void error(String msg) {
        log(6, msg, null);
    }

    public void error(String format, Object arg) {
        formatAndLog(6, format, arg);
    }

    public void error(String format, Object arg1, Object arg2) {
        formatAndLog(6, format, arg1, arg2);
    }

    public void error(String format, Object... argArray) {
        formatAndLog(6, format, argArray);
    }

    public void error(String msg, Throwable t) {
        log(6, msg, t);
    }

    private void formatAndLog(int priority, String format, Object... argArray) {
        if (argArray!= null && argArray.length > 0 && argArray[0] instanceof Throwable) {
            logInternal(priority, String.format(format, argArray), (Throwable) argArray[0]);
        } else {
            logInternal(priority, String.format(format, argArray), null);
        }
    }

    private void log(int priority, String message, Throwable throwable) {
        logInternal(priority, message, throwable);
    }

    private void logInternal(int priority, String message, Throwable throwable) {
        if (DEBUG) {
            if(throwable != null) {
                message = message + '\n' + Log.getStackTraceString(throwable);
            }
            Log.println(priority, tag, message);
        }
    }

}
