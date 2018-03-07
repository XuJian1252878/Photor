//
// Created by 许舰 on 2018/3/6.
//

#ifndef PHOTOR_GCAPPLICATION_H
#define PHOTOR_GCAPPLICATION_H

#endif //PHOTOR_GCAPPLICATION_H

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

class GCApplication
{
public:
    enum{ NOT_SET = 0, IN_PROCESS = 1, SET = 2 };
    static const int radius = 20;
    static const int thickness = -1;

    GCApplication();
    ~GCApplication();
    static void reset();
    static void setImageAndShowId(Mat *_image, Mat *resImgMat, Mat *maskMat, jmethodID _showId );
    static void showImage(JNIEnv *env, jobject instance);
    static void mouseClick( int event, int x, int y, int flags, JNIEnv *env, jobject instance);
    static int nextIter();
    static int getIterCount() { return iterCount; }

    static void setRectInMask();
    static void setLblsInMask( int flags, Point p ,bool isPr);

    static Mat* oriImgMat;
    static Mat* resImgMat;
    static jmethodID showId;


    static Mat *mask;

    static uchar rectState, lblsState, prLblsState;
    static bool isInitialized;

    static Rect rect;
    static vector<Point> fgdPxls, bgdPxls, prFgdPxls, prBgdPxls;
    static int iterCount;
};


const Scalar RED = Scalar(0,0,255);
const Scalar PINK = Scalar(230,130,255);
const Scalar BLUE = Scalar(255,0,0);
const Scalar LIGHTBLUE = Scalar(255,255,160);
const Scalar GREEN = Scalar(0,255,0);


static void getBinMask( const Mat& comMask, Mat& binMask )
{
    if( comMask.empty() || comMask.type()!=CV_8UC1 )
        CV_Error( Error::StsBadArg, "comMask is empty or has incorrect type (not CV_8UC1)" );
    if( binMask.empty() || binMask.rows!=comMask.rows || binMask.cols!=comMask.cols )
        binMask.create( comMask.size(), CV_8UC1 );
    binMask = comMask & 1;  // 获得二进制表示的掩膜
//    comMask.copyTo(binMask);
}

static void on_mouse(int event, int x, int y, int flags,JNIEnv *env, jobject instance)
{
    GCApplication::mouseClick( event, x, y, flags ,env,instance);
}