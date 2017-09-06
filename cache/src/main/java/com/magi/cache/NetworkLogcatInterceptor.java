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

import java.io.IOException;
import java.net.URLDecoder;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * Created by chenyang on 2017/8/1.
 */
public class NetworkLogcatInterceptor implements Interceptor {

    public static String TAG = "NetWorkInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String methodName = request.method();
        if (methodName.equalsIgnoreCase("GET")) {
            Log.i(TAG, "-url--" + methodName + "--" + request.url());
        } else if (methodName.equalsIgnoreCase("POST")) {
            RequestBody mRequestBody = request.body();
            if (mRequestBody != null) {
                String msg = "-url--" + methodName + "--" + request.url();
                String content;
                if (msg.contains("uploadFile")) {
                    content = "--上传文件内容--";
                } else {
                    content = getParam(mRequestBody);
                }
                Log.i(TAG, msg + content);
            }
        }
        long startTime = System.currentTimeMillis();
        try {
            return chain.proceed(request);
        } finally {
            Log.i(TAG, "-url--" + methodName + "-- " + request.url() + " --using time:" + (System.currentTimeMillis() - startTime));
        }
    }

    /**
     * 读取参数

     */
    private String getParam(RequestBody requestBody) {
        Buffer buffer = new Buffer();
        try {
            requestBody.writeTo(buffer);
            return URLDecoder.decode(buffer.readUtf8(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}
