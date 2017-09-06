package com.magi.cache.handles;

import android.net.Uri;
import android.text.TextUtils;

import com.magi.cache.AbstractLocalRequestHandler;
import com.magi.cache.CacheInfo;
import com.magi.cache.Logger;
import static com.magi.cache.NanoHTTPD.*;
import com.magi.cache.NanoHTTPD;
import com.magi.cache.OkHttpStatusCode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import java8.util.stream.StreamSupport;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * @author chenyang @ magi Inc.
 * @since 08-09-2017
 */
public class M3U8RequestHandlerHandler extends AbstractLocalRequestHandler {

    private static final Logger LOGGER = new Logger("M3U8RequestHandler");

    public M3U8RequestHandlerHandler(OkHttpClient pHttpClient, File pCacheFile, String url, String tag, String key) {
        super(pHttpClient, pCacheFile, url, tag, key);
    }

    @Override
    protected CacheInfo generateCacheInfo(String tag, String key) {
        CacheInfo info = super.generateCacheInfo(tag, key);
        Uri uri = Uri.parse(info.url);
        try {
            long expire = Long.parseLong(uri.getQueryParameter("expiration"));
            info.expireAt = TimeUnit.SECONDS.toMillis(expire);
        } catch (NumberFormatException ignored) {

        }
        return info;
    }

    @Override
    protected void server(NanoHTTPD.IHTTPSession session, NanoHTTPD.Response httpdResponse) {
        super.server(session, httpdResponse);
        LOGGER.info("TS: uri:%s cacheInfo:%s", session.getUri(), mCacheInfo);

        super.server(session, httpdResponse);
        switch (mCacheInfo.status) {
            case INIT: {
                byte[] data;
                try (Response response = mHttpClient.newCall(buildRequestFromSession(session)).execute()){
                    if (!response.isSuccessful()) {
                        LOGGER.info("failed request: %s, %s", response, mUrl);
                        httpdResponse.setStatus(new OkHttpStatusCode(response));
                        return;
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    data = response.body().bytes();
                } catch (IOException pE) {
                    pE.printStackTrace();
                    onErrorHappens(httpdResponse);
                    return;
                }

                if (Thread.currentThread().isInterrupted()) {
                    LOGGER.info("cancel server m3u8 request : " + mUrl);
                    return;
                }

                try {
                    makeSureNewFile();
                    FileOutputStream stream = new FileOutputStream(mCacheFile);
                    stream.write(data);
                    stream.flush();
                    stream.close();
                    StreamSupport.stream(mCleanerList).forEach(cleaner -> cleaner.onWriteFile(mCacheInfo, data.length));
                    mCacheInfo.savedLength = data.length;
                    Uri uri = Uri.parse(mUrl);
                    String expiration = uri.getQueryParameter("expiration");
                    if (!TextUtils.isEmpty(expiration)) {
                        try {
                            mCacheInfo.expireAt = TimeUnit.SECONDS.toMillis(Long.parseLong(expiration));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    mCacheInfo.setStatus(CacheInfo.State.LOADED);
                    mCacheInfo.save();

                    httpdResponse.setStatus(NanoHTTPD.Response.Status.OK);
                    httpdResponse.setData(new ByteArrayInputStream(data));
                    httpdResponse.setContentLength(data.length);
                    setHeaderFromOkHttp(httpdResponse, Headers.of(session.getHeaders()));
                    break;
                } catch (IOException pE) {
                    pE.printStackTrace();
                    onErrorHappens(httpdResponse);
                    return;
                }
            }
            case LOADED: {
                if (mCacheInfo.expireAt > 0 && Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() > mCacheInfo.expireAt) {
                    mCacheInfo.savedLength = 0;
                    mCacheInfo.file.delete();
                    // fail through
                } else if (loadFromCache(httpdResponse))  {
                    break;
                }
            }

            default: {
                mCacheInfo.setStatus(CacheInfo.State.INIT);
                server(session, httpdResponse);
            }
        }
    }
}
