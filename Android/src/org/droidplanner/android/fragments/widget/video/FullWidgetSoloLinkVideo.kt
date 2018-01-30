package org.droidplanner.android.fragments.widget.video

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.o3dr.android.client.apis.ControlApi
import com.o3dr.android.client.apis.GimbalApi
import com.o3dr.android.client.apis.solo.SoloCameraApi
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes
import com.o3dr.services.android.lib.drone.companion.solo.SoloEvents
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproConstants
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproState
import com.o3dr.services.android.lib.drone.property.Attitude
import com.o3dr.services.android.lib.model.AbstractCommandListener
import org.droidplanner.android.FacialRecog
import org.droidplanner.android.NativeClass
import org.droidplanner.android.OpenCVCam
import org.droidplanner.android.R
import org.droidplanner.android.dialogs.LoadingDialog
import org.opencv.android.JavaCameraView
import org.opencv.android.Utils
import org.opencv.utils.Converters
import timber.log.Timber
import java.util.*
import org.jetbrains.anko.toast
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.opencv.objdetect.HOGDescriptor
import org.opencv.objdetect.Objdetect

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class FullWidgetSoloLinkVideo : BaseVideoWidget(){
    private var ALTITUDE = 1.8
    private var Moving = false
    private val cmd_center = object : AbstractCommandListener() {
        override fun onSuccess() {
            //            Log.d(TAG, "Command received");
        }

        override fun onError(executionError: Int) {
            //            Log.d(TAG, "Command error");
        }

        override fun onTimeout() {
            //            Log.d(TAG, "Command not received, timeout.");
        }
    }
    // Move logic
    private fun stop() {
        Moving = false
        ControlApi.getApi(drone).manualControl(0f, 0.toFloat(), 0f, null)
    }

    private fun runLeft() {
        Moving = true
        ControlApi.getApi(drone).manualControl(0f, (-0.4).toFloat(), 0f, null)
    }

    private fun runRight() {
        Moving = true
        ControlApi.getApi(drone).manualControl(0f, 0.4.toFloat(), 0f, null)
    }

    private fun walkLeft() {
        Moving = true
        ControlApi.getApi(drone).manualControl(0f, (-0.2).toFloat(), 0f, null)
    }

    private fun walkRight() {
        Moving = true
        ControlApi.getApi(drone).manualControl(0f, 0.2.toFloat(), 0f, null)
    }

    private fun climbUp() {
        ALTITUDE += 0.2
        ControlApi.getApi(drone).climbTo(ALTITUDE)
    }

    private fun moveDown() {
        ALTITUDE -= 0.2
        ControlApi.getApi(drone).climbTo(ALTITUDE)
    }
    fun moveLogic(coordX: Int, coordY: Int, frameX: Int, frameY: Int) {
        val partition = (frameX / 9).toDouble()
        val stopRegion1 = 3 * partition
        val stopRegion2 = 6 * partition
        val runRegion1 = 2 * partition
        val runRegion2 = 7 * partition

        val partHeight = (frameY / 5).toDouble()
        val cameraTooLow = 4 * partHeight
        // horizontal movement
        if (coordX <= runRegion1) {

            runLeft()

        } else if (coordX >= runRegion2) {
            runRight()
        } else if (coordX <= stopRegion2 && coordX >= stopRegion1) {
            if (!Moving) {
                stop()
            }
        } else if (coordX > runRegion1) {
            walkLeft()
        } else if (coordX < runRegion2) {
            walkRight()
        }

        //vertical movements, remove if drone behaves abnormally
        if (coordY < partHeight) {
            moveDown()
        } else if (coordY < cameraTooLow) {
            climbUp()
        }
    }

        companion object {
        private val filter = initFilter()

        @JvmStatic protected val TAG = FullWidgetSoloLinkVideo::class.java.simpleName

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.STATE_CONNECTED)
            temp.addAction(SoloEvents.SOLO_GOPRO_STATE_UPDATED)
            return temp
        }
    }

    private var toggle: Boolean = false;

    private val handler = Handler()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AttributeEvent.STATE_CONNECTED -> {
                    tryStreamingVideo()
                    onGoproStateUpdate()
                }

                SoloEvents.SOLO_GOPRO_STATE_UPDATED -> {
                    onGoproStateUpdate()
                }
            }
        }

    }
    public fun get_person(img: Mat): IntArray
    {
        val hog =  HOGDescriptor()

        val img1ch = Mat(img.height(),img.width(),CvType.CV_8U)
        img.convertTo(img1ch, CvType.CV_8U)

//        val ho
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector())
        //                CVzie
        //                CvR
        val found = MatOfRect()
        val matOfDouble = MatOfDouble()
        val found_filtered: MatOfRect
        Imgproc.cvtColor(img1ch, img1ch, Imgproc.COLOR_BGR2GRAY)
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(img1ch, img1ch)
        Timber.d("Image channel is " + img1ch.channels())
        Timber.d("Image type is " + img1ch.type())

        hog.detectMultiScale(img1ch, found, matOfDouble, 0.0,
                Size(8.0, 8.0), Size(32.0, 32.0), 1.05, 2.0, false)
        val arr = found.toArray()

        if(arr.size > 0) {
        //        Timber.d("Arr is "+ arr.toString())

            val coord_x = arr[0].x
            val coord_y = arr[0].y
            val center_x = (arr[0].width + coord_x) / 2
            val center_y = (arr[0].height + coord_y) / 2
            Timber.d("xxxxcenter x is " + center_x)
            Timber.d("yyyycenter y is " + center_y)
            val x_y = intArrayOf(center_x, center_y)
            var center: Point


            center = Point(arr[0].x + arr[0].width * 0.5, arr[0].y + arr[0].height * 0.5)
            Imgproc.ellipse(img1ch, center, Size(arr[0].width * 0.5, arr[0].height * 0.5), 0.0, 0.0, 360.0, Scalar(255.0, 0.0, 255.0), 4, 8, 0)

            moveLogic(arr[0].x, arr[0].y, img.width(), img.height())




//            val bmp=  Bitmap();
//            Utils.matToBitmap(img1ch, bmp)
//
//            imageView2?.setImageBitmap()

            //            int higharea = -1;
            //            boolean flag = false;
            //            int highareaindex = -1;
            //            int i, j;
            //
            //            Rect[] arr = found.toArray();
            ////            int k =  found.size();
            //            for (i=0; i< found.toArray().length ; i++)
            //            {
            //                Rect r = arr[i];
            ////                for (j=0; j<arr.length; j++)
            ////                    if (j!=i && (r & arr[j])==r)
            ////                        break;
            //                if (j==arr.length) {
            //                    found_filtered.push_back(r);
            //                    Rect[] arr2 = found_filtered.toArray();
            //                    arr2.
            //                }
            //            }
            //            for (i=0; i<found_filtered.size(); i++)
            //            {
            //                Rect r = found_filtered[i];
            //                if (r.area() >= higharea){
            //                    higharea = r.area();
            //                    highareaindex = i;
            //                }
            //                flag = true;
            //
            //            }
            //
            //            if(flag == true){
            //                Rect r = found_filtered[highareaindex];
            //                cout << r.area() << endl;
            //                r.x += cvRound(r.width*0.1);
            //                r.width = cvRound(r.width*0.8);
            //                r.y += cvRound(r.height*0.06);
            //                r.height = cvRound(r.height*0.9);
            //                rectangle(img, r.tl(), r.br(), cv::Scalar(0,255,0), 2);
            //
            //            }
            //        }}
            //        x_y = [r.width, r.height]
            //        returnPoint = x_y;
            //
            //        // cleanup
            //        mxDestroyArray(arr);
            //        matClose(pmat);

            return x_y // return a pointer to r.x and r.y
        }
        val nothing = intArrayOf(img.height() / 2, img.width() / 2)
        return nothing
    }
    private val resetGimbalControl = object: Runnable {

        override fun run() {
            if (drone != null) {
                GimbalApi.getApi(drone).stopGimbalControl(orientationListener)
            }
            handler.removeCallbacks(this)
        }
    }

    private var fpvLoader: LoadingDialog? = null

    private var surfaceRef: Surface? = null


    private val textureView by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_video_view) as TextureView?
    }

    private val imageView2 by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.imageView3) as ImageView?
    }

    private val button by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.openCV) as Button?
    }


    private val videoStatus by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_video_status) as TextView?
    }

    private val widgetButtonBar by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.widget_button_bar)
    }



    private val openCVButton by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.openCV)

    }





    private val takePhotoButton by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_take_picture_button)
    }

    private val recordVideo by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_record_video_button)
    }

    private val fpvVideo by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_vr_video_button)
    }

    private val touchCircleImage by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_gimbal_joystick)
    }

    private val orientationListener = object : GimbalApi.GimbalOrientationListener {
        override fun onGimbalOrientationUpdate(orientation: GimbalApi.GimbalOrientation) {
        }

        override fun onGimbalOrientationCommandError(code: Int) {
            Timber.e("command failed with error code: %d", code)
        }
    }




    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater?.inflate(R.layout.fragment_widget_sololink_video, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                adjustAspectRatio(textureView as TextureView);
                surfaceRef = Surface(surface)
                tryStreamingVideo()
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                surfaceRef = null
                tryStoppingVideoStream()
                return true
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

                if (toggle == true) {
                    val bmp: Bitmap? = textureView?.getBitmap()

                //    bmp?.height = 240
                //    bmp?.width = 320
                    Timber.d("Height is :" + bmp?.height.toString())
                    Timber.d("Width is :" + bmp?.width.toString())

                    val cvimg = Mat()
                //                        val cvimg:Mat? = Mat
                    Utils.bitmapToMat(bmp, cvimg)
                //    cvimg.set
//                    val coords = get_person(cvimg)

                //                val displacement:Float? = NativeClass.faceDetection(cvimg.nativeObj);
                //jave to build in java code
                    val displacementArray: FloatArray? = detectAndDisplay(cvimg);
                    val displacementX: Float? = displacementArray?.get(0)
                    val displacementY: Float? = displacementArray?.get(1)





                    Timber.d("Frame Width Displace Of Cam" + displacementX.toString())
                    Timber.d("Frame Height Displace Of Cam" + displacementY.toString())

                    Utils.matToBitmap(cvimg, bmp)

                    imageView2?.setImageBitmap(bmp)




                    GimbalApi.getApi(drone).startGimbalControl(orientationListener)
                    val orientation2 = GimbalApi.getApi(drone).getGimbalOrientation()
//            var rotateYaw:Float? = orientation2.getYaw()+ -100.00.toFloat()

    //yaw is y axis spin
    //pitch is x axis spin
    //roll is camera spin
//    if (displacementY!! == 312.0f) {
//        Timber.d("No Face")
//    } else if (displacementY!! > 150f) {
//        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() + 10, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//    } else if (displacementY!! > 120f) {
//        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() + 6, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//    } else if (displacementY!! > 70f) {
//        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() + 3, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//    } else if (displacementY!! > 50f) {
//        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() + 1, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//    } else if (displacementY!! == 0f) {
//        Timber.d("Displacement 0 Centered")
//    } else if (displacementY!! > -50f) {
////                    GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() - 1, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//    } else if (displacementY!! > -70f) {
//        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() - 1, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//    } else if (displacementY!! > -120f) {
//        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() - 3, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//    } else if (displacementY!! > -150f) {
//        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() - 6, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//    } else {
//        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() - 10, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//    }
//
//
//    if (displacementX!! == 540.0f) {
//        Timber.d("No Face")
//    } else if (displacementX!! > 0.0f) {
////Left Is Positive
////        turnLeft()
//        Timber.d("Turn Left")
//    } else if (displacementX!! == 0.0f) {
//
//
//        Timber.d("Centered")
//    } else if (displacementX!! < 0.0f) {
//
////                    Negitive Is Right
////turnRight()
//        Timber.d("Turn Right")
//    } else {
//        Timber.d("None Width")
//    }


//            working buttons
//                if (displacement!! == 0f){
//                    Timber.d("Displacement 0 Centered")
//                }
//                if (displacement!! <100) {
//                    if (displacement!! <33){
//                        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() - 1, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//                    }
//                    else if (displacement!! <67) {
//                        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() - 3, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//                    }
//                    else    GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() - 5, orientation2.getRoll(), 0.toFloat(), orientationListener)
//                    }
//                else if (displacement == 312.0f){
//                    Timber.d("312 No faces");
//                }
//                else if(displacement!! > -100) {
//                    if (displacement!! > -67) {
//                        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() + 3, orientation2.getRoll(), 0.toFloat(), orientationListener)
//                    }
//                    else if (displacement!! > -33){
//                        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() + 1, orientation2.getRoll(), 0.toFloat(), orientationListener)
//                    }
//                    else
//                        GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() + 5, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
////                    GimbalApi.getApi(drone).stopGimbalControl(orientationListener)
//                }
//
//
                                }
                else if (toggle == false){

                    imageView2?.setImageBitmap(textureView?.getBitmap())


                }

            }

        }

        openCVButton?.setOnClickListener {


            if (toggle == false){
                toggle = true

                Timber.d("Toggle is Now True")

            }
            else if (toggle == true){
                toggle = false

                Timber.d("Toggle is Now False")
            }
//            turnRight()
//            Timber.d("TUrn Right Pressed")

//            val bmp:Bitmap? = textureView?.getBitmap()
//
//            val cvimg = Mat()
////                        val cvimg:Mat? = Mat
//            Utils.bitmapToMat(bmp,cvimg)
//
////            val displacement:Double? = NativeClass.faceDetection(cvimg.nativeObj);
////            Timber.d( displacement.toString())
//            Utils.matToBitmap(cvimg, bmp)
//
//            imageView2?.setImageBitmap(bmp)
//
//
//                val bmp:Bitmap? = textureView?.getBitmap()
//
//                val cvimg = Mat()
////                        val cvimg:Mat? = Mat
//                Utils.bitmapToMat(bmp,cvimg)
//
//            val displacement:Float? = NativeClass.faceDetection(cvimg.nativeObj);
//            Timber.d( displacement.toString())
//                Utils.matToBitmap(cvimg, bmp)
//
//                imageView2?.setImageBitmap(bmp)
//
//
//
//
//            GimbalApi.getApi(drone).startGimbalControl(orientationListener)
//            val orientation2 = GimbalApi.getApi(drone).getGimbalOrientation()
////            var rotateYaw:Float? = orientation2.getYaw()+ -100.00.toFloat()
//
//            //yaw is y axis spin
//            //pitch is x axis spin
//            //roll is camera spin
//
////            working buttons
//            if (displacement!! < 10) {
//
//                GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() - 10, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//            }
//            else if (displacement == 312.0f){
//                    Timber.d("312 No faces");
//
//                }
//
//
//            else if(displacement!! > -10) {
//
//                GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() + 10, orientation2.getRoll(), 0.toFloat(), orientationListener)
//
//            }
//
//
//
//
////            GimbalApi.getApi(drone).updateGimbalOrientation(orientation2.getPitch() - 20, orientation2.getRoll(), 20.toFloat(), orientationListener)
//
//

        }

        takePhotoButton?.setOnClickListener {
            Timber.d("Taking photo.. cheeze!")
            val drone = drone
            if (drone != null) {
                //TODO: fix when camera control support is stable on sololink
                SoloCameraApi.getApi(drone).takePhoto(null)
            }
        }

        recordVideo?.setOnClickListener {
            Timber.d("Recording video!")
            val drone = drone
            if (drone != null) {
                //TODO: fix when camera control support is stable on sololink
                SoloCameraApi.getApi(drone).toggleVideoRecording(null)
            }
        }

//
//        val onClickListener = openCVButton?.setOnClickListener {
//            Timber.d("OpenCV")
//
//            var launchIntent: Intent?
//            launchIntent = Intent(this@FullWidgetSoloLinkVideo)
//
//
//                val intent: Intent? = Intent(this@FullWidgetSoloLinkVideo,OpenCVCam::class.java)
//        }

        fpvVideo?.setOnClickListener {
            launchFpvApp()
        }
    }


    private fun launchFpvApp() {
        val appId = "meavydev.DronePro"

        //Check if the dronepro app is installed.
        val activity = activity ?: return
        val pm = activity.getPackageManager()
        var launchIntent: Intent? = pm.getLaunchIntentForPackage(appId)
        if (launchIntent == null) {

            //Search for the dronepro app in the play store
            launchIntent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setData(Uri.parse("market://details?id=" + appId))

            if (pm.resolveActivity(launchIntent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
                launchIntent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setData(Uri.parse("https://play.google.com/store/apps/details?id=" + appId))
            }

            startActivity(launchIntent)

        } else {
            if(fpvLoader == null) {
                launchIntent.putExtra("meavydev.DronePro.launchFPV", "Tower")

                fpvLoader = LoadingDialog.newInstance("Starting FPV...", object : LoadingDialog.Listener {
                    override fun onStarted() {
                        handler.postDelayed( {startActivity(launchIntent) }, 500L)
                    }

                    //                val bmp:Bitmap? = textureView?.getBitmap()
//
//                val cvimg = Mat()
////                        val cvimg:Mat? = Mat
//                Utils.bitmapToMat(bmp,cvimg)
//
//            val displacement:Float? = NativeClass.faceDetection(cvimg.nativeObj);
//            Timber.d( displacement.toString())
//                Utils.matToBitmap(cvimg, bmp)
//
//                imageView2?.setImageBitmap(bmp)
//
                    override fun onCancel() {
                        fpvLoader = null
                    }

                    override fun onDismiss() {
                        fpvLoader = null
                    }

                });
                fpvLoader?.show(childFragmentManager, "FPV launch dialog")
            }
        }
    }

    override fun onApiConnected() {
        tryStreamingVideo()
        onGoproStateUpdate()
        broadcastManager.registerReceiver(receiver, filter)
    }

    override fun onResume() {
        super.onResume()
        tryStreamingVideo()
    }

    override fun onPause() {
        super.onPause()
        tryStoppingVideoStream()
    }

    override fun onStop(){
        super.onStop()
        fpvLoader?.dismiss()
        fpvLoader = null
    }

    override fun onApiDisconnected() {
        tryStoppingVideoStream()
        onGoproStateUpdate()
        broadcastManager.unregisterReceiver(receiver)
    }

    fun tryStreamingVideo() {


        if (surfaceRef == null)
            return

        val drone = drone
        videoStatus?.visibility = View.GONE

        startVideoStream(surfaceRef!!, TAG, object : AbstractCommandListener() {
            override fun onError(error: Int) {
                Timber.d("Unable to start video stream: %d", error)
                GimbalApi.getApi(drone).stopGimbalControl(orientationListener)
                textureView?.setOnTouchListener(null)
                videoStatus?.visibility = View.VISIBLE
            }

            override fun onSuccess() {
                videoStatus?.visibility = View.GONE
                Timber.d("Video stream started successfully")

                val gimbalTracker = object : View.OnTouchListener {
                    var startX: Float = 0f
                    var startY: Float = 0f

                    override fun onTouch(view: View, event: MotionEvent): Boolean {



//                        textureView = cvimg
////                        cvimg.




                        return moveCopter(view, event)
                    }

                    private fun yawRotateTo(view: View, event: MotionEvent): Double {
                        val drone = drone ?: return -1.0

                        val attitude = drone.getAttribute<Attitude>(AttributeType.ATTITUDE)
                        var currYaw = attitude.getYaw()

                        //yaw value is between -180 and 180. Convert so the value is between 0 to 360
                        if (currYaw < 0) {
                            currYaw += 360.0
                        }

                        val degreeIntervals = (360f / view.width).toDouble()
                        val rotateDeg = (degreeIntervals * (event.x - startX)).toFloat()
                        var rotateTo = currYaw.toFloat() + rotateDeg

                        //Ensure value stays in range between 0 and 360
                        rotateTo = (rotateTo + 360) % 360
                        return rotateTo.toDouble()
                    }

                    private fun moveCopter(view: View, event: MotionEvent): Boolean {
                        val xTouch = event.x
                        val yTouch = event.y

                        val touchWidth = touchCircleImage?.width ?: 0
                        val touchHeight = touchCircleImage?.height ?: 0
                        val centerTouchX = (touchWidth / 2f).toFloat()
                        val centerTouchY = (touchHeight / 2f).toFloat()

                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                handler.removeCallbacks(resetGimbalControl)
                                GimbalApi.getApi(drone).startGimbalControl(orientationListener)

                                touchCircleImage?.setVisibility(View.VISIBLE)
                                touchCircleImage?.setX(xTouch - centerTouchX)
                                touchCircleImage?.setY(yTouch - centerTouchY)
                                startX = event.x
                                startY = event.y
                                return true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val yawRotateTo = yawRotateTo(view, event).toFloat()
                                sendYawAndPitch(view, event, yawRotateTo)
                                touchCircleImage?.setVisibility(View.VISIBLE)
                                touchCircleImage?.setX(xTouch - centerTouchX)
                                touchCircleImage?.setY(yTouch - centerTouchY)
                                return true
                            }
                            MotionEvent.ACTION_UP -> {
                                touchCircleImage?.setVisibility(View.GONE)
                                handler.postDelayed(resetGimbalControl, 3500L)
                            }
                        }
                        return false
                    }

                    private fun sendYawAndPitch(view: View, event: MotionEvent, rotateTo: Float) {
                        val orientation = GimbalApi.getApi(drone).getGimbalOrientation()

                        val degreeIntervals = 90f / view.height
                        val pitchDegree = (degreeIntervals * (startY - event.y)).toFloat()
                        val pitchTo = orientation.getPitch() + pitchDegree

                        Timber.d("Pitch %f roll %f yaw %f", orientation.getPitch(), orientation.getRoll(), rotateTo)
                        Timber.d("degreeIntervals: %f pitchDegree: %f, pitchTo: %f", degreeIntervals, pitchDegree, pitchTo)

                        GimbalApi.getApi(drone).updateGimbalOrientation(pitchTo, orientation.getRoll(), rotateTo, orientationListener)
                    }
                }

                textureView?.setOnTouchListener(gimbalTracker)
            }

            override fun onTimeout() {
                Timber.d("Timed out while trying to start the video stream")
                GimbalApi.getApi(drone).stopGimbalControl(orientationListener)
                textureView?.setOnTouchListener(null)
                videoStatus?.visibility = View.VISIBLE
            }

        })
    }

    private fun tryStoppingVideoStream() {
        val drone = drone

        stopVideoStream(TAG, object : AbstractCommandListener() {
            override fun onError(error: Int) {
                Timber.d("Unable to stop video stream: %d", error)
            }

            override fun onSuccess() {
                Timber.d("Video streaming stopped successfully.")
                GimbalApi.getApi(drone).stopGimbalControl(orientationListener)
            }

            override fun onTimeout() {
                Timber.d("Timed out while stopping video stream.")
            }

        })
    }

    private fun onGoproStateUpdate() {
        val goproState: SoloGoproState? = drone?.getAttribute(SoloAttributes.SOLO_GOPRO_STATE)
        if (goproState == null) {
            widgetButtonBar?.visibility = View.GONE
        } else {
            widgetButtonBar?.visibility = View.VISIBLE

            //Update the video recording button
            recordVideo?.isActivated = goproState.captureMode == SoloGoproConstants.CAPTURE_MODE_VIDEO
                    && goproState.recording == SoloGoproConstants.RECORDING_ON
        }
    }

    private fun adjustAspectRatio(textureView: TextureView) {
        val viewWidth = textureView.width
        val viewHeight = textureView.height
        val aspectRatio: Float = 9f / 16f

        val newWidth: Int
        val newHeight: Int
        if (viewHeight > (viewWidth * aspectRatio)) {
            //limited by narrow width; restrict height
            newWidth = viewWidth
            newHeight = (viewWidth * aspectRatio).toInt()
        } else {
            //limited by short height; restrict width
            newWidth = (viewHeight / aspectRatio).toInt();
            newHeight = viewHeight
        }

        val xoff = (viewWidth - newWidth) / 2f
        val yoff = (viewHeight - newHeight) / 2f

        val txform = Matrix();
        textureView.getTransform(txform);
        txform.setScale((newWidth.toFloat() / viewWidth), newHeight.toFloat() / viewHeight);

        txform.postTranslate(xoff, yoff);
        textureView.setTransform(txform);
    }

    private var face_cascade_name = "/storage/emulated/0/data/haarcascades/haarcascade_frontalface_alt.xml"
    private var eyes_cascade_name = "/storage/emulated/0/data/haarcascades/haarcascade_eye_tree_eyeglasses.xml"
    private var full_cascade_xml = "/storage/emulated/0/data/haarcascades/haarcascade_fullbody.xml"


    fun returnFrameWidth(frame: Mat): Float{

        val w = frame.size()
        val widthF = w.width.toFloat() /2;

        return widthF
    }


    fun returnFrameHeight(frame: Mat): Float{

        val h = frame.size()
        val heightF = h.height.toFloat() /2;

        return heightF
    }

    fun detectAndDisplay(frame: Mat): FloatArray {

        var faceCascade: CascadeClassifier
        var bodyCascade: CascadeClassifier


        val s = frame.size()
        val heightF = s.height.toFloat() / 2
        val widthF = s.width.toFloat() / 2
//        val widthF = s.width

//        HOGDescriptor hog
        faceCascade = CascadeClassifier()
//        bodyCascade = CascadeClassifier()
//        faceCascade.load(face_cascade_name)
        Timber.d("The loader is working++++++++++++++++\n" + faceCascade.load(full_cascade_xml))
        var absoluteFaceSize = 0


        val faces = MatOfRect()
        val grayFrame = Mat()

        //        if( !face_cascade.load( face_cascade_name ) ){ printf("--(!)Error loading\n");  };
        //        if( !eyes_cascade.load( eyes_cascade_name ) ){ printf("--(!)Error loading\n");  };


        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY)
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame)

        // compute minimum face size (20% of the frame height, in our case)
        if (absoluteFaceSize == 0) {
            val height = grayFrame.rows()
            if (Math.round(height * 1.00f) > 0) {
                absoluteFaceSize = Math.round(height * 1.00f)
            }
        }


        // detect faces
//        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 or Objdetect.CASCADE_SCALE_IMAGE,
//                Size(absoluteFaceSize.toDouble(), absoluteFaceSize.toDouble()), Size())

//detect body
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 or Objdetect.CASCADE_SCALE_IMAGE,
                Size(absoluteFaceSize.toDouble(), absoluteFaceSize.toDouble()), Size())
//faceCascade.d
        // each rectangle in faces is a face: draw them!
        val facesArray = faces.toArray()
        var center: Point
        var displaceY = 0f
        var displaceX = 0f
        for (i in facesArray.indices) {

            //            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
            center = Point(facesArray[i].x + facesArray[i].width * 0.5, facesArray[i].y + facesArray[i].height * 0.5)
            Imgproc.ellipse(frame, center, Size(facesArray[i].width * 0.5, facesArray[i].height * 0.5), 0.0, 0.0, 360.0, Scalar(255.0, 0.0, 255.0), 4, 8, 0)
            displaceY = center.y.toFloat()
            displaceX = center.x.toFloat()
        }

        val displaceArray = FloatArray(2)
        displaceArray[0] = widthF - displaceX
        displaceArray[1] = heightF- displaceY

        return displaceArray

//        return heightF - displace
    }

//    fun detectBody(frame: Mat): FloatArray{
//

//        val hog = HOGDescriptor();
//        hog.setSVMDetector(HOGDescriptor::getDefaultPeopleDetector());
//
//        while (true){
//            val vector<Rect> = Vector<Rect>;
//
//        }
//
//        return null;
//    }

//    Edits

//    private var ALTITUDE = 1.5
//    private val cmd_center = object : AbstractCommandListener() {
//        override fun onSuccess() {
//            //            Log.d(TAG, "Command received");
//        }
//
//        override fun onError(executionError: Int) {
//            //            Log.d(TAG, "Command error");
//        }
//
//        override fun onTimeout() {
//            //            Log.d(TAG, "Command not received, timeout.");
//        }
//    }

//    private fun ascend() {
//        //        final SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("climb up 1 meter", new Runnable() {
//        //            @Override
//        //            public void run() {
//        ALTITUDE += 0.1
//        ControlApi.getApi(drone).climbTo(ALTITUDE)
//        ////            }
//        //        });
//        //        unlockDialog.show(getChildFragmentManager(), "Slide to raise 1.0 m");
//    }
//
//    private fun descend() {
//        //        final SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("climb up 1 meter", new Runnable() {
//        //            @Override
//        //            public void run() {
//        ALTITUDE -= 0.1
//        ControlApi.getApi(drone).climbTo(ALTITUDE)
//        //            }
//        //        });
//        //        unlockDialog.show(getChildFragmentManager(), "Slide to raise 1.0 m");
//    }
//
//    private fun turnRight() {
//        //        final SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("turn left 90 degrees", new Runnable() {
//        //            @Override
//        //            public void run() {
//        //                Log.d(TAG, "Running Turn");
//        ControlApi.getApi(drone).turnTo(10f, 0.1f, true, cmd_center)
//        //                Log.d(TAG, "Running Turn finished");
//        //                ControlApi.getApi(getDrone()).goTo();
//        //            }
//        //        });
//        //        unlockDialog.show(getChildFragmentManager(), "Slide to turn left 90 degrees");
//    }
//
//    private fun turnLeft() {
//        //        final SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("turn left 90 degrees", new Runnable() {
//        //            @Override
//        //            public void run() {
//        //                Log.d(TAG, "Running Turn");
//        ControlApi.getApi(drone).turnTo(10f, -0.1f, true, cmd_center)
//        //                Log.d(TAG, "Running Turn finished");
//        //                ControlApi.getApi(getDrone()).goTo();
//        //            }
//        //        });
//        //        unlockDialog.show(getChildFragmentManager(), "Slide to turn left 90 degrees");
//    }

}