//
// Created by qiulinmin on 17-5-15.
//

#ifndef IMG_ANDROID_UTILS_H
#define IMG_ANDROID_UTILS_H

#include <iostream>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

void bitmap_to_mat(JNIEnv *env, jobject &srcBitmap, Mat &srcMat);

void mat_to_bitmap(JNIEnv *env, Mat &srcMat, jobject &dstBitmap);


//inline int fast_abs(uchar v) { return v; }
//inline int fast_abs(schar v) { return std::abs((int)v); }
//inline int fast_abs(ushort v) { return v; }
//inline int fast_abs(short v) { return std::abs((int)v); }
//inline int fast_abs(int v) { return std::abs(v); }
//inline float fast_abs(float v) { return std::abs(v); }
//inline double fast_abs(double v) { return std::abs(v); }

#endif //IMG_ANDROID_UTILS_H
