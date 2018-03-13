#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/xfeatures2d.hpp>
#include <bits/stdc++.h>
#include "StarImageRegistBuilder.h"
#include "GCApplication.h"
#include "StarGrabCut.h"
#include "Util.h"

#include <android/log.h>
#define  LOG_TAG    "JNI_PART"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG, __VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG, __VA_ARGS__)

using namespace std;
using namespace cv;
using namespace cv::xfeatures2d;


extern "C"
JNIEXPORT jstring JNICALL
Java_com_photor_base_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_photor_base_activity_test_OpencvTestActivity_stringFromJNI(JNIEnv *env, jobject instance) {
    std::string hello = "hello from opencv test!";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_photor_base_activity_test_OpencvTestActivity_nativeProcessFrame(JNIEnv *env, jobject instance,
                                                                         jlong addrGray,
                                                                         jlong addrRgba) {

    Mat &mGr = *(Mat *) addrGray;
    Mat &mRgb = *(Mat *) addrRgba;
    vector<KeyPoint> v;

    int minHessian = 50;
    Ptr<SURF> detector = SURF::create( minHessian );

//    Ptr<FeatureDetector> detector = FastFeatureDetector::create(50);

    detector->detect(mGr, v);
    for (unsigned int i = 0; i < v.size(); i++) {
        const KeyPoint &kp = v[i];
        circle(mRgb, Point((int)kp.pt.x, (int)kp.pt.y), 10, Scalar(0, 255, 255, 255));
    }
}

///**
// * return: 对齐成功返回MAT
// */
extern "C"
 JNIEXPORT jint JNICALL
 Java_com_photor_staralign_task_StarPhotoAlignTask_alignStarPhotos(JNIEnv *env, jobject instance,
                                                                   jobject starPhotos,
                                                                   jint alignBasePhotoIndex,
                                                                   jlong alignResMatAddr) {

     // 获取ArrayList对象的class
     jclass photoArrayList = static_cast<jclass>(env->FindClass("java/util/ArrayList"));
     jmethodID photoArrayListSize = env->GetMethodID(photoArrayList, "size", "()I");
     jmethodID photoArrayListGet = env->GetMethodID(photoArrayList, "get", "(I)Ljava/lang/Object;");

     int starPhotoSize = env->CallIntMethod(starPhotos, photoArrayListSize);

     if (photoArrayList == NULL) {
         return -2;  // 表示没有选择要对齐的星空图片
     } else if (starPhotoSize < 2) {
         return -1;  // 表示没有足够的图片进行对齐
     }


     jboolean isCopyStr = JNI_FALSE;
     Mat* resMatPtr = (Mat*) alignResMatAddr; // 存储图片对齐的结果信息
     const char* basicPhotoPathPtr = env->GetStringUTFChars(
             static_cast<jstring>(env->CallObjectMethod(starPhotos, photoArrayListGet, alignBasePhotoIndex)),
             &isCopyStr);

     // 指明图片分块的策略 5 * 5
     int rowParts = 5;
     int columnParts = 5;

     string test = string(basicPhotoPathPtr);

     Mat_<Vec3b> targetImage = imread(string(basicPhotoPathPtr), IMREAD_UNCHANGED);
     std::vector<Mat_<Vec3b>> sourceImages;
     for (int index = 0; index < starPhotoSize; index ++) {
         if (index == alignBasePhotoIndex)  // jint 和 int相互比较
             continue;
         const char* sourcePhotoPathPtr = env->GetStringUTFChars(
                 static_cast<jstring>(env->CallObjectMethod(starPhotos, photoArrayListGet, index)),
                 &isCopyStr);
         sourceImages.push_back(imread(string(sourcePhotoPathPtr), IMREAD_UNCHANGED));
     }

//     StarImageRegistBuilder starImageRegistBuilder = StarImageRegistBuilder(targetImage, sourceImages, rowParts, columnParts);
//     Mat_<Vec3b> resultImage = starImageRegistBuilder.registration(StarImageRegistBuilder::MERGE_MODE_MEAN);
//
//     resMatPtr->create(resultImage.rows, resultImage.cols, resultImage.type());
//     memcpy(resMatPtr->data, resultImage.data, resMatPtr->step * resMatPtr->rows);

     return 1; // 表示成功放回对齐之后的图像信息

 }

extern "C"
JNIEXPORT jint JNICALL
Java_com_photor_staralign_task_StarPhotoAlignThread_alignStarPhotos(JNIEnv *env, jobject instance,
                                                                    jobject starPhotos,
                                                                    jint alignBasePhotoIndex,
                                                                    jlong alignResMatAddr,
                                                                    jstring maskImgPath_,
                                                                    jstring generateImgAbsPath_) {

    const char *generateImgAbsPath = env->GetStringUTFChars(generateImgAbsPath_, 0); // 存储对齐图片的路径信息
    const char *maskImgPath = env->GetStringUTFChars(maskImgPath_, 0); // 图片的掩膜信息

    // 获取ArrayList对象的class
    jclass photoArrayList = static_cast<jclass>(env->FindClass("java/util/ArrayList"));
    jmethodID photoArrayListSize = env->GetMethodID(photoArrayList, "size", "()I");
    jmethodID photoArrayListGet = env->GetMethodID(photoArrayList, "get", "(I)Ljava/lang/Object;");

    int starPhotoSize = env->CallIntMethod(starPhotos, photoArrayListSize);

    if (photoArrayList == NULL) {
        return -2;  // 表示没有选择要对齐的星空图片
    } else if (starPhotoSize < 2) {
        return -1;  // 表示没有足够的图片进行对齐
    }


    jboolean isCopyStr = JNI_FALSE;
    Mat* resMatPtr = (Mat*) alignResMatAddr; // 存储图片对齐的结果信息
    const char* basicPhotoPathPtr = env->GetStringUTFChars(
            static_cast<jstring>(env->CallObjectMethod(starPhotos, photoArrayListGet, alignBasePhotoIndex)),
            &isCopyStr);

    // 指明图片分块的策略 5 * 5
    int rowParts = 5;
    int columnParts = 5;

    Mat_<Vec3b> targetImage = imread(string(basicPhotoPathPtr), IMREAD_UNCHANGED);  // 获得基准的图像信息
    Mat groundMaskImg = imread(string(maskImgPath), IMREAD_UNCHANGED);

//    groundMaskImg = groundMaskImg & 1;  // 获得可以分割地面图片的模板
    Mat skyMaskImg = ~ groundMaskImg;  // 获得可以风格的天空图片

    // 基准星空部分图片
    Mat_<Vec3b> skyTargetImg;
    targetImage.copyTo(skyTargetImg, skyMaskImg);

    // 基准地面部分图片
    Mat_<Vec3b> groundTargetImg;
    targetImage.copyTo(groundTargetImg, groundMaskImg);

    // 待配准的图片列表
    std::vector<Mat_<Vec3b>> skySourceImages;
    std::vector<Mat_<Vec3b>> groundSourceImages;

    for (int index = 0; index < starPhotoSize; index ++) {
        if (index == alignBasePhotoIndex)  // jint 和 int相互比较
            continue;
        const char* sourcePhotoPathPtr = env->GetStringUTFChars(
                static_cast<jstring>(env->CallObjectMethod(starPhotos, photoArrayListGet, index)),
                &isCopyStr);

        Mat_<Vec3b> imgMat = imread(string(sourcePhotoPathPtr), IMREAD_UNCHANGED);
        Mat_<Vec3b> skyImgMat;
        imgMat.copyTo(skyImgMat, skyMaskImg);

        Mat_<Vec3b> groundImgMat;
        imgMat.copyTo(groundImgMat, groundMaskImg);

        skySourceImages.push_back(skyImgMat);
        groundSourceImages.push_back(groundImgMat);
    }

    // 对星空部分进行对齐操作
    StarImageRegistBuilder starImageRegistBuilder = StarImageRegistBuilder(skyTargetImg, skySourceImages, skyMaskImg, rowParts, columnParts);
    Mat_<Vec3b> resSkyMat_ = starImageRegistBuilder.registration(StarImageRegistBuilder::MERGE_MODE_MEAN);
    Mat_<Vec3b> resSkyMat;
    resSkyMat_.copyTo(resSkyMat, skyMaskImg);

    // 对地面部分进行对齐操作
    Mat_<Vec3b> resGroundMat = superimposedImg(groundSourceImages, groundTargetImg);

    // 分别整合星空和地面部分的图片
    Mat_<Vec3b> resultImage = resSkyMat + resGroundMat;

    // 通过传地址在java中获得mat的方式
    resMatPtr->create(resultImage.rows, resultImage.cols, resultImage.type());
    memcpy(resMatPtr->data, resultImage.data, resMatPtr->step * resMatPtr->rows);

    // 将对齐结果写入文件中
    imwrite(generateImgAbsPath, resultImage);
    env->ReleaseStringUTFChars(generateImgAbsPath_, generateImgAbsPath);

    return 1; // 表示成功放回对齐之后的图像信息
}


extern "C"
JNIEXPORT void JNICALL
Java_com_photor_staralign_GrabCutActivity_initGrabCut(JNIEnv *env, jobject instance,
                                                      jlong oriImgMatAddr, jlong resImgMatAddr,
                                                      jlong maskMatAddr) {
    Mat *oriImgMat = (Mat*) oriImgMatAddr;
    Mat *resImgMat = (Mat*) resImgMatAddr;
    Mat *maskMat = (Mat*) maskMatAddr;
//    GCApplication *gcapp = new GCApplication();

    jclass jc = env->GetObjectClass(instance);
    jmethodID showId = env->GetMethodID(jc, "showImage", "()V");

    GCApplication::setImageAndShowId(oriImgMat, resImgMat, maskMat, showId);
    GCApplication::showImage(env, instance);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_photor_staralign_GrabCutActivity_moveGrabCut(JNIEnv *env, jobject instance,
                                                              jint event, jint x, jint y,
                                                              jint flags) {
    on_mouse(event, x, y, flags, env, instance);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_photor_staralign_GrabCutActivity_reset(JNIEnv *env, jobject instance) {
    GCApplication::reset();
    GCApplication::showImage(env,instance);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_photor_staralign_GrabCutActivity_grabCut(JNIEnv *env, jobject instance) {
    int iterCount = GCApplication::getIterCount();
    int newIterCount = GCApplication::nextIter();
    return (jboolean) (newIterCount > iterCount);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_photor_staralign_GrabCutActivity_grabCutOver(JNIEnv *env, jobject instance) {
    GCApplication::showImage(env,instance);
}


// StarAlignSplitActivity 分割星空前景背景的方法
extern "C"
JNIEXPORT void JNICALL
Java_com_photor_staralign_StarAlignSplitActivity_initGrabCut(JNIEnv *env, jobject instance,
                                                             jlong oriImgMatAddr,
                                                             jlong resImgMatAddr,
                                                             jlong maskMatAddr) {
    Mat *oriImgMat = (Mat*) oriImgMatAddr;
    Mat *resImgMat = (Mat*) resImgMatAddr;
    Mat *maskMat = (Mat*) maskMatAddr;

    jclass jc = env->GetObjectClass(instance);
    jmethodID showId = env->GetMethodID(jc, "showImage", "()V");

    StarGrabCut::init(oriImgMat, resImgMat, maskMat, showId);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_photor_staralign_StarAlignSplitActivity_moveGrabCut(JNIEnv *env, jobject instance,
                                                             jint event, jint x, jint y,
                                                             jint flags, jint lastX, jint lastY) {
    StarGrabCut::mouseClick(event, x, y, flags, lastX, lastY, env, instance);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_photor_staralign_StarAlignSplitActivity_grabCut(JNIEnv *env, jobject instance) {
    int iterCount = StarGrabCut::getIterCount();
    int newIterCount = StarGrabCut::nextIter();
    return (jboolean) (newIterCount > iterCount);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_photor_staralign_StarAlignSplitActivity_grabCutOver(JNIEnv *env, jobject instance) {
    StarGrabCut::showImage(env, instance);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_photor_staralign_StarAlignSplitActivity_reset(JNIEnv *env, jobject instance) {
    // 重新设置照片的前景以及背景
    StarGrabCut::reset();
}