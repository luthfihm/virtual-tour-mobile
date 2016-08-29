package com.luthfihm.virtualtour.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
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
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
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

    private Mat icon = null;

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

    private Button viewDetailsButton;
    private Button viewVideoButton;
    private String objectId = null;
    private String objectName;

    private VideoView videoView;

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

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.info);
                    Mat origIcon = new Mat();
                    icon = new Mat();
                    Utils.bitmapToMat(bitmap, origIcon);
                    Imgproc.resize(origIcon, icon, new Size(150, 150));

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
                handler.postDelayed(this, 1000);
                if (!isRecognizing) {
                    viewDetailsButton.setVisibility(View.VISIBLE);
                    viewVideoButton.setVisibility(View.VISIBLE);
                    Log.d("Haha", isRecognizing+"");
                } else {
                    viewDetailsButton.setVisibility(View.GONE);
                    viewVideoButton.setVisibility(View.GONE);
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

        viewDetailsButton = (Button) findViewById(R.id.viewDetailsButton);
        viewVideoButton = (Button) findViewById(R.id.viewVideoButton);
        videoView = (VideoView) findViewById(R.id.videoView);

        viewDetailsButton.setVisibility(View.VISIBLE);
        viewVideoButton.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);

        viewDetailsButton.setOnClickListener(new View.OnClickListener() {
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
                objectId = objectDetector.getTrainModels().get(matchIndex).getObjectId();
                objectName = sharedPreferences.getString(objectId,"object");
            }
        } else {
            vOrientation = orientation.getOrientation();

            double xAxis = Math.toDegrees(vOrientation[0]-vOrientationPrev[0]);
            double yAxis = Math.toDegrees(vOrientation[1]-vOrientationPrev[1]);
            double zAxis = Math.toDegrees(vOrientation[2]-vOrientationPrev[2]);

            double centerX = mRgba.cols()/2- xAxis*20;
            double centerY = mRgba.rows()/2- (zAxis)*20;

            if ((centerX > 75) && (centerY > 75) && (centerX < mRgba.cols()-75) && (centerY < mRgba.rows()-75)) {
//                Imgproc.rectangle(mRgba,new Point(centerX-50,centerY-50), new Point(centerX+50,centerY+50), new Scalar(0,0,255));
                Mat submat = mRgba.submat((int)centerY-75,(int)centerY+75, (int)centerX-75,(int)centerX+75);
                icon.copyTo(submat);
                Imgproc.putText(mRgba, objectName, new Point(centerX-75,centerY+130), 3, 1, new Scalar(0,0,255), 2);
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
