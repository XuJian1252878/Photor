//
// Created by 许舰 on 2018/3/8.
//

#ifndef PHOTOR_STARGRABCUT_H
#define PHOTOR_STARGRABCUT_H

#endif //PHOTOR_STARGRABCUT_H

#include <jni.h>
#include <iostream>
#include <string>

#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>

#include <android/log.h>
#define  LOG_TAG    "JNI_PART"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG, __VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG, __VA_ARGS__)

using namespace std;
using namespace cv;

class StarGrabCut
{
public:
    enum { NOT_SET = 0, IN_PROCESS = 1, SET = 2 };

    enum { SKY = 1, GROUND = 2, BOUNDARY = 3 };

    enum { NOT_INIT_POSITION };  // 用于记录边界线划分时的初始情况

    StarGrabCut();
    ~StarGrabCut();
    static void reset();
    static void init(Mat *_image, Mat *resImgMat, Mat *maskMat, jmethodID _showId );
    static void showImage(JNIEnv *env, jobject instance);
    static void mouseClick( int event, int x, int y, int flags, int lastX, int lastY, JNIEnv *env, jobject instance);
    static float calcSlop(int x, int y, int lastX, int lastY);
    static void setPtrAreaInMask(int x, int y, int lastX, int lastY);

private:
    static void setSkyRectInMask();
    static void setGroundRectInMask();

    static Mat* oriImgMat;
    static Mat* resImgMat;
    static jmethodID showId;


    static Mat *mask;

    static uchar skyRectState, groundRectState, boundaryState;
    static bool isInitialized;

    static Rect skyRect, groundRect;
//    static int iterCount;
};