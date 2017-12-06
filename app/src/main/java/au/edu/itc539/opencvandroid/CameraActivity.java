package au.edu.itc539.opencvandroid;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.android.JavaCameraView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


// OpenCV Classes

public class CameraActivity extends AppCompatActivity implements CvCameraViewListener2, SensorEventListener {

    private Scalar RED = new Scalar(255, 0, 0);

    private static final String TAG = "CameraActivity";

    public static final int NATIVE_DETECTOR = 1;

    public static final int JAVA_DETECTOR = 0;

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    private File mCascadeFile;

    private int mDetectorType = JAVA_DETECTOR;

    private String[] mDetectorName;

    // https://msdn.microsoft.com/en-us/library/azure/dn913079.aspx
    // https://github.com/opencv/opencv/tree/master/data/haarcascades
    private CascadeClassifier mJavaDetector;

    private Mat mRgba;

    private Mat mGray;

    private float mRelativeFruitSize = 0.2f;

    private int mAbsoluteFruitSize = 0;

    private TextView portrait_label, landscape_label;

    private CustTextView rev_landscape_label;


    private SensorManager mSensorManager;

    private Sensor mRotationSensor;



    private static final int SENSOR_DELAY = 500 * 1000; // 500ms

    private static final int FROM_RADS_TO_DEGS = -57;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {

                    Log.i(TAG, "OpenCV loaded successfully");

                    //  Intent iin= getIntent();

                    //  Bundle b = iin.getExtras();

                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);

                    try {

                        // if(b!=null) {

                        //   String j = (String) b.get("fruit");

                        //  String classifier = j;

                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(
                                getResources().getIdentifier("banana", "raw", getPackageName()));

                        portrait_label.setText("banana");

                        landscape_label.setText("banana");

                        rev_landscape_label.setText("banana");


                        mCascadeFile = new File(cascadeDir, "banana");

                        Log.i(TAG, "Classifier: " + "banana" + ".xml");

                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];

                        int bytesRead;

                        while ((bytesRead = is.read(buffer)) != -1) {

                            os.write(buffer, 0, bytesRead);

                        }

                        is.close();

                        os.close();

                        //}

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

                        if (mJavaDetector.empty()) {

                            Log.e(TAG, "Failed to load cascade classifier");

                            mJavaDetector = null;

                        } else

                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {

                        e.printStackTrace();

                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);

                    }

                    mOpenCvCameraView.setCameraIndex(0);

                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraActivity() {

        Log.i(TAG, "Instantiated new " + this.getClass());

        mDetectorName = new String[2];

        mDetectorName[JAVA_DETECTOR] = "Java";

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");

        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.show_camera);

        // First check android version
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            //Check if permission is already granted
            //thisActivity is your activity. (e.g.: MainActivity.this)
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Give first an explanation, if needed.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{ Manifest.permission.CAMERA},
                            1);

                }
            }
        }


        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);


        portrait_label = findViewById(R.id.fruit_target_portrait);

        landscape_label = findViewById(R.id.fruit_target_landscape);

        rev_landscape_label = findViewById(R.id.fruit_target_reverse_landscape);


/**
 // Get an instance of the SensorManager
 try {

 mSensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);

 mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

 mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY);

 } catch (Exception e) {

 Toast.makeText(this, "Hardware compatibility issue", Toast.LENGTH_LONG).show();

 }
 **/
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        mSensorManager.unregisterListener(this);
        super.onStop();

    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        if (mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
            Sensor s = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mRotationSensor) {
            if (event.values.length > 4) {
                float[] truncatedRotationVector = new float[4];
                System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                update(truncatedRotationVector);
            } else {
                update(event.values);
            }
        }

    }


    private void update(float[] vectors) {

        float[] rotationMatrix = new float[9];

        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);

        int worldAxisX = SensorManager.AXIS_X;

        int worldAxisZ = SensorManager.AXIS_Z;

        float[] adjustedRotationMatrix = new float[9];

        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);

        float[] orientation = new float[3];

        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        //    float pitch = orientation[1] * FROM_RADS_TO_DEGS;

        float roll = orientation[2] * FROM_RADS_TO_DEGS;    // relevant

        if ((roll >= 70 && roll <= 180)) {

            portrait_label.setVisibility(View.GONE);

            landscape_label.setVisibility(View.VISIBLE);

            rev_landscape_label.setVisibility(View.GONE);


        } else if (roll >= -180 && roll <= -70) {

            portrait_label.setVisibility(View.GONE);

            landscape_label.setVisibility(View.GONE);

            rev_landscape_label.setVisibility(View.VISIBLE);

        } else {

            portrait_label.setVisibility(View.VISIBLE);

            landscape_label.setVisibility(View.GONE);

            rev_landscape_label.setVisibility(View.GONE);

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

        mRgba = new Mat(height, width, CvType.CV_8UC4);

        mGray = new Mat();

    }

    public void onCameraViewStopped() {

        mRgba.release();

        mGray.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mGray = inputFrame.gray();

        //  Imgproc.rectangle(mRgba, new Point(10, 10), new Point(20, 20),new Scalar(0, 255, 0));
        // Imgproc.putText(mRgba, "====", new Point(10,10), 3, 1, new Scalar(255, 0, 0, 255), 2);

        if (mAbsoluteFruitSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFruitSize) > 0) {
                mAbsoluteFruitSize = Math.round(height * mRelativeFruitSize);
            }

        }

        MatOfRect fruit = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, fruit, 1.1, 2, 2,
                        new Size(mAbsoluteFruitSize, mAbsoluteFruitSize), new Size());
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] fruits = fruit.toArray();

        for (int i = 0; i < fruits.length; i++)
            Imgproc.rectangle(mRgba, fruits[i].tl(), fruits[i].br(), RED, 3);

        return inputFrame.rgba();


    }
}
