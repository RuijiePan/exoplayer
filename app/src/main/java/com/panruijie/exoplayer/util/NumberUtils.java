package com.panruijie.exoplayer.util;

import android.text.TextUtils;

/**
 * 数字转换工具类
 *
 * @author chenbenbin
 */
public class NumberUtils {

    public static long getLong(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        return Long.valueOf(value);
    }

    public static int getInteger(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        return Integer.valueOf(value);
    }
}
