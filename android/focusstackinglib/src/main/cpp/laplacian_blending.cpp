//
// Created by 许舰 on 2018/11/16.
//


#include "laplacian_blending.h"
#include "images_utils.h"
#include "grayscale_converter.h"

void LaplacianBlending::buildPyramids() {
    buildLaplacianPyramid(imgs, lapPyrs, highestLevels);
}


void LaplacianBlending::buildLaplacianPyramid(const vector<Mat_<Vec3f> >& imgs, vector<vector<Mat_<Vec3f>> >& lapPyrs, vector<Mat_<Vec3f>>& HighestLevels){

    lapPyrs.clear();
    for (Mat_<Vec3f> img: imgs) {
        // 对于每一副图像，构建拉普拉斯金字塔
        vector<Mat_<Vec3f>> lapPyr;
        Mat currentImg = img;
        for (int l=0; l<levels; l++) {
            Mat down,up;
            pyrDownCustom(currentImg, down);
            pyrUpCustom(down, up,currentImg.size());
            Mat lap = currentImg - up;
            lapPyr.push_back(lap);
            currentImg = down;
        }

        // 存储最高层的拉普拉斯金字塔
        Mat HighestLevel;
        currentImg.copyTo(HighestLevel);
        HighestLevels.push_back(HighestLevel);

        lapPyrs.push_back(lapPyr);
    }

}

Mat_<Vec3f> LaplacianBlending::reconstructImgFromLapPyramid(){
    Mat currentImg = resultHighestLevel;
    for(int l=levels-1; l>=0; l--){
        Mat up;
        pyrUpCustom(currentImg, up, resultLapPyr[l].size());
        currentImg = up + resultLapPyr[l];
    }
    return currentImg;
}


void LaplacianBlending::fusionLapPyrs() {
    // 融合拉普拉斯金字塔
    // 融合拉普拉斯金字塔顶层（最高层系数取平均）
    int topRows = highestLevels[0].rows;
    int topCols = highestLevels[0].cols;

    resultHighestLevel = Mat(highestLevels[0].rows, highestLevels[0].cols, highestLevels[0].type());

    int size = highestLevels.size();

    for (int i = 0; i < topCols; i ++) {
        for (int j = 0; j < topRows; j ++) {

            // 最高层系数的处理（求平均）
            float curPixelR = 0.0F, curPixelG = 0.0F, curPixelB = 0.0F;
            for (int k = 0; k < highestLevels.size(); k ++) {
                curPixelR += highestLevels[k].at<Vec3f>(j, i)[2];
                curPixelG += highestLevels[k].at<Vec3f>(j, i)[1];
                curPixelB += highestLevels[k].at<Vec3f>(j, i)[0];
            }
            resultHighestLevel.at<Vec3f>(j, i) = Vec3f(curPixelB/size, curPixelG/size, curPixelR/size);
        }
    }



    // 其余层系数的处理（取最大）
    for (int l = 0; l < levels; l ++) {
        // 获取每一层图像的大小
        int rows = lapPyrs[0][l].rows;
        int cols = lapPyrs[0][l].cols;

        Mat_<Vec3f> tmp = Mat(lapPyrs[0][l].rows, lapPyrs[0][l].cols, lapPyrs[0][l].type());;

        // 对于对应层的拉普拉斯金字塔      resultLapPyr
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {

                float maxK = 0, maxGrayPixel = 0;
                for (int k = 0; k < lapPyrs.size(); k++) {

                    float tmpGrayPixel = abs(0.299F * lapPyrs[k][l].at<Vec3f>(j, i)[2] +
                                             0.587F * lapPyrs[k][l].at<Vec3f>(j, i)[1] +
                                             0.114F * lapPyrs[k][l].at<Vec3f>(j, i)[0]);

                    if (tmpGrayPixel > maxGrayPixel) {
                        maxGrayPixel = tmpGrayPixel;
                        maxK = k;
                    }
                }
                tmp.at<Vec3f>(j, i) = Vec3f(lapPyrs[maxK][l].at<Vec3f>(j, i)[0],
                                            lapPyrs[maxK][l].at<Vec3f>(j, i)[1],
                                            lapPyrs[maxK][l].at<Vec3f>(j, i)[2]);
            }
        }
        resultLapPyr.push_back(tmp);
    }

}

Mat_<Vec3f> LaplacianBlending::blend() {
    return reconstructImgFromLapPyramid();//reconstruct Image from Laplacian Pyramid
}


// 图像下采样
void LaplacianBlending::pyrDownCustom(Mat src, Mat dst) {

    // 高斯模糊
    ImageFilter<uint8_t, uint8_t> gaussian_filter = ImageFilter<uint8_t, uint8_t>(gaussian_kernel);
    Matrix<uint8_t> srcMatrix = images_utils::cvMat2Matrix(src);
    Matrix<uint8_t> blurred_image = gaussian_filter.convolution(srcMatrix);
    images_utils::matrix2CvMat(blurred_image, src);

    // 下采样
    pyrDown(src, dst);
}


// 图像上采样
void LaplacianBlending::pyrUpCustom(Mat src, Mat dst, const Size& dstsize) {
    // 上采样
    pyrUp(src, dst, dstsize);
    // 高斯模糊【高斯核变成原来的四倍】
    gaussian_kernel.changeMultiple(4);
    ImageFilter<uint8_t, uint8_t> gaussian_filter = ImageFilter<uint8_t, uint8_t>(gaussian_kernel);

    Matrix<uint8_t> srcMatrix = images_utils::cvMat2Matrix(src);
    Matrix<uint8_t> blurred_image = gaussian_filter.convolution(srcMatrix);
    images_utils::matrix2CvMat(blurred_image, src);

    // 将高斯核恢复原样
    gaussian_kernel.changeMultiple(1 / 4);
}


/**
 * 景深合成功能函数
 * @param l
 * @param level 拉普拉斯金字塔层数
 * @param kernels_size 高斯模糊模版大小
 * @param gaussian_sigma 高斯模糊中sigma的取值
 * @return
 */
Mat_<Vec3f> LaplacianBlend(const vector<Mat_<Vec3f>>& l,int level, int kernels_size, int gaussian_sigma) {
    LaplacianBlending lb(l, level, kernels_size, gaussian_sigma);
    return lb.blend();
}


void readImagesFromDirToFocusStack(std::string images_dir_path, vector<Mat_<Vec3f> >& focus_stack) {

    std::vector<cv::String> filenames;
    cv::glob(cv::String(images_dir_path), filenames);
    std::sort(filenames.begin(), filenames.end());

    for (auto& filename : filenames) {
        cv::Mat image = cv::imread(filename, CV_LOAD_IMAGE_UNCHANGED);
//        std::cout << "reading image: " << filename << std::endl;

        Mat_<Vec3f> l;
        image.convertTo(l,CV_32F, 1.0 / 255.0);
        focus_stack.push_back(l);
    }
}

