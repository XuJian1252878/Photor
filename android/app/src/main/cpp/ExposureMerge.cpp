//
// Created by 许舰 on 2018/9/27.
//
#include "ExposureMerge.h"
#include "Util.h"

void readImagesAndTimes(vector<string>& imagesPath, vector<Mat>& images) {
    for (int index = 0; index < imagesPath.size(); index ++) {
        Mat im = imread(imagesPath[index], IMREAD_UNCHANGED);
        images.push_back(im);
    }
}

int ExposureMergeProcess(vector<string>& imagesPath, vector<float>& times, Mat* ldrDurand, string generateImgAbsPath) {

    vector<Mat> images;
    readImagesAndTimes(imagesPath, images);

    // 2. 图像可能由稍微的位移现象，需要对图片进行对齐操作
    Ptr<AlignMTB> alignMTB = createAlignMTB();
    alignMTB->process(images, images);

    // 3. 提取当前相机的响应函数(CRF)
    Mat responseDebevec;
    Ptr<CalibrateDebevec> calibrateDebevec = createCalibrateDebevec();
    calibrateDebevec->process(images, responseDebevec, times);

    // 4. 将原始的输入图像合并为HDR线性图像
    Mat hdrDebevec;
    Ptr<MergeDebevec> mergeDebevec = createMergeDebevec();
    mergeDebevec->process(images, hdrDebevec, times, responseDebevec);
    // 保存hdr线性图像信息
//    imwrite("/Users/xujian/Workspace/ImageHDR/hdrDebevec.hdr", hdrDebevec);

    // 5. 对hdr图像进行色调映射
    // Since we want to see our results on common LDR display we have to map our HDR image to 8-bit range preserving most details.
    // 使用Durand色调映射算法获得24位彩色图像
    Mat tmpResMat;
//    Ptr<TonemapDurand> tonemapDurand = createTonemapDurand(1.5, 4, 1.0, 1, 1);
    Ptr<TonemapDurand> tonemapDurand = createTonemapDurand(1.9f);
    tonemapDurand->process(hdrDebevec, tmpResMat);
    tmpResMat = 2 * tmpResMat * 255;  //  这个跟亮度有关系，不做*处理的话图片可能会有些暗


    imwrite(generateImgAbsPath, tmpResMat);  // all HDR imaging functions return results in [0, 1] range so we should multiply result by 255.

    ldrDurand->create(tmpResMat.rows, tmpResMat.cols, tmpResMat.type());
    memcpy(ldrDurand->data, tmpResMat.data, tmpResMat.step * tmpResMat.rows);

    return 1;
}


int ExposureMergeProcessDrago(vector<string>& imagesPath,
                              vector<float>& times,
                              Mat* ldrDurand,
                              string generateImgAbsPath,
                              float gamma_drago,
                              float saturation_drago,
                              float bias_drago) {

    vector<Mat> images;
    readImagesAndTimes(imagesPath, images);

    // 2. 图像可能由稍微的位移现象，需要对图片进行对齐操作
    Ptr<AlignMTB> alignMTB = createAlignMTB();
    alignMTB->process(images, images);

    // 3. 提取当前相机的响应函数(CRF)
    Mat responseDebevec;
    Ptr<CalibrateDebevec> calibrateDebevec = createCalibrateDebevec();
    calibrateDebevec->process(images, responseDebevec, times);

    // 4. 将原始的输入图像合并为HDR线性图像
    Mat hdrDebevec;
    Ptr<MergeDebevec> mergeDebevec = createMergeDebevec();
    mergeDebevec->process(images, hdrDebevec, times, responseDebevec);
    // 保存hdr线性图像信息
//    imwrite("/Users/xujian/Workspace/ImageHDR/hdrDebevec.hdr", hdrDebevec);

    // 5. 对hdr图像进行色调映射
    Mat tmpResMat;
    Ptr<TonemapDrago> tonemapDrago = createTonemapDrago(gamma_drago, saturation_drago, bias_drago);
    tonemapDrago->process(hdrDebevec, tmpResMat);
    tmpResMat = 2 * tmpResMat * 255;  //  这个跟亮度有关系，不做*处理的话图片可能会有些暗


    imwrite(generateImgAbsPath, tmpResMat);  // all HDR imaging functions return results in [0, 1] range so we should multiply result by 255.

    ldrDurand->create(tmpResMat.rows, tmpResMat.cols, tmpResMat.type());
    memcpy(ldrDurand->data, tmpResMat.data, tmpResMat.step * tmpResMat.rows);

    return 1;
}


int ExposureMergeProcessDurand(vector<string>& imagesPath,
                               vector<float>& times,
                               Mat* ldrDurand,
                               string generateImgAbsPath,
                               float gamma_durand,
                               float saturation_durand,
                               float contrast_durand,
                               float sigma_space_durand,
                               float sigma_color_durand) {

    vector<Mat> images;
    readImagesAndTimes(imagesPath, images);

    // 2. 图像可能由稍微的位移现象，需要对图片进行对齐操作
    Ptr<AlignMTB> alignMTB = createAlignMTB();
    alignMTB->process(images, images);

    // 3. 提取当前相机的响应函数(CRF)
    Mat responseDebevec;
    Ptr<CalibrateDebevec> calibrateDebevec = createCalibrateDebevec();
    calibrateDebevec->process(images, responseDebevec, times);

    // 4. 将原始的输入图像合并为HDR线性图像
    Mat hdrDebevec;
    Ptr<MergeDebevec> mergeDebevec = createMergeDebevec();
    mergeDebevec->process(images, hdrDebevec, times, responseDebevec);
    // 保存hdr线性图像信息
//    imwrite("/Users/xujian/Workspace/ImageHDR/hdrDebevec.hdr", hdrDebevec);

    // 5. 对hdr图像进行色调映射
    Mat tmpResMat;
    Ptr<TonemapDurand> tonemapDurand = createTonemapDurand(gamma_durand, contrast_durand, saturation_durand, sigma_space_durand, sigma_color_durand);
    tonemapDurand->process(hdrDebevec, tmpResMat);
    tmpResMat = 2 * tmpResMat * 255;  //  这个跟亮度有关系，不做*处理的话图片可能会有些暗


    imwrite(generateImgAbsPath, tmpResMat);  // all HDR imaging functions return results in [0, 1] range so we should multiply result by 255.

    ldrDurand->create(tmpResMat.rows, tmpResMat.cols, tmpResMat.type());
    memcpy(ldrDurand->data, tmpResMat.data, tmpResMat.step * tmpResMat.rows);

    return 1;
}


int ExposureMergeProcessMantiuk(vector<string>& imagesPath,
                               vector<float>& times,
                               Mat* ldrDurand,
                               string generateImgAbsPath,
                               float gamma_mantiuk,
                               float saturation_mantiuk,
                               float scale_mantiuk) {

    vector<Mat> images;
    readImagesAndTimes(imagesPath, images);

    // 2. 图像可能由稍微的位移现象，需要对图片进行对齐操作
    Ptr<AlignMTB> alignMTB = createAlignMTB();
    alignMTB->process(images, images);

    // 3. 提取当前相机的响应函数(CRF)
    Mat responseDebevec;
    Ptr<CalibrateDebevec> calibrateDebevec = createCalibrateDebevec();
    calibrateDebevec->process(images, responseDebevec, times);

    // 4. 将原始的输入图像合并为HDR线性图像
    Mat hdrDebevec;
    Ptr<MergeDebevec> mergeDebevec = createMergeDebevec();
    mergeDebevec->process(images, hdrDebevec, times, responseDebevec);
    // 保存hdr线性图像信息
//    imwrite("/Users/xujian/Workspace/ImageHDR/hdrDebevec.hdr", hdrDebevec);

    // 5. 对hdr图像进行色调映射
    Mat tmpResMat;
    Ptr<TonemapMantiuk> tonemapMantiuk = createTonemapMantiuk(gamma_mantiuk, scale_mantiuk, saturation_mantiuk);
    tonemapMantiuk->process(hdrDebevec, tmpResMat);
    tmpResMat = 2 * tmpResMat * 255;  //  这个跟亮度有关系，不做*处理的话图片可能会有些暗


    imwrite(generateImgAbsPath, tmpResMat);  // all HDR imaging functions return results in [0, 1] range so we should multiply result by 255.

    ldrDurand->create(tmpResMat.rows, tmpResMat.cols, tmpResMat.type());
    memcpy(ldrDurand->data, tmpResMat.data, tmpResMat.step * tmpResMat.rows);

    return 1;
}


int ExposureMergeProcessReinhard(vector<string>& imagesPath,
                                vector<float>& times,
                                Mat* ldrDurand,
                                string generateImgAbsPath,
                                float gamma_reinhard,
                                float color_adapt_reinhard,
                                float light_adapt_reinhard,
                                 float intensity_reinhard) {

    vector<Mat> images;
    readImagesAndTimes(imagesPath, images);

    // 2. 图像可能由稍微的位移现象，需要对图片进行对齐操作
    Ptr<AlignMTB> alignMTB = createAlignMTB();
    alignMTB->process(images, images);

    // 3. 提取当前相机的响应函数(CRF)
    Mat responseDebevec;
    Ptr<CalibrateDebevec> calibrateDebevec = createCalibrateDebevec();
    calibrateDebevec->process(images, responseDebevec, times);

    // 4. 将原始的输入图像合并为HDR线性图像
    Mat hdrDebevec;
    Ptr<MergeDebevec> mergeDebevec = createMergeDebevec();
    mergeDebevec->process(images, hdrDebevec, times, responseDebevec);
    // 保存hdr线性图像信息
//    imwrite("/Users/xujian/Workspace/ImageHDR/hdrDebevec.hdr", hdrDebevec);

    // 5. 对hdr图像进行色调映射
    Mat tmpResMat;
    Ptr<TonemapReinhard> tonemapReinhard = createTonemapReinhard(gamma_reinhard, intensity_reinhard, light_adapt_reinhard, color_adapt_reinhard);
    tonemapReinhard->process(hdrDebevec, tmpResMat);
    tmpResMat = 2 * tmpResMat * 255;  //  这个跟亮度有关系，不做*处理的话图片可能会有些暗


    imwrite(generateImgAbsPath, tmpResMat);  // all HDR imaging functions return results in [0, 1] range so we should multiply result by 255.

    ldrDurand->create(tmpResMat.rows, tmpResMat.cols, tmpResMat.type());
    memcpy(ldrDurand->data, tmpResMat.data, tmpResMat.step * tmpResMat.rows);

    return 1;
}

