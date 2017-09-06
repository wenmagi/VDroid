package com.magi.videomodule.util;

import java.io.File;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 09-05-2017
 */

public interface ICacheConfig {

    String getCacheRequestKey(String url);

    boolean usingCache(String url);

    /**
     * 根据 url 和 初始化设置的缓冲根文件夹，获取当前缓存文件所在的文件夹
     * 比如 http://movie.zhihu.com/{quality}/{job}/{resource}.m3u8 可以设置为 {quality}-{job}/{resource} 的目录结构
     *
     * @param url 资源 url
     * @return 缓存文件所在文件夹
     */
    File generateCacheFile(String url);
}
