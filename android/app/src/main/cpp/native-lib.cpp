#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/xfeatures2d.hpp>
#include <bits/stdc++.h>

using namespace std;
using namespace cv;
using namespace cv::xfeatures2d;


extern "C"
JNIEXPORT jstring JNICALL
Java_com_photor_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_photor_activity_test_OpencvTestActivity_stringFromJNI(JNIEnv *env, jobject instance) {

    // TODO
    std::string hello = "hello from opencv test!";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_photor_activity_test_OpencvTestActivity_nativeProcessFrame(JNIEnv *env, jobject instance,
                                                                    jlong addrGray,
                                                                    jlong addrRgba) {

    // TODO
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