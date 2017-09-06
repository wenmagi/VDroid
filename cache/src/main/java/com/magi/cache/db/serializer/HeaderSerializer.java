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

package com.magi.cache.db.serializer;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import okhttp3.Headers;

/**
 * @author chenyang @ Zhihu Inc.
 * @since 08-11-2017
 */
public class HeaderSerializer extends TypeSerializer {
    @Override
    public Class<?> getDeserializedType() {
        return Headers.class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public String serialize(Object data) {
        Headers headers = (Headers) data;
        StringBuilder builder = new StringBuilder();
        for (String key : headers.names()) {
            try {
                builder.append(key)
                        .append(":")
                        .append(URLEncoder.encode(headers.get(key), "utf-8"))
                        .append("\n");
            } catch (UnsupportedEncodingException ignored) {
            }
        }
        return builder.toString();
    }

    @Override
    public Headers deserialize(Object data) {
        String str = (String) data;
        Headers.Builder builder = new Headers.Builder();
        for (String pair : str.split("\n")) {
            if (TextUtils.isEmpty(pair)) {
                continue;
            }
            String[] array = pair.split(":");
            if (array.length != 2) {
                continue;
            }
            try {
                builder.add(array[0], URLDecoder.decode(array[1], "utf-8"));
            } catch (UnsupportedEncodingException ignored) {
            }
        }
        return builder.build();
    }
}
