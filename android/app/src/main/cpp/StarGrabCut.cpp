//
// Created by 许舰 on 2018/3/8.
//

#include "StarGrabCut.h"
#include "globalmatting.h"
#include "guidedfilter.h"

Mat* StarGrabCut::oriImgMat;
Mat* StarGrabCut::resImgMat;
Mat* StarGrabCut::alphaImgMask;
jmethodID StarGrabCut::showId;

int StarGrabCut::mattingPixels;
float StarGrabCut::mattingPercent = 0.1;

Mat* StarGrabCut::mask;

uchar StarGrabCut::skyRectState, StarGrabCut::groundRectState, StarGrabCut::boundaryState;
bool StarGrabCut::isInitialized;

Rect StarGrabCut::skyRect, StarGrabCut::groundRect;
int StarGrabCut::iterCount;

StarGrabCut::StarGrabCut() {

}

StarGrabCut::~StarGrabCut() {

}

void StarGrabCut::reset() {
    if (!mask->empty()) {
        mask->setTo(Scalar::all(GC_BGD));
    }

    if (!alphaImgMask->empty()) {
        alphaImgMask->setTo(Scalar::all(GC_BGD));
    }

    skyRectState = NOT_SET;
    skyRect = Rect(0, 0, 0, 0);

    groundRectState = NOT_SET;
    groundRect = Rect(0, 0, 0, 0);

    boundaryState = NOT_SET;

    mattingPixels = (int)(oriImgMat->rows * mattingPercent / 2);
}

void StarGrabCut::init(Mat *_image, Mat *_resImgMat, Mat *_maskMat, Mat *_alphaMaskMat, jmethodID _showId) {
    if (_image->empty()) {
        return;
    }

    oriImgMat = _image;
    resImgMat = _resImgMat;

    mask = _maskMat;
    mask->create(oriImgMat->size(), CV_8UC1);

    alphaImgMask = _alphaMaskMat;
    alphaImgMask->create(oriImgMat->size(), CV_8UC1);

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

//    if (!isInitialized) {
//        oriImgMat->copyTo(res);
//    } else {
//        getBinMask(*mask, binMask);
//        oriImgMat->copyTo(res, binMask); // 获得星空前景图像
//    }

//    resImgMat->create(res.rows, res.cols, res.type());
//    memcpy(resImgMat->data, res.data, resImgMat->step * resImgMat->rows);


    if (!isInitialized) {
        oriImgMat->copyTo(res);
    } else {
        oriImgMat->copyTo(res, *alphaImgMask); // 获得星空前景图像
    }

//    resImgMat->create(alphaImgMask->rows, alphaImgMask->cols, alphaImgMask->type());
//    memcpy(resImgMat->data, alphaImgMask->data, resImgMat->step * resImgMat->rows);

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

            if (j > yBoundary + mattingPixels && yBoundary + mattingPixels < oriImgMat->rows) {  // 设置抠图的背景，地面部分
                *(mask->data + j * (mask->step) + i) = 0;
            }
//            else if (yBoundary - mattingPixels <= j <= yBoundary + mattingPixels) {  // 设置边界可能在的地方（这个错哪了？怎么进不来？）
//                *(mask->data + j * (mask->step) + i) = 175;
//            }
            else if (j < yBoundary - mattingPixels && yBoundary - mattingPixels > 0) {  // 设置抠图的前景，天空部分
                *(mask->data + j * (mask->step) + i) = 255;
            } else {
                *(mask->data + j * (mask->step) + i) = 128;  // 设置边界可能在的地方
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

int StarGrabCut::nextIter()
{
    LOGD("nextIter enter");
    Mat bgdModel, fgdModel;

    Rect rect = Rect(0, 0, oriImgMat->cols, oriImgMat->rows);

//    if( isInitialized ) {
//        // mask 是最终生成的掩膜
//        grabCut( *oriImgMat, *mask, rect, bgdModel, fgdModel, 1 );
//    } else {
//        if( skyRectState != SET || groundRectState != SET)
//            return iterCount;
//
//        if( skyRectState == SET && groundRectState == SET ) {
//            grabCut( *oriImgMat, *mask, rect, bgdModel, fgdModel, 1, GC_INIT_WITH_MASK );
//        }
//        else {
//            // 如果是矩形方式选择
//            /**
//             * Mat result; // 分割结果 (4种可能取值)
//             * // 设定矩形,该矩形的长宽分别-1
//             * Rect rectangle(1, 1, image.cols - 1, image.rows - 1);
//             * grabCut(image, result, rectangle, bgModel, fgModel, 1, cv::GC_INIT_WITH_RECT);
//             * // 得到可能为前景的像素
//             * compare(result, cv::GC_PR_FGD, result, cv::CMP_EQ);  // rect方式这样获得mask
//             * // 生成输出图像
//             * Mat foreground(image.size(), CV_8UC3, cv::Scalar(255, 255, 255));
//             * image.copyTo(foreground, result); // 不复制背景数据
//             */
//            grabCut( *oriImgMat, *mask, rect, bgdModel, fgdModel, 1, GC_INIT_WITH_RECT );
//        }
//        isInitialized = true;
//    }

    // 开始抠图操作
    expansionOfKnownRegions(*oriImgMat, *mask, 9);
    globalMatting(*oriImgMat, *mask, *resImgMat, *alphaImgMask);
    *alphaImgMask = guidedFilter(*oriImgMat, *alphaImgMask, 10, 1e-5);

    for (int x = 0; x < (*mask).cols; ++x)
        for (int y = 0; y < (*mask).rows; ++y)
        {
            if ((*mask).at<uchar>(y, x) == 0)
                (*alphaImgMask).at<uchar>(y, x) = 0;
            else if ((*mask).at<uchar>(y, x) == 255)
                (*alphaImgMask).at<uchar>(y, x) = 255;
        }

    isInitialized = true;
    iterCount++;

    LOGD("nextIter leave");
    return iterCount;
};