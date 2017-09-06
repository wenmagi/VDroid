/*
 * Copyright (c) 2017 magi Inc.
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

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser;
import com.magi.cache.cleaner.AbstractCacheCleaner;
import com.magi.cache.db.ActiveAndroid;
import com.magi.cache.db.Configuration;
import com.magi.cache.db.query.Select;
import com.magi.cache.db.serializer.FileSerializer;
import com.magi.cache.db.serializer.HeaderSerializer;
import com.magi.cache.handles.M3U8RequestHandlerHandler;
import com.magi.cache.handles.TSRequestHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by chenyang on 2017/8/2.
 */

public class CacheServer extends NanoHTTPD {

    public static final String HEADER_HOST_KEY = "actual-host";
    public static final String URL_BEGIN_SPEEDUP = "_begin_speed_up";
    public static final String URL_PING = "_ping";
    public static final String URL_END_SPEEDUP = "_end_speed_up";
    public static final String URL_DISABLE_CACHE = "disable_local_cache";

    public static final String DB_NAME = "media-cache.db";
    public static final int DB_VERSION = 1;

    private static final String PROXY_HOST = "localhost";
    private static final Logger LOG = new Logger("server");

    private ICacheConfig mCacheConfig;
    private final OkHttpClient mOkHttpClient;
    private final Map<String, AbstractLocalRequestHandler> mRequestHandlerMap = new ConcurrentHashMap<>();
    private final List<AbstractCacheCleaner> mCleanerList = new LinkedList<>();
    private final Object mRequestLock = new Object();

    private CacheServer(Context context) {
        super(0);
        ActiveAndroid.initialize(new Configuration.Builder(context)
                .setDatabaseName(DB_NAME)
                .setDatabaseVersion(DB_VERSION)
                .addModelClass(CacheInfo.class)
                .addTypeSerializer(FileSerializer.class)
                .addTypeSerializer(HeaderSerializer.class)
                .create(), false);

        mOkHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.MINUTES)
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                .addInterceptor(new NetworkLogcatInterceptor())
                .build();
        setAsyncRunner(new ThreadPoolRunner());
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if (uri.endsWith(URL_BEGIN_SPEEDUP)) {
//            String m3u8Url = session.getHeaders().get(HEADER_HOST_KEY);
//            if (!TextUtils.isEmpty(m3u8Url)) {
//            }
            return newFixedLengthResponse("ok");
        } else if (uri.endsWith(URL_END_SPEEDUP)) {
//            String m3u8Url = session.getHeaders().get(HEADER_HOST_KEY);
//            if (!TextUtils.isEmpty(m3u8Url)) {
//            }
            return newFixedLengthResponse("ok");
        } else if (uri.endsWith(URL_PING)) {
            return newFixedLengthResponse("pong");
        } else {
            String url = getUrlFromSession(session);
            LOG.info("server:%s", url);
            String key = mCacheConfig.getCacheRequestKey(url);
            AbstractLocalRequestHandler handler;
            Response response = newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, MIME_HTML, null);
            String fileName = Uri.parse(url).getLastPathSegment();
            String tag = session.getHeaders().get(HEADER_HOST_KEY);
            synchronized (mRequestLock) {
                handler = mRequestHandlerMap.get(key);
                if (handler == null) {
                    if (fileName.endsWith(".m3u8")) {
                        handler = new M3U8RequestHandlerHandler(mOkHttpClient, mCacheConfig.generateCacheFile(url), url, tag, key);
                    } else if (fileName.endsWith(".ts")) {
                        handler = new TSRequestHandler(mOkHttpClient, mCacheConfig.generateCacheFile(url), url, tag, key);
                    } else {
                        return response;
                    }
                    mRequestHandlerMap.put(key, handler);
                    handler.mCleanerList.addAll(mCleanerList);
                }
            }

            handler.setUrl(url);
            handler.server(session, response);
            return response;
        }
    }

    public String getPureProxyUrl(String url) {
        Uri uri = Uri.parse(url);
        if ("1".equals(uri.getQueryParameter(URL_DISABLE_CACHE))) {
            return url;
        }
        Uri.Builder builder =  new Uri.Builder()
                .scheme("http")
                .encodedAuthority(PROXY_HOST + ":" + getListeningPort())
                .appendPath(uri.getPath().substring(1));

        for (String key : uri.getQueryParameterNames()) {
            builder.appendQueryParameter(key, uri.getQueryParameter(key));
        }
        builder.appendQueryParameter("_prefix", URLEncoder.encode(
                url.substring(0, url.indexOf("/", url.indexOf("://") + 3))));
        return builder.build()
                .toString();
    }

    public String getProxyUrl(String url) {
        Uri uri = Uri.parse(url);
        if ("1".equals(uri.getQueryParameter(URL_DISABLE_CACHE))) {
            return url;
        }
        Uri.Builder builder =  new Uri.Builder()
                .scheme("http")
                .encodedAuthority(PROXY_HOST + ":" + getListeningPort());

        for (String path : uri.getPath().substring(1).split("/")) {
            builder.appendPath(path);
        }
        for (String key : uri.getQueryParameterNames()) {
            builder.appendQueryParameter(key, uri.getQueryParameter(key));
        }
        return builder.build()
                .toString();
    }

    public String getUrlFromSession(NanoHTTPD.IHTTPSession session) {
        String m3u8Host = session.getHeaders().get(CacheServer.HEADER_HOST_KEY);
        String schemaAndHost = m3u8Host.substring(0, m3u8Host.indexOf("/", m3u8Host.indexOf("://") + 3));
        String fullUrl = "http://" + PROXY_HOST + ":" + getListeningPort() + session.getUri();
        if (!TextUtils.isEmpty(session.getQueryParameterString())) {
            fullUrl += "?" + session.getQueryParameterString()
                    + "&_prefix=" + URLEncoder.encode(schemaAndHost);
        } else {
            fullUrl += "?_prefix=" + URLEncoder.encode(schemaAndHost);
        }
        return getUrlFromProxy(fullUrl);
    }

    public String getUrlFromProxy(String proxyUrl) {
        Uri proxyUri = Uri.parse(proxyUrl);

        StringBuilder url = new StringBuilder(URLDecoder.decode(proxyUri.getQueryParameter("_prefix")));
        url.append(proxyUri.getPath());
        if (proxyUri.getQueryParameterNames().size() > 1) {
            url.append("?");
            for (String key : proxyUri.getQueryParameterNames()) {
                if ("_prefix".equals(key)) {
                    continue;
                }
                url.append(key)
                        .append("=")
                        .append(proxyUri.getQueryParameter(key))
                        .append("&");
            }
            url.deleteCharAt(url.length() - 1);
        }
        return url.toString();
    }

    public static class Builder {

        final CacheServer mServer;

        public Builder(Context context) {
            mServer = new CacheServer(context);
        }


        public Builder config(ICacheConfig pICacheConfig) {
            mServer.mCacheConfig = pICacheConfig;
            return this;
        }

        public Builder addCacheCleaner(AbstractCacheCleaner cleaner) {
            if (cleaner != null) {
                mServer.mCleanerList.add(cleaner);
            }
            return this;
        }


        public CacheServer build(){
            new Thread(() -> {
                try {
                    mServer.start();
                } catch (IOException e) {
                    mServer.stop();
                }
            }).start();
            return mServer;
        }
    }

    public void beginSpeedUp(String m3u8Url) {
        AbstractLocalRequestHandler handler;
        int count = 5;
        while ((handler = mRequestHandlerMap.get(m3u8Url)) == null && count > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            --count;
        }
        if (handler == null || !(handler instanceof M3U8RequestHandlerHandler)) {
            return;
        }
        M3U8RequestHandlerHandler m3U8RequestHandlerHandler = (M3U8RequestHandlerHandler) handler;

        count = 5;
        CacheInfo info = m3U8RequestHandlerHandler.getCacheInfo();
        if (info == null) {
            return;
        }
        while (info.status != CacheInfo.State.LOADED && count > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            --count;
        }
        if (info.status != CacheInfo.State.LOADED) {
            return;
        }

        try (FileInputStream inputStream = new FileInputStream(info.file)){
            HlsPlaylist playlist = new HlsPlaylistParser().parse(Uri.EMPTY, inputStream);

            if (!(playlist instanceof HlsMediaPlaylist)) {
                return;
            }
            HlsMediaPlaylist mediaPlaylist = (HlsMediaPlaylist) playlist;

            Uri m3u8Uri = Uri.parse(m3u8Url);
            final String hostAndPort = m3u8Uri.getScheme() + "://" + m3u8Uri.getHost() + (m3u8Uri.getPort() > 0 ? ":" + m3u8Uri.getPort() : "");

            for (int i = 0; i < mediaPlaylist.segments.size(); ++i) {
                HlsMediaPlaylist.Segment segment = mediaPlaylist.segments.get(i);
                String url = hostAndPort + "/" + segment.url;
                if (new Select().from(CacheInfo.class).where("url = ?", url).count() <= 0) {
                    mOkHttpClient.newCall(new Request.Builder()
                            .get()
                            .url(getProxyUrl(url))
                            .header(CacheServer.HEADER_HOST_KEY, m3u8Url)
                            .build())
                            .enqueue(null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endSpeedUp(String m3u8Url) {

    }
}
