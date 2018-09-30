package com.photor.camera.event.setting;

/**
 * 相机输出照片的分辨率信息
 */
public enum ResolutionEnum {

//    MP320_240(0, 320, 240, "320*240"), //
//    MP352_288(1, 352, 288, "352*288"), //
//    MP640_360(2, 640, 360, "640*360"), //
    MP640_480(0, 640, 480, "640*480"), //
//    MP720_480(4, 720, 480, "720*480"), //
//    MP800_480(5, 800, 480, "800*480"), //
//    MP864_480(6, 864, 480, "864*480"), //
    MP800_600(1, 800, 600, "800*600"), //
//    MP1024_768(8, 1024, 768, "1024*768"), //
    MP1280_720(2, 1280, 720, "1280*720"), //
    MP1280_768(3, 1280, 768, "1280*768"), //
    MP1280_960(4, 1280, 960, "1280*960"), //
    MP1440_1080(5, 1440, 1080, "1440*1080"), //
    MP1600_1200(6, 1600, 1200, "1600*1200"), //
    MP1920_1080(7, 1920, 1080, "1920*1080"), //
//    MP2048_1536(5, 2048, 1536, "2048*1536"), //
//    MP2688_1512(6, 2688, 1512, "2688*1512"), //
//    MP2592_1944(7, 2592, 1944, "2592*1944"), //
//    MP2976_2976(8, 2976, 2976, "2976*2976"), //
//    MP3200_2400(9, 3200, 2400, "3200*2400"), //
//    MP3264_2448(10, 3264, 2448, "3264*2448"), //
//    MP3288_2480(11, 3288, 2480, "3288*2480"), //
//    MP3840_2160(12, 3840, 2160, "3840*2160"), //
//    MP4000_3000(13, 4000, 3000, "4000*3000"), //
//    MP4032_3024(14, 4032, 3024, "4032*3024")
    ; //

    private int index;
    private int height;
    private int width;
    private String message;

    public int getIndex() {
        return index;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getMessage() {
        return message;
    }

    public static ResolutionEnum getResolutionEnumByIndex(int index) {
        for (ResolutionEnum re: ResolutionEnum.values()) {
            if (re.index == index) {
                return re;
            }
        }
        return null;
    }

    ResolutionEnum(int index, int width, int height, String message) {
        this.index = index;
        this.width = width;
        this.height = height;
        this.message = message;
    }
}
