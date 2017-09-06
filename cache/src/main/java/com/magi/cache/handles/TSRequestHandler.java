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

package com.magi.cache.handles;

import com.magi.cache.AbstractLocalRequestHandler;
import com.magi.cache.CacheInfo;
import com.magi.cache.Logger;
import com.magi.cache.NanoHTTPD;
import com.magi.cache.NanoHTTPD.Response.Status;
import com.magi.cache.OkHttpStatusCode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import java8.util.stream.StreamSupport;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * @author chenyang @ magi Inc.
 * @since 08-11-2017
 */
public class TSRequestHandler extends AbstractLocalRequestHandler {

    private static final Logger LOGGER = new Logger("TsRequestHandler");

    private final Object mFileLock = new Object();
    private RandomAccessFile mCacheAccessFile;
    private Headers mRequestHeaders;
    private volatile boolean mFailed;
    private LoadFileThread mLoadFileThread;

    public TSRequestHandler(OkHttpClient pHttpClient, File pCacheFile, String url, String tag, String key) {
        super(pHttpClient, pCacheFile, url, tag, key);
    }

    @Override
    protected synchronized void server(NanoHTTPD.IHTTPSession session, NanoHTTPD.Response httpdResponse) {
        super.server(session, httpdResponse);
        LOGGER.info("TS: uri:%s cacheInfo:%s", session.getUri(), mCacheInfo);

        switch (mCacheInfo.status) {
            case INIT: {
                mFailed = false;
                try {
                    makeSureNewFile();
                    mCacheAccessFile = new RandomAccessFile(mCacheFile, "rws");
                    mCacheInfo.setStatus(CacheInfo.State.LOADING);
                    Response response;
                    try {
                        response = mHttpClient.newCall(buildRequestFromSession(session))
                                .execute();
                    } catch (IOException pE) {
                        pE.printStackTrace();
                        onErrorHappens(httpdResponse);
                        return;
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        LOGGER.info("set init status in RequestHandler :%s", mUrl);
                        return;
                    }

                    if (!response.isSuccessful()) {
                        LOGGER.info("set init status in failed request :%s", mUrl);
                        httpdResponse.setStatus(new OkHttpStatusCode(response));
                        mCacheInfo.setStatus(CacheInfo.State.INIT);
                        mCacheInfo.save();
                        return;
                    }
                    mRequestHeaders = response.headers();
                    httpdResponse.setStatus(Status.OK);
                    setHeaderFromOkHttp(httpdResponse, mRequestHeaders);
                    mCacheInfo.length = response.body().contentLength();
                    mCacheInfo.save();
                    httpdResponse.setContentLength(mCacheInfo.length);
                    httpdResponse.setData(new CacheFileInputStream(httpdResponse));

                    mLoadFileThread = new LoadFileThread(response.body().byteStream());
                    mLoadFileThread.start();
                    break;
                } catch (IOException pE) {
                    pE.printStackTrace();
                    onErrorHappens(httpdResponse);
                    return;
                }
            }
            case LOADING: {
                if (mCacheAccessFile == null || !mCacheFile.exists()) {
                    LOGGER.info("missing cache file");
                    mCacheInfo.setStatus(CacheInfo.State.INIT);
                    server(session, httpdResponse);
                    return;
                }

                httpdResponse.setStatus(Status.OK);
                setHeaderFromOkHttp(httpdResponse, mRequestHeaders);
                httpdResponse.setContentLength(mCacheInfo.length);
                httpdResponse.setData(new CacheFileInputStream(httpdResponse));
                LOGGER.info("return loading status");
                break;
            }
            case LOADED: {
                if (loadFromCache(httpdResponse)) {
                    break;
                }
            }

            default: {
                LOGGER.info("set init status default case:" + mCacheInfo.status);
                mCacheInfo.setStatus(CacheInfo.State.INIT);
                server(session, httpdResponse);
            }
        }
    }

    private class LoadFileThread extends Thread {

        final InputStream mInputStream;

        int mWriteSize = 0;

        public LoadFileThread(InputStream inputStream) {
            mInputStream = inputStream;
        }

        @Override
        public void run() {
            int size;
            byte[] buffer = new byte[16 * 1024];
            try {
                LOGGER.info("net before put url:%s writeSize:%s", mUrl, mWriteSize);
                while ((size = mInputStream.read(buffer)) > 0 && !isInterrupted()) {
                    synchronized (mFileLock) {
                        try {
                            mCacheAccessFile.seek(mWriteSize);
                            mCacheAccessFile.write(buffer, 0, size);
                            mWriteSize += size;
                            mCacheInfo.savedLength = mWriteSize;
                        } catch (IOException e) {
                            mFailed = true;
                        }
                        mFileLock.notifyAll();
                    }
                    StreamSupport.stream(mCleanerList).forEach(cleaner -> cleaner.onWriteFile(mCacheInfo, mWriteSize));
                    if (mWriteSize >= mCacheInfo.length) {
                        mCacheInfo.setStatus(CacheInfo.State.LOADED);
                        mCacheInfo.save();
                        break;
                    } else if (mFailed) {
                        LOGGER.info("set init status in LoadFileThread, mFailed = true  :%s", mUrl);
                        mCacheInfo.setStatus(CacheInfo.State.INIT);
                    }
                    mCacheInfo.save();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            LOGGER.info("net after put :%s", mCacheInfo.url);

            if (isInterrupted() && mCacheInfo.getStatus() != CacheInfo.State.LOADED) {
                LOGGER.info("set init status in LoadFileThread :%s", mUrl);
                mCacheInfo.setStatus(CacheInfo.State.INIT);
                mCacheInfo.save();
            }
        }
    }

    private class CacheFileInputStream extends InputStream {
        final NanoHTTPD.Response mResponse;
        int mReadSize = 0; //已读计数

        private CacheFileInputStream(NanoHTTPD.Response response) {
            mResponse = response;
        }

        @Override
        public int read() throws IOException {
            synchronized (mFileLock) {
                mCacheAccessFile.seek(mReadSize);
                ++mReadSize;
                return mCacheAccessFile.readByte();
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int size;
            if (mFailed) {
                mResponse.setStatus(Status.BAD_GATEWAY);
                return -1;
            }
            if (mReadSize >= mCacheInfo.length) {
                return -1;
            }
            synchronized (mFileLock) {
                if (mReadSize >= mCacheAccessFile.length() && mCacheInfo.getStatus() == CacheInfo.State.LOADING) {
                    try {
//                        LOGGER.info("begin wait for fileLock:%s, readSize=%s, total=%s, status=%s",
//                                mCacheInfo.url, mReadSize, mCacheInfo.length, mCacheInfo.status);
                        mFileLock.wait(TimeUnit.SECONDS.toMillis(10));
//                        LOGGER.info("end wait for fileLock:%s", mCacheInfo.url);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }

                if (mReadSize + len > mCacheInfo.savedLength) {
                    len = mCacheInfo.savedLength - mReadSize;
                }
                if (len == 0 || mFailed) {
                    return 0;
                }
                mCacheAccessFile.seek(mReadSize);
                size = mCacheAccessFile.read(b, off, len);

            }
            mReadSize += size > 0 ? size : 0;
            return size;
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (mLoadFileThread != null && mCacheInfo.getStatus() != CacheInfo.State.LOADED) {
                mLoadFileThread.interrupt();
            }
        }
    }
}
