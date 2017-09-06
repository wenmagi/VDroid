package com.magi.videomodule.util;

import com.magi.cache.CacheInfo;
import com.magi.cache.CacheServer;
import com.magi.cache.db.query.Delete;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 09-05-2017
 */

class CacheMediaServerUtils {
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
