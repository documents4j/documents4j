package com.documents4j.util;

public class OsUtils {

    private static final String myOS = System.getProperty("os.name").toLowerCase();

    private OsUtils() {
        /* empty, but suppress visibility outside of package */
    }

    public static boolean isWindows() {
        return (myOS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (myOS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (myOS.indexOf("nix") >= 0 || myOS.indexOf("nux") >= 0 || myOS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (myOS.indexOf("sunos") >= 0);
    }

}
