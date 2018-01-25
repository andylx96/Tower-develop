package org.droidplanner.android;

        import android.content.pm.ActivityInfo;
        import android.graphics.Bitmap;
        import android.graphics.SurfaceTexture;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.SurfaceView;
        import android.view.TextureView;

        import org.droidplanner.android.fragments.widget.video.FullWidgetSoloLinkVideo;
        import org.opencv.android.BaseLoaderCallback;
        import org.opencv.android.CameraBridgeViewBase;
        import org.opencv.android.JavaCameraView;
        import org.opencv.android.LoaderCallbackInterface;
        import org.opencv.android.OpenCVLoader;
        import org.opencv.core.CvType;
        import org.opencv.core.Mat;

public class OpenCvDroneCam extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "MainActivity";
    JavaCameraView javaCameraView;
    Mat mRgba, imgGray, imgCanny;


    private TextureView soloLinkVideo;


    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
//                    javaCameraView.enableView();
                    break;

                }
                default: {

                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    // Used to load the 'native-lib' library on application startup.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// mod
//      setContentView(R.layout.activity_open_cv_drone_cam);
      setContentView(R.layout.fragment_widget_sololink_video);
//        FullWidgetSoloLinkVideo.

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());


        soloLinkVideo = (TextureView) findViewById(R.id.sololink_video_view);
        soloLinkVideo.setVisibility(SurfaceView.VISIBLE);

//        soloLinkVideo.try



//        soloLinkVideo.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
////                adjustAspectRatio(textureView as TextureView);
////                surfaceRef = Surface(surface)
////                tryStreamingVideo()
//
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                surfaceRef = null
//                tryStoppingVideoStream()
//
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//            }
//        });


//        javaCameraView = (JavaCameraView) findViewById(R.id.javaCamView);
//        javaCameraView.setVisibility(SurfaceView.VISIBLE);
//        javaCameraView.setCvCameraViewListener(this);
//        javaCameraView.setMaxFrameSize(480,320);
//            javaCameraView.set

//        ((TextView)findViewById(R.id.text_view)).setText(NativeClass.getMessageFromJNI());
//        Button cannyButton =(Button)findViewById(R.id.buttonCanny);
//        cannyButton.setOnClickListener(new cannyButtonListener());

//        bmp = soloLinkVideo.getBitmap();

//        droneMat = new Mat(bmp.getHeight(),bmp.getWidth() , CvType.CV_8UC1);

    }


    @Override
    protected void onPause() {
        super.onPause();
//        if (javaCameraView != null)
//            javaCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "Open CV Loaded");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        } else {
            Log.d(TAG, "Open CV Not Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallBack);
        }


    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
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
//        droneMat = inputFrame.rgba();

//        NativeClass.faceDetection(mRgba.getNativeObjAddr());
        return mRgba;
    }


    //import




}