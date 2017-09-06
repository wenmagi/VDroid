package com.magi.cache;

import android.net.Uri;

import com.magi.cache.cleaner.AbstractCacheCleaner;
import com.magi.cache.db.query.Select;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java8.util.stream.StreamSupport;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author chenyang @ Zhihu Inc.
 * @since 08-09-2017
 */
public abstract class AbstractLocalRequestHandler {

    protected OkHttpClient mHttpClient;
    protected File mCacheFile;
    protected CacheInfo mCacheInfo;
    protected String mUrl;
    protected String mKey;

    protected List<AbstractCacheCleaner> mCleanerList = new ArrayList<>();

    public AbstractLocalRequestHandler(OkHttpClient httpClient, File cacheFile, String url, String tag, String key) {
        mHttpClient = httpClient;
        mCacheFile = cacheFile;
        mUrl = url;
        mKey = key;

        mCacheInfo = new Select()
                .from(CacheInfo.class)
                .where("key = ?", key)
                .executeSingle();
        if (mCacheInfo == null) {
            mCacheInfo = generateCacheInfo(tag, key);
            mCacheInfo.save();
        }
    }

    /**
     *
     * @param tag 相同 tag 表示是同一个视频的文件，一般 tag 指的是 m3u8 文件 URL
     * @param key 表示去掉 auth key 等查询参数的链接，用于去重相同链接
     */
    protected CacheInfo generateCacheInfo(String tag, String key) {
        CacheInfo info = new CacheInfo();
        info.tag = tag;
        info.file = mCacheFile;
        info.url = mUrl;
        info.key = key;
        info.status = CacheInfo.State.INIT;
        info.updateTime = info.createTime = System.currentTimeMillis();
        return info;
    }


    protected void server(NanoHTTPD.IHTTPSession session, NanoHTTPD.Response response) {
        if (!mCacheInfo.exists()) {
            mCacheInfo = generateCacheInfo(session.getHeaders().get(CacheServer.HEADER_HOST_KEY), mKey);
        }
        mCacheInfo.requestCount++;
        mCacheInfo.save();
    }

    protected Request buildRequestFromSession(NanoHTTPD.IHTTPSession session) {
        Map<String, String> headerMap = session.getHeaders();
        headerMap.remove(CacheServer.HEADER_HOST_KEY);
        headerMap.remove("http-client-ip");
        headerMap.remove("remote-add");
        headerMap.put("host", Uri.parse(mUrl).getHost());
        return new Request.Builder()
                .url(mUrl)
                .headers(Headers.of(headerMap))
                .cacheControl(CacheControl.FORCE_NETWORK)
                .method(session.getMethod().toString(), null) // 假如播放器的请求不带 body
                .build();
    }

    protected void setHeaderFromOkHttp(NanoHTTPD.Response response, Headers headers) {
        if (headers == null) {
            return;
        }
        for (String key : headers.names()) {
            for (String value : headers.values(key)) {
                response.addHeader(key, value);
            }
        }
    }

    protected void makeSureNewFile() throws IOException {
        if (!mCacheFile.getParentFile().exists()) {
            mCacheFile.getParentFile().mkdirs();
        }
        if (mCacheFile.exists()) {
            mCacheFile.delete();
        }
        mCacheFile.createNewFile();
        StreamSupport.stream(mCleanerList).forEach(cleaner -> cleaner.onCreateFile(mCacheInfo));
    }

    protected boolean loadFromCache(NanoHTTPD.Response response) {
        if (!mCacheFile.exists() || mCacheFile.length() == 0) {
            return false;
        }
        StreamSupport.stream(mCleanerList).forEach(cleaner -> cleaner.onReadFile(mCacheInfo));
        try {
            response.setStatus(NanoHTTPD.Response.Status.OK);
            response.setContentLength(mCacheFile.length());
            response.setData(new FileInputStream(mCacheFile));
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void addCacheCleaner(AbstractCacheCleaner cleaner) {
        mCleanerList.add(cleaner);
    }

    public CacheInfo getCacheInfo() {
        return mCacheInfo;
    }

    protected void onErrorHappens(NanoHTTPD.Response response) {
        response.setStatus(NanoHTTPD.Response.Status.BAD_GATEWAY);
        mCacheInfo.setStatus(CacheInfo.State.INIT);
        mCacheInfo.save();
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
