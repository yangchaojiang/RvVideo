package com.vrvideo.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.Formatter;

/**
 * Created by yangc on 2017/5/18.
 * E-Mail:yangchaojiang@outlook.com
 * Deprecated:
 */
public class Utils {
    public static final String TAG = "Utils";

    /**
     * Returns the specified millisecond time formatted as a string.
     *
     * @param builder   The builder that {@code formatter} will write to.
     * @param formatter The formatter.
     * @param timeMs    The time to format as a string, in milliseconds.
     * @return The time formatted as a string.
     */
    public static String getStringForTime(StringBuilder builder, Formatter formatter, long timeMs) {
        if (timeMs == Long.MIN_VALUE + 1) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        builder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    /***
     * 根据Wifi信息获取本地Mac
     *
     * @param context 上下文
     ***/
    public static String getLocalMacAddressFromWifiInfo(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    /***
     * 获取sn号
     *
     * @return String
     ***/
    public static String getLocaSerialNumber() {
        return android.os.Build.SERIAL;
    }
}
