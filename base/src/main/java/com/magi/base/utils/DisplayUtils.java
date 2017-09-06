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

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-24-2017
 */
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

public class DisplayUtils {

    public static int getScreenWidthPixels(final Context pContext) {
        if (pContext == null) {
            return 0;
        }

        return pContext.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeightPixels(final Context pContext) {
        if (pContext == null) {
            return 0;
        }

        return pContext.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getStatusBarHeightPixels(final Context pContext) {
        if (pContext == null) {
            return 0;
        }

        return getInternalDimensionPixelSize(pContext.getResources(), "status_bar_height");
    }

    public static int getInternalDimensionPixelSize(final Resources pResources, final String pKey) {
        final int resourceId = pResources.getIdentifier(pKey, "dimen", "android");

        if (resourceId > 0) {
            return pResources.getDimensionPixelSize(resourceId);
        }

        return 0;
    }

    public static int getActionBarHeightPixels(final Context pContext) {
        final TypedValue typedValue = new TypedValue();

        if (pContext != null && pContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            return TypedValue.complexToDimensionPixelSize(typedValue.data, pContext.getResources().getDisplayMetrics());
        }

        return 0;
    }

    public static boolean hasNavigationBar(final Context pContext) {
        final int id = pContext.getResources().getIdentifier("config_showNavigationBar", "bool", "android");

        if (id > 0) {
            return pContext.getResources().getBoolean(id);
        } else {
            return !ViewConfiguration.get(pContext).hasPermanentMenuKey() && !KeyCharacterMap.deviceHasKey(KeyEvent
                    .KEYCODE_BACK);
        }
    }

    public static int getNavigationBarHeight(final Context pContext) {
        if (!DisplayUtils.hasNavigationBar(pContext)) {
            return 0;
        }

        final Resources resources = pContext.getResources();

        final int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }

        return 0;
    }

    public static int pixelToDp(final Context pContext, final float pPixels) {
        if (pContext == null) {
            return 0;
        }

        final float density = pContext.getResources().getDisplayMetrics().density;

        return (int) ((pPixels / density) + 0.5);
    }

    public static int dpToPixel(final Context pContext, final float pDp) {
        if (pContext == null) {
            return 0;
        }

        final float density = pContext.getResources().getDisplayMetrics().density;

        return (int) ((pDp * density) + 0.5f);
    }

    public static int spToPixel(final Context pContext, final float pSp) {
        if (pContext == null) {
            return 0;
        }

        return (int) (pSp * pContext.getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }
}