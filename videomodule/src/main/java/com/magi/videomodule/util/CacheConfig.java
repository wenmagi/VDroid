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

import android.content.Context;
import android.net.Uri;


import java.io.File;
import java.io.IOException;

/**
 * @author chenyang @ Zhihu Inc.
 * @since 08-21-2017
 */
public class CacheConfig implements ICacheConfig {

    private Context mContext;
    private File mCacheRoot;

    public CacheConfig(Context context) {
        mContext = context;
        mCacheRoot = new File(mContext.getCacheDir(), "media-cache");
    }

    @Override
    public String getCacheRequestKey(String url) {
        return CacheMediaServerUtils.getUrlPath(url);
    }

    @Override
    public boolean usingCache(String url) {
        return true;
    }

    @Override
    public File generateCacheFile(String url) {
        Uri uri = Uri.parse(url);
        File cacheFile = new File(mCacheRoot, uri.getPath());
        if (!cacheFile.getParentFile().exists()) {
            cacheFile.getParentFile().mkdirs();
        }
        if (!cacheFile.exists()) {
            try {
                cacheFile.createNewFile();
            } catch (IOException pE) {
                pE.printStackTrace();
            }
        }
        return cacheFile;
    }
}
