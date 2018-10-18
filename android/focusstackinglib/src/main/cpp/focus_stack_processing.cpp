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

using namespace std;
using namespace cv;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_focusstackinglib_FocusStackProcessing_nativeFocusStackImage(JNIEnv *env, jobject instance, jobject inputImagePaths, jlong outAddr, jint bg_threshold, jshort kernels_size, jfloat gaussian_sigma) {

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
    FocusStack focus_stack;
    try {
        images_utils::readImagesFromPathsToFocusStack(imgPathVec, focus_stack);
    } catch (exception& e) {
        return false;
    }

    auto start = std::chrono::steady_clock::now();

    focus_stack.computeAllInFocusAndDepthMap((unsigned short)kernels_size, gaussian_sigma, bg_threshold);

    auto duration = std::chrono::duration<double, std::milli>(std::chrono::steady_clock::now() - start);
    std::cout << "processing time: " << duration.count() << " ms" << std::endl;

    Matrix<uint8_t> all_in_focus_image = focus_stack.getAllInFocusImage();
    // 记录当前获得的景深合成Mat信息
    *((Mat*) outAddr) = images_utils::matrix2CvMat(all_in_focus_image);

    return JNI_TRUE;
}
