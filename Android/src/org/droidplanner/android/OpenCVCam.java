package org.droidplanner.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

public class OpenCVCam extends AppCompatActivity {

    private static String TAG = "OpenCV";

    static {
        if(OpenCVLoader.initDebug()){
            Log.i(TAG,"OpenCVLoaded");
        }
        else {
            Log.i(TAG,"OpenCv Not Loaded");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cvcam);
    }
}
