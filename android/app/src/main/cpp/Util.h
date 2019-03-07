//
// Created by 许舰 on 2018/3/11.
//

#ifndef PHOTOR_UTIL_H
#define PHOTOR_UTIL_H

#endif //PHOTOR_UTIL_H

#include <iostream>
#include <sys/types.h>
#include <dirent.h>
#include <stdio.h>
#include <errno.h>
#include <opencv/cv.hpp>
#include <time.h>

#include "opencv2/xfeatures2d.hpp"

using namespace cv;
using namespace std;
using namespace cv::xfeatures2d;

Mat_<Vec3b> addMeanImgs(std::vector<Mat_<Vec3b>>& sourceImages);

Mat_<Vec3b> addMeanImgs(std::vector<Mat_<Vec3b>>& sourceImages, Mat_<Vec3b>& targetImage);

Mat_<Vec3b> superimposedImg(vector<Mat_<Vec3b>>& images, Mat_<Vec3b>& trainImg);

Mat_<Vec3b> superimposedImg(Mat_<Vec3b>& queryImg, Mat_<Vec3b>& trainImg);

/**
 * 根据配准参数homo，获得queryImg根据配准参数 变换 后的图像
 * @param queryImg
 * @param homo
 * @return
*/
Mat_<Vec3b> getTransformImgByHomo(Mat_<Vec3b>& queryImg, Mat homo);

/**
 * 将 uchar Mat矩阵中 <127的像素设置为0 大于127的像素设置为255
 * @param mask 传入的图片mask图像信息
 * @return
 */
bool adjustMaskPixel(Mat& mask);


/**
 * 对一组图像进行图像配准操作
 * @param images 引用参数，函数调用后每幅图像都为配准之后的图像
 * @param baseIndex 以images 中第 baseIndex 个图像为基准进行配准操作
 */
void registrationImages(vector<Mat>& images,int baseIndex);