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

package com.magi.cache.cleaner;

import com.magi.cache.CacheInfo;

import java.util.List;

/**
 * @author chenyang @ Zhihu Inc.
 * @since 08-22-2017
 */
public class SizeCacheCleaner extends AbstractCacheCleaner {

    private long mMaxSize;

    public SizeCacheCleaner(long maxSize) {
        mMaxSize = maxSize;
    }

    private synchronized void calculate() {
        long totalSize = 0;
        List<CacheInfo> currentCacheInfo = getLoadedCache();

        for (CacheInfo info : currentCacheInfo) {
            totalSize += info.savedLength;
        }

        if (totalSize < mMaxSize) {
            LOGGER.info("totalSize(%s) less than max size(%s)", totalSize, mMaxSize);
            return;
        }

        currentCacheInfo = getOrderedAndLoadedCache();

        for (CacheInfo info : currentCacheInfo) {
            if (totalSize < mMaxSize / 2) {
                return;
            }
            info.delete();
            totalSize -= info.savedLength;
            LOGGER.info("delete file|url|" + info.url);
        }
    }

    @Override
    public void onCreateFile(CacheInfo cacheInfo) {
        super.onCreateFile(cacheInfo);
        calculate();
    }

    @Override
    public void onWriteFile(CacheInfo cacheInfo, int writeSize) {
        super.onWriteFile(cacheInfo, writeSize);
        if (writeSize == cacheInfo.length) {
            LOGGER.info("onWriteFile done:" + cacheInfo.url);
            calculate();
        }
    }
}
