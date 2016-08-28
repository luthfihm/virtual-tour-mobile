package com.luthfihm.virtualtour.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.luthfihm.virtualtour.R;
import com.luthfihm.virtualtour.ar.GyroscopeOrientation;
import com.luthfihm.virtualtour.ar.ObjectDetector;
import com.luthfihm.virtualtour.ar.Orientation;
import com.luthfihm.virtualtour.ar.TrainModel;
import com.luthfihm.virtualtour.ar.TrainModelBuilder;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ARViewActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, Runnable {

    private Mat mRgba = null;
    private Mat mGray = null;
    private Mat prevGray = null;

    private boolean isRecognizing = true;

    private TrainModelBuilder trainModelBuilder;
    private ObjectDetector objectDetector;

    private CameraBridgeViewBase cameraView;

    private Mat firstFrame = null;
    private Mat secondFrame = null;

    private boolean isCalibrated = true;
    private boolean gyroscopeAvailable;
    private float[] vOrientationPrev = new float[3];
    private float[] vOrientation = new float[3];
    protected Handler handler;
    private Orientation orientation;
    protected Runnable runable;
    private boolean cameraReady = false;

    private MatOfPoint2f objectPoints = null;
    private MatOfPoint2f objectPointsPrev = null;

    private ImageView objectIcon;
    private TextView objectTitle;
    private String objectId = null;
    private String objectName;

    private SharedPreferences sharedPreferences;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    cameraView.enableView();
                    cameraView.setFocusable(true);

                    trainModelBuilder = new TrainModelBuilder(getExternalFilesDir(null));
                    objectDetector = new ObjectDetector(trainModelBuilder.getTrainModels());

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this,
                mLoaderCallback);

        orientation = new GyroscopeOrientation(this);
        handler = new Handler();

        runable = new Runnable()
        {
            @Override
            public void run()
            {
//                handler.postDelayed(this, 100);

//                System.arraycopy(vOrientation, 0, vOrientationPrev, 0, vOrientation.length);
//
//                vOrientation = orientation.getOrientation();
//
//                dataReady = true;

                if (cameraReady) {
//                    int matchIndex = objectDetector.recognize(mGray);
//                    Log.d("matchIndex", matchIndex+"");
//                    if (matchIndex != -1) {
//                        objectId = objectDetector.getTrainModels().get(matchIndex).getObjectId();
//                        objectName = sharedPreferences.getString(objectId,"object");
//                        objectTitle.setText(objectName);
//                        objectIcon.setVisibility(View.VISIBLE);
//                        objectTitle.setVisibility(View.VISIBLE);
//                    } else {
//                        objectIcon.setVisibility(View.INVISIBLE);
//                        objectTitle.setVisibility(View.INVISIBLE);
//                    }
                }
            }
        };

        orientation.onResume();

        handler.post(runable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        cameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        cameraView.setVisibility(View.VISIBLE);
        cameraView.setCvCameraViewListener(this);

        sharedPreferences = getSharedPreferences("com.luthfihm.virtualtour", Context.MODE_PRIVATE);

        gyroscopeAvailable = gyroscopeAvailable();

        objectIcon = (ImageView) findViewById(R.id.objectIcon);
        objectTitle = (TextView) findViewById(R.id.objectTitle);

        objectIcon.setVisibility(View.INVISIBLE);
        objectTitle.setVisibility(View.INVISIBLE);

        objectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (objectId != null) {
                    Intent intent = new Intent(ARViewActivity.this,ObjectDetailsActivity.class);
                    intent.putExtra("objectTitle", objectName);
                    intent.putExtra("objectId", objectId);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.disableView();
    }

    private boolean gyroscopeAvailable()
    {
        return getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_SENSOR_GYROSCOPE);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        cameraReady = true;

        if (isRecognizing) {
            int matchIndex = objectDetector.recognize(mGray);
            Log.d("matchIndex", matchIndex+"");
            if (matchIndex != -1) {
                vOrientationPrev[0] = orientation.getOrientation()[0];
                vOrientationPrev[1] = orientation.getOrientation()[1];
                vOrientationPrev[2] = orientation.getOrientation()[2];
                isRecognizing = false;
            }
        } else {
            vOrientation = orientation.getOrientation();

            double xAxis = Math.toDegrees(vOrientation[0]-vOrientationPrev[0]);
            double yAxis = Math.toDegrees(vOrientation[1]-vOrientationPrev[1]);
            double zAxis = Math.toDegrees(vOrientation[2]-vOrientationPrev[2]);

            double centerX = mRgba.cols()/2- xAxis*20;
            double centerY = mRgba.rows()/2- (zAxis)*20;

            if ((centerX > 0) && (centerY > 0)) {
                Imgproc.rectangle(mRgba,new Point(centerX-50,centerY-50), new Point(centerX+50,centerY+50), new Scalar(0,0,255));
            } else {
                isRecognizing = true;
            }
        }

        return mRgba;
    }

    @Override
    public void run() {
        Thread.currentThread().interrupt();
    }
}
