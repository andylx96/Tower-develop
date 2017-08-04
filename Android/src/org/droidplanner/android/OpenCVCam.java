package org.droidplanner.android;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class OpenCVCam extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG = "MainActivity";
    JavaCameraView javaCameraView;
    Mat mRgba, imgGray, imgCanny;



    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:{
                    javaCameraView.enableView();
                    break;

                }
                default:{

                    super.onManagerConnected(status);
                    break;
                }}}
    };

    // Used to load the 'native-lib' library on application startup.
    static {
//        System.loadLibrary("native-lib");
        System.loadLibrary("MyLibs");
//        System.loadLibrary("MyFaceLibs");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cvcam);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());

        javaCameraView = (JavaCameraView)findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
//        javaCameraView.setMaxFrameSize(480,320);
//            javaCameraView.set

//        ((TextView)findViewById(R.id.text_view)).setText(NativeClass.getMessageFromJNI());
//        Button cannyButton =(Button)findViewById(R.id.buttonCanny);
//        cannyButton.setOnClickListener(new cannyButtonListener());
    }


    @Override
    protected void onPause(){
        super.onPause();
        if (javaCameraView!=null)
            javaCameraView.disableView();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        javaCameraView.disableView();
    }

    @Override
    protected void onResume(){
        super.onResume();


        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "Open CV Loaded");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }
        else {Log.d(TAG,"Open CV Not Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,mLoaderCallBack);
        }


    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba =  new Mat(height, width, CvType.CV_8UC4);
//initizlize var
        imgGray = new Mat(height, width, CvType.CV_8UC1);
        imgCanny = new Mat(height, width, CvType.CV_8UC1);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

//set gray scale;
//        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);
// set canny
//        Imgproc.Canny(mRgba, imgCanny, 50, 150);
//caled face dection method
        NativeClass.faceDetection(mRgba.getNativeObjAddr());
        return mRgba;
//        returns canny
//    return imgCanny;
    }

//    public native String stringFromJNI();



//    private class cannyButtonListener implements View.OnClickListener {
//        @Override
//        public void onClick(View v) {
//
//        }
//    }
}











//    private static final String TAG = "MainActivity";
//    JavaCameraView javaCameraView;
//    Mat mRgba, imgGray, imgCanny;
//
//
//
//    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status){
//                case BaseLoaderCallback.SUCCESS:{
//                    javaCameraView.enableView();
//                    break;
//
//                }
//                default:{
//
//                    super.onManagerConnected(status);
//                    break;
//                }}}
//    };
//
//    // Used to load the 'native-lib' library on application startup.
//    static {
////        System.loadLibrary("native-lib");
////        System.loadLibrary("MyLibs");
////        System.loadLibrary("MyFaceLibs");
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_open_cvcam);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//
//        // Example of a call to a native method
////        TextView tv = (TextView) findViewById(R.id.sample_text);
////        tv.setText(stringFromJNI());
//
//        javaCameraView = (JavaCameraView)findViewById(R.id.java_camera_view);
//        javaCameraView.setVisibility(SurfaceView.VISIBLE);
//        javaCameraView.setCvCameraViewListener(this);
////        javaCameraView.setMaxFrameSize(480,320);
////            javaCameraView.set
//
////        ((TextView)findViewById(R.id.text_view)).setText(NativeClass.getMessageFromJNI());
////        Button cannyButton =(Button)findViewById(R.id.buttonCanny);
////        cannyButton.setOnClickListener(new cannyButtonListener());
//    }
//
//
//    @Override
//    protected void onPause(){
//        super.onPause();
//        if (javaCameraView!=null)
//            javaCameraView.disableView();
//    }
//
//    @Override
//    protected void onDestroy(){
//        super.onDestroy();
//        javaCameraView.disableView();
//    }
//
//    @Override
//    protected void onResume(){
//        super.onResume();
//
//
//        if(OpenCVLoader.initDebug()){
//            Log.d(TAG, "Open CV Loaded");
//            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//
//        }
//        else {Log.d(TAG,"Open CV Not Loaded");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,mLoaderCallBack);
//        }
//
//
//    }
//
//    @Override
//    public void onCameraViewStarted(int width, int height) {
//        mRgba =  new Mat(height, width, CvType.CV_8UC4);
////initizlize var
//        imgGray = new Mat(height, width, CvType.CV_8UC1);
//        imgCanny = new Mat(height, width, CvType.CV_8UC1);
//
//    }
//
//    @Override
//    public void onCameraViewStopped() {
//        mRgba.release();
//    }
//
//    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        mRgba = inputFrame.rgba();
//
////set gray scale;
////        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);
//// set canny
////        Imgproc.Canny(mRgba, imgCanny, 50, 150);
////caled face dection method
////        NativeClass.faceDetection(mRgba.getNativeObjAddr());
//        return mRgba;
////        returns canny
////    return imgCanny;
//    }
//
////    public native String stringFromJNI();
//
//
//
////    private class cannyButtonListener implements View.OnClickListener {
////        @Override
////        public void onClick(View v) {
////
////        }
////    }
//}
