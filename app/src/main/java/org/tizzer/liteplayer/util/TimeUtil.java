package org.tizzer.liteplayer.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {

    /**
     * 毫秒转时间段
     * <code>
     * mm:ss / HH:mm:ss
     * </code>
     *
     * @param duration 时长
     * @param option   是否返回长时间
     *                 <code>
     *                 true "HH:mm:ss"
     *                 false "HH:mm:ss" / "mm:ss"
     *                 </code>
     * @return 时间格式
     */
    public static String mills2timescale(int duration, boolean option) {
        int mills = duration / 1000;
        int hour = mills / 60 / 60;
        int minute = mills / 60 % 60;
        int second = mills % 60;
        if (!option && hour == 0) {
            return String.format(Locale.CHINA, "%02d:%02d", minute, second);
        } else {
            return String.format(Locale.CHINA, "%02d:%02d:%02d", hour, minute, second);
        }
    }

    /**
     * 时间戳转时间
     * <code>
     * yyyy年MM月dd日 HH:mm:ss
     * </code>
     *
     * @param timestamp 时间戳
     * @return
     */
    public static String timestamp2datetime(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);
        Date date = new Date(timestamp);
        return simpleDateFormat.format(date);
    }

}
