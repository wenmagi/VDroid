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

import java.io.File;

/**
 * Created by chenyang on 2017/8/3.
 */

public interface ICacheConfig {

    String getCacheRequestKey(String url);

    boolean usingCache(String url);

    /**
     * 根据 url 和 初始化设置的缓冲根文件夹，获取当前缓存文件所在的文件夹
     * 比如 http://movie.zhihu.com/{quality}/{job}/{resource}.m3u8 可以设置为 {quality}-{job}/{resource} 的目录结构
     * @param url 资源 url
     * @return 缓存文件所在文件夹
     */
    File generateCacheFile(String url);
}
