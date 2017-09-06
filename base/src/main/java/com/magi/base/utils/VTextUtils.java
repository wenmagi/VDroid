package com.magi.base.utils;

import android.text.TextUtils;


/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-26-2017
 */

public class VTextUtils {

    /**
     * UserName to user_name
     */
    public String changeName(String name) {

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < name.length(); ++i) {
            char pageInfoType = name.charAt(i);
            if (Character.isUpperCase(pageInfoType) && i != 0 && i != name.length() - 1) {
                stringBuilder.append("_").append(Character.toLowerCase(pageInfoType));
            } else if (i != 0 && i != name.length() - 1) {
                stringBuilder.append(pageInfoType);
            } else {
                stringBuilder.append(Character.toLowerCase(pageInfoType));
            }
        }
        return stringBuilder.toString();
    }
}
