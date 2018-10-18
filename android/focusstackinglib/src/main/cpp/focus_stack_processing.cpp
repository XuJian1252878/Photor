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

    return true;
}
