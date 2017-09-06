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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import java8.util.stream.StreamSupport;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 09-05-2017
 */

public class ThreadPoolRunner implements NanoHTTPD.AsyncRunner {

    private final ThreadPoolExecutor mExecutor;
    private final Map<Runnable, Thread> mRunningHandlerMap = Collections.synchronizedMap(new HashMap<Runnable, Thread>());
    private final Set<NanoHTTPD.ClientHandler> mPostedSet = Collections.synchronizedSet(new HashSet<>());
    public ThreadPoolRunner() {
        mExecutor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(20), new ThreadPoolExecutor.DiscardOldestPolicy()) {
            @Override
            protected void beforeExecute(final Thread t, final Runnable r) {
                mRunningHandlerMap.put(r, t);
            }

            @Override
            protected void afterExecute(final Runnable r, final Throwable t) {
                mRunningHandlerMap.remove(r);
            }
        };
        mExecutor.prestartAllCoreThreads();
    }

    @Override
    public void closeAll() {
        StreamSupport
                .stream(mPostedSet)
                .forEach(handler -> {
                    handler.close();
                    mExecutor.remove(handler);
                });
        StreamSupport.stream(mRunningHandlerMap.values()).forEach(Thread::interrupt);
        mRunningHandlerMap.clear();
        mPostedSet.clear();
    }

    @Override
    public void closed(final NanoHTTPD.ClientHandler clientHandler) {
        mExecutor.remove(clientHandler);
        mPostedSet.remove(clientHandler);
        if (mRunningHandlerMap.containsKey(clientHandler)) {
            mRunningHandlerMap.get(clientHandler).interrupt();
        }
    }

    @Override
    public void exec(final NanoHTTPD.ClientHandler code) {
        mPostedSet.add(code);
        mExecutor.submit(code);
    }
}
