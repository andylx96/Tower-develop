#include "org_droidplanner_android_NativeClass.h"
String face_cascade_name = "/storage/emulated/0/data/haarcascades/haarcascade_frontalface_alt.xml";
String eyes_cascade_name = "/storage/emulated/0/data/haarcascades/haarcascade_eye_tree_eyeglasses.xml";

int j=2;

JNIEXPORT void JNICALL Java_org_droidplanner_android_NativeClass_faceDetection
(JNIEnv *, jclass, jlong addRgba){
Mat& frame = *(Mat*) addRgba;
//
//while(j == 1){

detect(frame);

//j = j+1;
//printf("--(!)Error loading\n");
//}
//j = 1;

}


float detect(Mat& frame) {
    CascadeClassifier face_cascade;
    CascadeClassifier eyes_cascade;

    //-- 1. Load the cascades
    if( !face_cascade.load( face_cascade_name ) ){ printf("--(!)Error loading\n");  };
    if( !eyes_cascade.load( eyes_cascade_name ) ){ printf("--(!)Error loading\n");  };

    std::vector<Rect> faces;
    Mat frame_gray;
    Size s = frame.size();
    float heightF = s.height/2;

    cvtColor( frame, frame_gray, CV_BGR2GRAY );
    equalizeHist( frame_gray, frame_gray );

    face_cascade.detectMultiScale(frame_gray, faces, 1.1, 2, 0 | CV_HAAR_SCALE_IMAGE, Size(30, 30));
    Point centerf;
    for( size_t i = 0; i < faces.size(); i++ )
    {
        Point center( faces[i].x + faces[i].width*0.5, faces[i].y + faces[i].height*0.5 );
        centerf = center;
        ellipse( frame, center, Size( faces[i].width*0.5, faces[i].height*0.5), 0, 0, 360, Scalar( 255, 0, 255 ), 4, 8, 0 );

        Mat faceROI = frame_gray( faces[i] );


    }
    return heightF - centerf.y;
//    return 10.0;
}

