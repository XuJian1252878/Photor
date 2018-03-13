//
// Created by 许舰 on 2018/3/11.
//

#ifndef PHOTOR_UTIL_H
#define PHOTOR_UTIL_H

#endif //PHOTOR_UTIL_H

#include <iostream>
#include <opencv/cv.hpp>

#include "opencv2/xfeatures2d.hpp"

using namespace cv;
using namespace std;
using namespace cv::xfeatures2d;


Mat_<Vec3b> superimposedImg(vector<Mat_<Vec3b>>& images, Mat_<Vec3b>& trainImg);