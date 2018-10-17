package com.example.constant;

public class PhotoOperator {

    // 用于启动  显示图像配准、景深合成、曝光合成 结果的 Activity Extra参数
    public static final String EXTRA_PHOTO_OPERATE_RESULT_PATH = "extra_photo_operate_result_path"; // 对齐结果的图片路径
    public static final String EXTRA_PHOTO_IS_FROM_OPERATE_RESULT = "EXTRA_PHOTO_IS_FROM_OPERATE_RESULT";  // 说明图片是从照相程序直接来的

    // 用于启动图片pdf页面的
    public static final String EXTRA_PHOTO_TO_PDF_PATH = "EXTRA_PHOTO_TO_PDF_PATH";  // 图片转化成PDF之后，PDF文件存储路径的label
    public static final String EXTRA_PHOTO_TO_PDF_URI = "EXTRA_PHOTO_TO_PDF_URI";  // 图片转化成PDF之后，PDF文件存储路径的label

    // REQUEST 参数
    public static final int REQUEST_ACTION_EDITIMAGE = 9;

    // 相册贴图
    public static final int REQUEST_ACTION_CHART_LET = 10;

    // 打开寻找pdf文件浏览器的请求参数
    public static final int REQUEST_ACTION_PDF_FILE = 10;

}
