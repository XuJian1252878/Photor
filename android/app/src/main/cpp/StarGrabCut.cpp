//
// Created by 许舰 on 2018/3/8.
//

#include "StarGrabCut.h"

Mat* StarGrabCut::oriImgMat;
Mat* StarGrabCut::resImgMat;
jmethodID StarGrabCut::showId;


Mat* StarGrabCut::mask;

uchar StarGrabCut::skyRectState, StarGrabCut::groundRectState, StarGrabCut::boundaryState;
bool StarGrabCut::isInitialized;

Rect StarGrabCut::skyRect, StarGrabCut::groundRect;

StarGrabCut::StarGrabCut() {

}

StarGrabCut::~StarGrabCut() {

}

void StarGrabCut::reset() {
    if (!mask->empty()) {
        mask->setTo(Scalar::all(GC_BGD));
    }

    skyRectState = NOT_SET;
    skyRect = Rect(0, 0, 0, 0);

    groundRectState = NOT_SET;
    groundRect = Rect(0, 0, 0, 0);


    boundaryState = NOT_SET;
}

void StarGrabCut::init(Mat *_image, Mat *_resImgMat, Mat *maskMat, jmethodID _showId) {
    if (_image->empty()) {
        return;
    }

    oriImgMat = _image;
    resImgMat = _resImgMat;

    mask = maskMat;
    mask->create(oriImgMat->size(), CV_8UC1);

    showId = _showId;
    reset();
}

static void getBinMask( const Mat& comMask, Mat& binMask )
{
    if( comMask.empty() || comMask.type()!=CV_8UC1 )
        CV_Error( Error::StsBadArg, "comMask is empty or has incorrect type (not CV_8UC1)" );
    if( binMask.empty() || binMask.rows!=comMask.rows || binMask.cols!=comMask.cols )
        binMask.create( comMask.size(), CV_8UC1 );
    binMask = comMask & 1;  // 获得二进制表示的掩膜
}

void StarGrabCut::showImage(JNIEnv *env, jobject instance) {
    if (oriImgMat->empty()) {
        return;
    }

    Mat res;
    Mat binMask;

    if (!isInitialized) {
        oriImgMat->copyTo(res);
    } else {
        getBinMask(*mask, binMask);
        oriImgMat->copyTo(res, binMask); // 获得星空前景图像
    }

//    resImgMat->create(res.rows, res.cols, res.type());
//    memcpy(resImgMat->data, res.data, resImgMat->step * resImgMat->rows);


    resImgMat->create(mask->rows, mask->cols, mask->type());
    memcpy(resImgMat->data, mask->data, resImgMat->step * resImgMat->rows);

    env->CallVoidMethod(instance, showId);
}

// 传入的参数以左下角为坐标原点
float StarGrabCut::calcSlop(int x, int y, int lastX, int lastY) {
    return (float)((y - lastY) * 1.0 / (x - lastX));
}

// 传入的参数以左上角为坐标原点
void StarGrabCut::setPtrAreaInMask(int x, int y, int lastX, int lastY) {

    if (x == lastX) {
        return;
    }

    float k = calcSlop(x, y, lastX, lastY);
    float b = y - k * x;

    for (int i = lastX; i <= x; i ++) {

        int yBoundary = (int)(k * i + b);

        LOGD("TAG Last yBoundary: %d", yBoundary);

        for (int j = 0; j < oriImgMat->rows; j ++) {

            if (j > yBoundary) {
                *(mask->data + j * (mask->step) + i) = 255;
            }

        }
    }
}

// 根据用户在手机屏幕上指定的区域确定前景以及背景信息
// event: DOWN = 0, UP = 1, MOVE = 2
// flags: 1 前景；2 背景；3 分界线
void StarGrabCut::mouseClick( int event, int x, int y, int flags, int lastX, int lastY, JNIEnv *env, jobject instance) {

    LOGD("TAG C++: %d \t %d", x, y);
    LOGD("TAG Last C++: %d \t %d", lastX, lastY);

    switch(event) {
        case 0:  // down
            if (flags == SKY && skyRectState == NOT_SET) {
                skyRect = Rect(x, y, 1, 1);
                skyRectState = IN_PROCESS;
            } else if (flags == GROUND && groundRectState == NOT_SET) {
                groundRect = Rect(x, y, 1, 1);
                groundRectState = IN_PROCESS;
            } else if (flags == BOUNDARY && boundaryState == NOT_SET) {
                boundaryState = IN_PROCESS;
                setPtrAreaInMask(x, y, 0, y); // 第一个触点选择 x 方向平齐的点做连线
            }
            break;
        case 1: // up
            if (flags == SKY && skyRectState == IN_PROCESS) {
                skyRectState = SET;
                skyRect = Rect( Point(skyRect.x, skyRect.y), Point(x, y) );
                setSkyRectInMask();
            } else if (flags == GROUND && groundRectState == IN_PROCESS) {
                groundRectState = SET;
                groundRect = Rect( Point(groundRect.x, groundRect.y), Point(x, y));
                setGroundRectInMask();
            } else if (flags == BOUNDARY && lastX != NOT_INIT_POSITION && lastY != NOT_INIT_POSITION && boundaryState == IN_PROCESS) {
                boundaryState = SET;
                setPtrAreaInMask(x, y, lastX, lastY); // 第一个触点选择 x 方向平齐的点做连线
                setPtrAreaInMask(oriImgMat->cols, lastY, x, y);  // 跟最后一点连线
            }
            break;
        case 2: // move
            if (flags == SKY && skyRectState == IN_PROCESS) {
                // 更新前景矩形的范围（先考虑从上到下划的情况）
                skyRect = Rect( Point(skyRect.x, skyRect.y), Point(x, y) );
            } else if (flags == GROUND && groundRectState == IN_PROCESS) {
                // 更新背景矩形范围（先考虑从上到下划的情况）
                groundRect = Rect( Point(groundRect.x, groundRect.y), Point(x, y));
            } else if (flags == BOUNDARY && lastX != NOT_INIT_POSITION && lastY != NOT_INIT_POSITION && boundaryState == IN_PROCESS) {
                setPtrAreaInMask(x, y, lastX, lastY); // 正常做连线操作
            }
            break;
        default:
            break;
    }
}

void StarGrabCut::setSkyRectInMask() {
    CV_Assert(!mask->empty());
    skyRect = Rect(0, 0, oriImgMat->cols, skyRect.y + skyRect.height);
    // 将选定的星空部分设置为确定的前景色信息
    ((*mask)(skyRect)).setTo(255);
}

void StarGrabCut::setGroundRectInMask() {
    CV_Assert(!mask->empty());
    groundRect = Rect(0, groundRect.y, oriImgMat->cols, oriImgMat->rows - groundRect.y);
    ((*mask)(groundRect)).setTo(Scalar(0));
}