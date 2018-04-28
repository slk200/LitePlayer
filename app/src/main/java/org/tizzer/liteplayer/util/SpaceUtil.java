package org.tizzer.liteplayer.util;

import java.util.Locale;

public class SpaceUtil {

    /**
     * B转换成B，KB，MB，GB
     *
     * @param bytes
     * @return
     */
    public static String byte2space(long bytes) {
        float kb = bytes / 1024;
        if (kb <= 1) {
            return bytes + "B";
        }
        float mb = kb / 1024;
        if (mb <= 1) {
            return String.format(Locale.CHINA, "%.2fKB", kb);
        }
        float gb = mb / 1024;
        if (gb <= 1) {
            return String.format(Locale.CHINA, "%.2fMB", mb);
        }
        return String.format(Locale.CHINA, "%.2fGB", gb);
    }
}
