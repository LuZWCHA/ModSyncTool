package com.rxcode.rxdownload;

public final class DownloadConfig {
    static final String TEMP_SUFFIX = ".temp";
    static int MAX_TASK_NUM = 2;
    static long SAMPLE_INTERVAL = 500;
    static String UNKNOWN_NAME = "unknown";
    static boolean USE_DEFAULT_NAME_FIRST = true;
    static String DEFAULT_DOWNLOAD_PATH = "";
    static boolean SKIP_COMPLETED_FILE = true;

    public static int getMaxTaskNum() {
        return MAX_TASK_NUM;
    }

    public static long getSampleInterval() {
        return SAMPLE_INTERVAL;
    }

    public static String getTempSuffix() {
        return TEMP_SUFFIX;
    }

    public static String getDefaultDownloadPath() {
        return DEFAULT_DOWNLOAD_PATH;
    }

    public static String getUnknownName() {
        return UNKNOWN_NAME;
    }

    public static boolean isSkipCompletedFile() {
        return SKIP_COMPLETED_FILE;
    }

    public static boolean isUseDefaultNameFirst() {
        return USE_DEFAULT_NAME_FIRST;
    }

    public static String getAbsolutePath(String fileName){
        return DEFAULT_DOWNLOAD_PATH + fileName;
    }
}
