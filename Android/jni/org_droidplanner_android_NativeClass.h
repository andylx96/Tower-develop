/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <opencv2/opencv.hpp>
/* Header for class io_github_andylx96_myapplication_NativeClass */

using namespace cv;
#ifndef _Included_org_droidplanner_android_NativeClass
#define _Included_org_droidplanner_android_NativeClass
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     io_github_andylx96_myapplication_NativeClass
 * Method:    faceDetection
 * Signature: (J)V
 */

float detect (Mat& frame);


JNIEXPORT void JNICALL Java_org_droidplanner_android_NativeClass_faceDetection
(JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
