//
// Created by 许舰 on 2018/11/16.
//

#ifndef PHOTOR_LAPLACIAN_BLENDING_H
#define PHOTOR_LAPLACIAN_BLENDING_H


#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>
#include "gaussian_kernel.h"
#include "image_filter.h"

using namespace cv;
using namespace std;

/*************************************************/
/*说明:
*金字塔从下到上依次为 [0,1,....,level-1]层
*resultLapPyr 存放每层金字塔中直接用左右图Laplacian变换拼成的图像
*/
/****************************************************************/

class LaplacianBlending {
private:
    vector<Mat_<Vec3f>> imgs;
    vector<Mat_<Vec3f> > resultLapPyr;//Laplacian Pyramids
    vector<vector<Mat_<Vec3f>> > lapPyrs;

    Mat_<Vec3f> resultHighestLevel;
    vector<Mat_<Vec3f> > highestLevels;

    GaussianKernel gaussian_kernel;

    int levels;
    int kernels_size;
    float gaussian_sigma;

    void buildPyramids();

    // 图像下采样
    void pyrDownCustom( Mat src, Mat dst);
    // 图像上采样
    void pyrUpCustom( Mat src, Mat dst, const Size& dstsize = Size());


    void buildLaplacianPyramid(const vector<Mat_<Vec3f> >& imgs, vector<vector<Mat_<Vec3f>> >& lapPyrs, vector<Mat_<Vec3f>>& HighestLevels);

    Mat_<Vec3f> reconstructImgFromLapPyramid();

    void fusionLapPyrs();


public:
    LaplacianBlending(const vector<Mat_<Vec3f>>& _left, int _levels,int _kernels_size, float _gaussian_sigma) {
        imgs = _left;
        levels = _levels;
        kernels_size = _kernels_size;
        gaussian_sigma = _gaussian_sigma;


        buildPyramids();  //construct Laplacian Pyramid and Gaussian Pyramid
        fusionLapPyrs(); // 拉普拉斯金字塔融合

        gaussian_kernel = GaussianKernel(kernels_size, gaussian_sigma);

    };

    Mat_<Vec3f> blend();
};

Mat_<Vec3f> LaplacianBlend(const vector<Mat_<Vec3f>>& l,int level, int kernels_size, int gaussian_sigma);

void readImagesFromDirToFocusStack(std::string images_dir_path, vector<Mat_<Vec3f> >& focus_stack);

#endif //PHOTOR_LAPLACIAN_BLENDING_H
