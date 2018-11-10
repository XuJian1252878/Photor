//
// Created by 许舰 on 2018/9/27.
//

#ifndef PHOTOR_EXPOSUREMERGE_H
#define PHOTOR_EXPOSUREMERGE_H

#endif //PHOTOR_EXPOSUREMERGE_H

#include <iostream>
#include <opencv/cv.hpp>

using namespace cv;
using namespace std;

#include <android/log.h>
#define  LOG_TAG    "JNI_PART"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG, __VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG, __VA_ARGS__)


/**
 * 曝光合成的流程
 * @param imagesPath 原始的曝光值不同的图片路径
 * @param times 不同index图片的不同曝光时间
 * @param ldrDurand 最终结果存储的Mat
 * @param generateImgAbsPath 最终结果存储的路径
 * @return
 */
int ExposureMergeProcess(vector<string>& imagesPath, vector<float>& times, Mat* ldrDurand, string generateImgAbsPath);

/**
 *
 * @param imagesPath 原始的曝光值不同的图片路径
 * @param images 原始的曝光值不同的图片Mat
 */
void readImagesAndTimes(vector<string>& imagesPath, vector<Mat>& images);


int ExposureMergeProcessDrago(vector<string>& imagesPath,
                              vector<float>& times,
                              Mat* ldrDurand,
                              string generateImgAbsPath,
                              float gamma_drago,
                              float saturation_drago,
                              float bias_drago);


int ExposureMergeProcessDurand(vector<string>& imagesPath,
                               vector<float>& times,
                               Mat* ldrDurand,
                               string generateImgAbsPath,
                               float gamma_durand,
                               float saturation_durand,
                               float contrast_durand,
                               float sigma_space_durand,
                               float sigma_color_durand);


int ExposureMergeProcessMantiuk(vector<string>& imagesPath,
                                vector<float>& times,
                                Mat* ldrDurand,
                                string generateImgAbsPath,
                                float gamma_mantiuk,
                                float saturation_mantiuk,
                                float scale_mantiuk);

int ExposureMergeProcessReinhard(vector<string>& imagesPath,
                                 vector<float>& times,
                                 Mat* ldrDurand,
                                 string generateImgAbsPath,
                                 float gamma_reinhard,
                                 float color_adapt_reinhard,
                                 float light_adapt_reinhard,
                                 float intensity_reinhard);
