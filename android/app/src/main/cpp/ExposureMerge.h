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


/**
 * 曝光合成的流程
 * @param imagesPath 原始的曝光值不同的图片路径
 * @param times 不同index图片的不同曝光时间
 * @param ldrDurand 最终结果存储的Mat
 * @param generateImgAbsPath 最终结果存储的路径
 * @return
 */
int ExposureMergeProcess(vector<string>& imagesPath, vector<double>& times, Mat* ldrDurand, string generateImgAbsPath);

/**
 *
 * @param imagesPath 原始的曝光值不同的图片路径
 * @param images 原始的曝光值不同的图片Mat
 */
void readImagesAndTimes(vector<string>& imagesPath, vector<Mat>& images);