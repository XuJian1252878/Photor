//
// Created by 许舰 on 2018/10/18.
//

#include <jni.h>
#include <iostream>
#include <chrono>
#include <cstdlib>
#include <cstdint>

#include "images_utils.h"
#include "focus_stack.h"
#include "laplacian_blending.h"

using namespace std;
using namespace cv;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_focusstackinglib_FocusStackProcessing_nativeFocusStackImage1(JNIEnv *env, jobject instance, jobject inputImagePaths, jlong outAddr, jshort kernels_size, jfloat gaussian_sigma, jstring resImgPath) {

    // 获取ArrayList对象的class
    jclass arrayList = static_cast<jclass>(env->FindClass("java/util/ArrayList"));
    jmethodID arrayListSize = env->GetMethodID(arrayList, "size", "()I");
    jmethodID arrayListGet = env->GetMethodID(arrayList, "get", "(I)Ljava/lang/Object;");

    int imgPathSize = env->CallIntMethod(inputImagePaths, arrayListSize);
    jboolean isCopyStr = JNI_FALSE;
    vector<string> imgPathVec;
    for (int index = 0; index < imgPathSize; index ++) {
        const char* sourcePhotoPathPtr = env->GetStringUTFChars(
                static_cast<jstring>(env->CallObjectMethod(inputImagePaths, arrayListGet, index)),
                &isCopyStr);

        imgPathVec.push_back(string(sourcePhotoPathPtr));
    }

    // 景深合成信息
    vector<Mat_<Vec3f>> focus_stack;
    for (int index = 0; index < imgPathVec.size(); index ++) {
        focus_stack.push_back(imread(imgPathVec[index], CV_LOAD_IMAGE_UNCHANGED));
    }
    Mat_<Vec3f> all_in_focus_image = LaplacianBlend(focus_stack, 3, kernels_size, gaussian_sigma);

    // 生成结果Mat存储路径
    const char *generateImgAbsPath_ = env->GetStringUTFChars(resImgPath, 0); // 存储对齐图片的路径信息
    string generateImgAbsPath = string(generateImgAbsPath_);
    imwrite(generateImgAbsPath, all_in_focus_image);

    return JNI_TRUE;
}
