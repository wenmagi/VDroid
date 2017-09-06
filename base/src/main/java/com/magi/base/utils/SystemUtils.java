package com.magi.base.utils;


/*
 * Copyright (c) 2015 Zhihu Inc.
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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;

@SuppressWarnings("unused")
public class SystemUtils {

    public static final boolean SDK_VERSION_ICE_CREAM_SANDWICH_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES
            .ICE_CREAM_SANDWICH;

    public static final boolean SDK_VERSION_ICE_CREAM_SANDWICH_MR1_OR_LATER = Build.VERSION.SDK_INT >= Build
            .VERSION_CODES
            .ICE_CREAM_SANDWICH_MR1;

    public static final boolean SDK_VERSION_JELLY_BEAN_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES
            .JELLY_BEAN;

    public static final boolean SDK_VERSION_JELLY_BEAN_MR1_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES
            .JELLY_BEAN_MR1;

    public static final boolean SDK_VERSION_JELLY_BEAN_MR2_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES
            .JELLY_BEAN_MR2;

    public static final boolean SDK_VERSION_KITKAT_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    public static final boolean SDK_VERSION_KITKAT_WATCH_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES
            .KITKAT_WATCH;

    public static final boolean SDK_VERSION_LOLLIPOP_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public static final boolean SDK_VERSION_LOLLIPOP_MR1_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES
            .LOLLIPOP_MR1;

    public static final boolean SDK_VERSION_M_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    public static final boolean SDK_VERSION_N_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;

    public static final boolean SDK_VERSION_N_MR1_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;

    public static final boolean SDK_VERSION_O_OR_LATER = Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1;

    public static final boolean SDK_VERSION_HONEYCOMB_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private static final String BOGOMIPS_PATTERN = "BogoMIPS[\\s]*:[\\s]*(\\d+\\.\\d+)[\\s]*\n";

    private static final String MEMTOTAL_PATTERN = "MemTotal[\\s]*:[\\s]*(\\d+)[\\s]*kB\n";

    private static final String MEMFREE_PATTERN = "MemFree[\\s]*:[\\s]*(\\d+)[\\s]*kB\n";

    public static int getPackageVersionCode(final Context pContext) throws SystemUtilsException {
        return SystemUtils.getPackageInfo(pContext).versionCode;
    }

    public static String getPackageVersionName(final Context pContext) throws SystemUtilsException {
        return SystemUtils.getPackageInfo(pContext).versionName;
    }

    public static String getPackageName(final Context pContext) {
        return pContext.getPackageName();
    }

    private static PackageInfo getPackageInfo(final Context pContext) throws SystemUtilsException {
        final PackageManager packageManager = pContext.getPackageManager();

        try {
            return packageManager.getPackageInfo(pContext.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            throw new SystemUtilsException(e);
        }
    }

    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isCharging(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent == null) return false;
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged != 0;
    }

    public static class SystemUtilsException extends Exception {

        public SystemUtilsException() {

        }

        public SystemUtilsException(final Throwable pThrowable) {
            super(pThrowable);
        }
    }

    public static String getApplicationLabel(final Context pContext, final String pPackageName) {
        final PackageManager packageManager = pContext.getPackageManager();

        try {
            final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(pPackageName, 0);
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (final PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean hasSystemFeature(final Context pContext, final String pFeature) {
        try {
            final Method PackageManager_hasSystemFeatures = PackageManager.class.getMethod("hasSystemFeature", String
                    .class);
            return (PackageManager_hasSystemFeatures == null) ? false : (Boolean) PackageManager_hasSystemFeatures
                    .invoke(pContext.getPackageManager(), pFeature);
        } catch (final Throwable t) {
            return false;
        }
    }

    public static int currentYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    public static boolean isLessThan(int version) {
        return Build.VERSION.SDK_INT < version;
    }

    public static boolean isEqualAndLessThan(int version) {
        return Build.VERSION.SDK_INT <= version;
    }

    public static boolean isMoreThan(int version) {
        return Build.VERSION.SDK_INT > version;
    }

    public static boolean isEqualAndMoreThan(int version) {
        return Build.VERSION.SDK_INT >= version;
    }

    public static boolean isEqual(int version) {
        return Build.VERSION.SDK_INT == version;
    }


    private static boolean contains(String what, String... items) {
        for (String s : items) {
            if (s != null && s.toLowerCase(Locale.getDefault()).contains(what)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSony() {
        return contains("sony", Build.MANUFACTURER, Build.BRAND, Build.FINGERPRINT);
    }

    // 检测MIUI
    public static boolean isMIUI() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            return prop.getProperty("ro.miui.ui.version.code", null) != null
                    || prop.getProperty("ro.miui.ui.version.name", null) != null
                    || prop.getProperty("ro.miui.internal.storage", null) != null;
        } catch (final IOException e) {
            return false;
        }
    }

    // =================================================================================================================
    // System Info
    // =================================================================================================================

    /**
     * @return in kiloBytes.
     * @throws SystemUtilsException
     */
    public static int getMemoryTotal() throws SystemUtilsException {
        final MatchResult matchResult = SystemUtils.matchSystemFile("/proc/meminfo", SystemUtils.MEMTOTAL_PATTERN,
                1000);

        try {
            if (matchResult.groupCount() > 0) {
                return Integer.parseInt(matchResult.group(1));
            } else {
                throw new SystemUtilsException();
            }
        } catch (final NumberFormatException e) {
            throw new SystemUtilsException(e);
        }
    }

    /**
     * @return in kiloBytes.
     * @throws SystemUtilsException
     */
    public static int getMemoryFree() throws SystemUtilsException {
        final MatchResult matchResult = SystemUtils.matchSystemFile("/proc/meminfo", SystemUtils.MEMFREE_PATTERN, 1000);

        try {
            if (matchResult.groupCount() > 0) {
                return Integer.parseInt(matchResult.group(1));
            } else {
                throw new SystemUtilsException();
            }
        } catch (final NumberFormatException e) {
            throw new SystemUtilsException(e);
        }
    }

    private static MatchResult matchSystemFile(final String pSystemFile, final String pPattern, final int pHorizon)
            throws SystemUtilsException {
        try (final InputStream inputStream = new ProcessBuilder(new String[]{"/system/bin/cat", pSystemFile}).start()
                .getInputStream()) {
            final Scanner scanner = new Scanner(inputStream);

            final boolean matchFound = scanner.findWithinHorizon(pPattern, pHorizon) != null;

            if (matchFound) {
                return scanner.match();
            } else {
                throw new SystemUtilsException();
            }
        } catch (final IOException e) {
            throw new SystemUtilsException(e);
        }
    }

    // =================================================================================================================
    //
    // =================================================================================================================


    //引用到的工具类
    private static class BuildProperties {

        private final Properties properties;

        private BuildProperties() throws IOException {
            properties = new Properties();
            properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        }

        public static BuildProperties newInstance() throws IOException {
            return new BuildProperties();
        }

        public boolean containsKey(final Object key) {
            return properties.containsKey(key);
        }

        public boolean containsValue(final Object value) {
            return properties.containsValue(value);
        }

        public Set<Map.Entry<Object, Object>> entrySet() {
            return properties.entrySet();
        }

        public String getProperty(final String name) {
            return properties.getProperty(name);
        }

        public String getProperty(final String name, final String defaultValue) {
            return properties.getProperty(name, defaultValue);
        }

        public boolean isEmpty() {
            return properties.isEmpty();
        }

        public Enumeration<Object> keys() {
            return properties.keys();
        }

        public Set<Object> keySet() {
            return properties.keySet();
        }

        public int size() {
            return properties.size();
        }

        public Collection<Object> values() {
            return properties.values();
        }

    }

}