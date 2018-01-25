package org.droidplanner.android;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 * Created by zabuz on 9/4/2017.
 */

public class FacialRecog {

    String face_cascade_name = "/storage/emulated/0/data/haarcascades/haarcascade_frontalface_alt.xml";
    String eyes_cascade_name = "/storage/emulated/0/data/haarcascades/haarcascade_eye_tree_eyeglasses.xml";

    CascadeClassifier faceCascade;
    CascadeClassifier eyesCascade;


    public float detectAndDisplay(Mat frame)
    {

        Size s = frame.size();
        float heightF = (float)s.height/2;


        faceCascade = new CascadeClassifier();
        faceCascade.load(face_cascade_name);
        int absoluteFaceSize  = 0;


        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

//        if( !face_cascade.load( face_cascade_name ) ){ printf("--(!)Error loading\n");  };
//        if( !eyes_cascade.load( eyes_cascade_name ) ){ printf("--(!)Error loading\n");  };


        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (absoluteFaceSize == 0)
        {
            int height = grayFrame.rows();
            if (Math.round(height * 0.1f) > 0)
            {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }


        // detect faces
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        Point center;
        float displace = 0;
        for (int i = 0; i < facesArray.length; i++) {

//            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
            center = new Point( facesArray[i].x + facesArray[i].width*0.5, facesArray[i].y + facesArray[i].height*0.5 );
            Imgproc.ellipse( frame, center,new  Size( facesArray[i].width*0.5, facesArray[i].height*0.5), 0, 0, 360,new  Scalar( 255, 0, 255 ), 4, 8, 0 );
            displace = (float)center.y;

        }

        return heightF - displace;
    }

}
