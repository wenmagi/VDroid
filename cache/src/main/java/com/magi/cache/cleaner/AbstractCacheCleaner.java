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
import com.magi.cache.Logger;
import com.magi.cache.db.query.Select;

import java.util.List;

/**
 * @author chenyang @ Zhihu Inc.
 * @since 08-22-2017
 */
public abstract class AbstractCacheCleaner {

    protected static final Logger LOGGER = new Logger("CacheCleaner");

    protected List<CacheInfo> getLoadedCache() {
        return new Select()
                .from(CacheInfo.class)
                .where("status = ?", CacheInfo.State.LOADED)
                .execute();
    }

    protected List<CacheInfo> getOrderedAndLoadedCache() {
        return new Select()
                .from(CacheInfo.class)
                .where("status = ?", CacheInfo.State.LOADED)
                .orderBy("update_time, request_count asc")
                .execute();
    }

    public void onCreateFile(CacheInfo cacheInfo) {

    }

    public void onWriteFile(CacheInfo cacheInfo, int writeSize) {

    }

    public void onReadFile(CacheInfo cacheInfo) {

    }

}
