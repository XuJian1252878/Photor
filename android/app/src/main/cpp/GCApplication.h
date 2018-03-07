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
    void reset();
    void setImageAndShowId(Mat *_image, jmethodID _showId );
    void showImage(JNIEnv *env, jobject instance) const;
    void mouseClick( int event, int x, int y, int flags, JNIEnv *env, jobject instance);
    int nextIter();
    int getIterCount() const { return iterCount; }
private:
    void setRectInMask();
    void setLblsInMask( int flags, Point p ,bool isPr);

    const Mat* image;
    jmethodID showId;


    Mat mask;
    Mat bgdModel, fgdModel;

    uchar rectState, lblsState, prLblsState;
    bool isInitialized;

    Rect rect;
    vector<Point> fgdPxls, bgdPxls, prFgdPxls, prBgdPxls;
    int iterCount;
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
    binMask = comMask & 1;
}

static void on_mouse(GCApplication *gcapp, int event, int x, int y, int flags,JNIEnv *env, jobject instance)
{
    gcapp->mouseClick( event, x, y, flags ,env,instance);
}