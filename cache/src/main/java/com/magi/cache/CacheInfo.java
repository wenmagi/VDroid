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

import com.magi.cache.db.Model;
import com.magi.cache.db.annotation.Column;
import com.magi.cache.db.annotation.Table;

import java.io.File;

/**
 * Created by chenyang on 2017/8/3.
 */

@Table(name = "t_cache_info")
public class CacheInfo extends Model {

    @Column(value = "url", notNull = true, unique = true)
    public String url;

    @Column(value = "key", notNull = true, unique = true)
    public String key;

    @Column("length")
    public long length = 0;

    @Column("saved_length")
    public int savedLength = 0;

    @Column("cache_file")
    public File file;

    @Column(value = "status", notNull = true)
    public volatile State status;

    @Column("expire_time")
    public long expireAt = 0L;

    @Column("tag")
    public String tag;

    @Column("request_count")
    public int requestCount = 0;

    @Column(value = "create_time", notNull = true)
    public long createTime;

    @Column(value = "update_time", notNull = true)
    public long updateTime;

    public enum State {
        INIT,
        LOADING,
        LOADED,
    }

    public State getStatus() {
        return status;
    }

    public void setStatus(final State pState) {
        switch (pState) {
            case INIT: {
                if (file != null) {
                    file.delete();
                    savedLength = 0;
                }
                break;
            }

        }
        status = pState;
    }

    @Override
    protected void onBeforeSave() {
        super.onBeforeSave();
        updateTime = System.currentTimeMillis();
    }

    @Override
    protected void onBeforeInsert() {
        super.onBeforeInsert();
        updateTime = System.currentTimeMillis();
    }

    @Override
    public void delete() {
        super.delete();
        file.delete();
    }

    @Override
    public String toString() {
        return "CacheInfo{" +
                "url='" + url + '\'' +
                ", length=" + length +
                ", savedLength=" + savedLength +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
