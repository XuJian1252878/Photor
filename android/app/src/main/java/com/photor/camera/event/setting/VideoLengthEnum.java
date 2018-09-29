package com.photor.camera.event.setting;

public enum VideoLengthEnum {

    INFINITE(0, 0, "不限"),
    SECONDS_10(1, 10 * 1000, "10S"),
    SECONDS_30(2, 30 * 1000, "30S"),
    MINUTE_1(3, 60 * 1000, "1M"),
    MINUTE_5(4, 5 * 60 * 1000, "5M"),
    MINUTE_10(5, 10 * 60 * 1000, "10M"),
    MINUTE_20(6, 20 * 60 * 1000, "20M"),
    MINUTE_30(7, 30 * 60 * 1000, "30M");

    private int index;
    private int milliseconds;
    private String timeInfo;

    public int getIndex() {
        return index;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public String getTimeInfo() {
        return timeInfo;
    }

    public static String getTimeInfoByMilliseconds(int milliseconds) {
        for (VideoLengthEnum vle: VideoLengthEnum.values()) {
            if (vle.milliseconds == milliseconds) {
                return vle.timeInfo;
            }
        }
        return null;
    }

    public static int getMillisecondsByIndex(int index) {
        for (VideoLengthEnum vle: VideoLengthEnum.values()) {
            if (vle.index == index) {
                return vle.milliseconds;
            }
        }
        return -1;
    }

    public static int getMillisecondsByTimeInfo(String timeInfo) {
        for (VideoLengthEnum vle: VideoLengthEnum.values()) {
            if (vle.timeInfo.equals(timeInfo)) {
                return vle.getMilliseconds();
            }
        }
        return -1;
    }

    public static int getIndexByMilliseconds(int milliseconds) {
        for (VideoLengthEnum vle: VideoLengthEnum.values()) {
            if (vle.milliseconds == milliseconds) {
                return vle.index;
            }
        }

        return -1;
    }

    public static int getMillisecondsAround(int milliseconds) {
        if (milliseconds <= 0) {
            return INFINITE.milliseconds;
        }

        for (VideoLengthEnum vle: VideoLengthEnum.values()) {
            if (vle.milliseconds > milliseconds) {
                return vle.milliseconds;
            }
        }
        return MINUTE_30.milliseconds;
    }


    VideoLengthEnum(int index, int milliseconds, String timeInfo) {
        this.index = index;
        this.milliseconds = milliseconds;
        this.timeInfo = timeInfo;
    }
}
