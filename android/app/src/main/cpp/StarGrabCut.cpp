//
// Created by 许舰 on 2018/3/8.
//

#include "StarGrabCut.h"

Mat* StarGrabCut::oriImgMat;
Mat* StarGrabCut::resImgMat;
jmethodID StarGrabCut::showId;


Mat* StarGrabCut::mask;

uchar StarGrabCut::skyRectState, StarGrabCut::groundRectState;
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

    skyRectState = NOT_SET;
    skyRect = Rect(0, 0, 0, 0);

    groundRectState = NOT_SET;
    groundRect = Rect(0, 0, 0, 0);
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

    resImgMat->create(res.rows, res.cols, res.type());
    memcpy(resImgMat->data, res.data, resImgMat->step * resImgMat->rows);

    env->CallVoidMethod(instance, showId);
}

// 根据用户在手机屏幕上指定的区域确定前景以及背景信息
// event: DOWN = 0, UP = 1, MOVE = 2
// flags: 1 前景；2 背景
void StarGrabCut::mouseClick( int event, int x, int y, int flags, JNIEnv *env, jobject instance) {

    LOGD("TAG C++: %d \t %d", x, y);

    switch(event) {
        case 0:  // down
            if (flags == SKY && skyRectState == NOT_SET) {
                skyRect = Rect(x, y, 1, 1);
                skyRectState = IN_PROCESS;
            } else if (flags == GROUND && groundRectState == NOT_SET) {
                groundRect = Rect(x, y, 1, 1);
                groundRectState = IN_PROCESS;
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
            }
            break;
        case 2: // move
            if (flags == SKY && skyRectState == IN_PROCESS) {
                // 更新前景矩形的范围（先考虑从上到下划的情况）
                skyRect = Rect( Point(skyRect.x, skyRect.y), Point(x, y) );
            } else if (flags == GROUND && groundRectState == IN_PROCESS) {
                // 更新背景矩形范围（先考虑从上到下划的情况）
                groundRect = Rect( Point(groundRect.x, groundRect.y), Point(x, y));
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
    ((*mask)(skyRect)).setTo(Scalar(GC_FGD));
}

void StarGrabCut::setGroundRectInMask() {
    CV_Assert(!mask->empty());
    groundRect = Rect(0, groundRect.y, oriImgMat->cols, oriImgMat->rows - groundRect.y);
    ((*mask)(groundRect)).setTo(Scalar(GC_BGD));
}

int StarGrabCut::nextIter()
{
    LOGD("nextIter enter");
    Mat bgdModel, fgdModel;

    Rect rect = Rect(0, 0, oriImgMat->cols, oriImgMat->rows);

    if( isInitialized )
        // mask 是最终生成的掩膜
        grabCut( *oriImgMat, *mask, rect, bgdModel, fgdModel, 1 );
    else
    {
        if( skyRectState != SET || groundRectState != SET)
            return iterCount;

        if( skyRectState == SET && groundRectState == SET )
            grabCut( *oriImgMat, *mask, rect, bgdModel, fgdModel, 1, GC_INIT_WITH_MASK );
        else
            // 如果是矩形方式选择
            /**
             * Mat result; // 分割结果 (4种可能取值)
             * // 设定矩形,该矩形的长宽分别-1
             * Rect rectangle(1, 1, image.cols - 1, image.rows - 1);
             * grabCut(image, result, rectangle, bgModel, fgModel, 1, cv::GC_INIT_WITH_RECT);
             * // 得到可能为前景的像素
             * compare(result, cv::GC_PR_FGD, result, cv::CMP_EQ);  // rect方式这样获得mask
             * // 生成输出图像
             * Mat foreground(image.size(), CV_8UC3, cv::Scalar(255, 255, 255));
             * image.copyTo(foreground, result); // 不复制背景数据
             */
            grabCut( *oriImgMat, *mask, rect, bgdModel, fgdModel, 1, GC_INIT_WITH_RECT );

        isInitialized = true;
    }
    iterCount++;

    LOGD("nextIter leave");
    return iterCount;
};