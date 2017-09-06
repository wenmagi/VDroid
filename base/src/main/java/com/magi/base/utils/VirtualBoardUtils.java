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
import android.graphics.Rect;
import android.os.IBinder;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import com.magi.base.VApplication;

/**
 * listen visible changed of Navigation Bar and Keyboard
 *
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-23-2017
 */
public class VirtualBoardUtils {

    public static void hideKeyBoard(final Context pContext, final IBinder pIBinder) {
        if (pContext == null) {
            return;
        }

        if (pIBinder != null) {
            InputMethodManager imm = (InputMethodManager) pContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            //隐藏软键盘
            imm.hideSoftInputFromWindow(pIBinder, 0);
        }
    }

    public static void showKeyBoard(final Context pContext, final View pView) {
        if (pContext == null) {
            return;
        }

        pView.post(() -> {
            pView.requestFocus();
            InputMethodManager imm = (InputMethodManager) pContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(pView, InputMethodManager.SHOW_FORCED);
        });
    }

    public interface OnKeyboardVisibilityListener {
        void onVisibility(boolean visible);
    }

    public static void checkVisible(final View view, final OnKeyboardVisibilityListener listener) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            Rect r = new Rect();
            view.getWindowVisibleDisplayFrame(r);
            int screenHeight = view.getRootView().getHeight();

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                if (listener != null) {
                    listener.onVisibility(true);
                }
            } else {
                // keyboard is closed
                if (listener != null) {
                    listener.onVisibility(false);
                }
            }
        });
    }
}
