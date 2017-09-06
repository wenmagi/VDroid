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

import com.magi.cache.db.query.Delete;
import okhttp3.*;

import java.io.IOException;

/**
 * @author chenyang @ Zhihu Inc.
 * @since 08-18-2017
 */
public final class CacheMediaServerUtils {

    private static final Callback IGNORE_CALLBACK = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {}

        @Override
        public void onResponse(Call call, Response response) throws IOException {}
    };

    public static void sendBeginSpeedUpCommand(CacheServer server, String m3u8Url, OkHttpClient client) {
        client.newCall(new Request.Builder()
                .get()
                .url("http://localhost:" + server.getListeningPort() + "/" + CacheServer.URL_BEGIN_SPEEDUP)
                .header(CacheServer.HEADER_HOST_KEY, m3u8Url)
                .build())
                .enqueue(IGNORE_CALLBACK);
    }

    public static void sendEndSpeedUpCommand(CacheServer server, String m3u8Url, OkHttpClient client) {
        client.newCall(new Request.Builder()
                .url("http://localhost:" + server.getListeningPort() + "/" + CacheServer.URL_END_SPEEDUP)
                .header(CacheServer.HEADER_HOST_KEY, m3u8Url)
                .build())
                .enqueue(IGNORE_CALLBACK);
    }

    public static String getUrlPath(String url) {
        if (url.contains("?")) {
            return url.substring(0, url.indexOf("?"));
        } else {
            return url;
        }
    }

    public static void deleteAllDBs() {
        new Delete().from(CacheInfo.class).execute();
    }
}
