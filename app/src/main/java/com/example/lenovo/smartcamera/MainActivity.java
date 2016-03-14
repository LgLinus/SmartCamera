package com.example.lenovo.smartcamera;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MainActivity
 */
public class MainActivity extends Cloud implements CameraBridgeViewBase.CvCameraViewListener2 {

    /* Camera variables */
    // Used to display footage
    CameraBridgeViewBase camera_view;

    // Current image
    Mat current_frame;
    /* End of camera variables */

    Button btnCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiateComponents();
        initiateListeners();

    }

    private void initiateListeners(){
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });
    }

    /**
     * Save the image currently being displayed
     */
    public void saveImage(){

        Mat matrix = current_frame;
        // Create bitmap from image
        Bitmap resultBitmap = Bitmap.createBitmap(matrix.cols(),matrix.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matrix, resultBitmap);

        Date date = new Date();

        SimpleDateFormat ft =
                new SimpleDateFormat ("yyyy/mm/dd_hh/mm/ss");

        String filename = "test";

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,filename+".png");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), resultBitmap, filename+".png", "xaxa");
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {

        }

        this.uploadFile(mypath,"filepath","testing","blabla");

    }

    private void initiateComponents(){

        btnCapture = (Button)findViewById(R.id.btnCapture);

        camera_view = (CameraBridgeViewBase)findViewById(R.id.camera_view);

        // Make the cameraview visible
        camera_view.setVisibility(SurfaceView.VISIBLE);

        camera_view.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        // Set the listener to the implemented CvCameraViewListener
        camera_view.setCvCameraViewListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Called when the frame for the camera changes, here appropiate transformations should occur
     * @param inputFrame
     * @return
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Mat res;
        res = inputFrame.gray();

        this.current_frame = res;
        return res;
    }


    public void onCameraViewStarted(int width, int height)
    {
    }

    public void onCameraViewStopped()
    {
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        initOpenCV();
    }

    /**
     * Init functions below
     */
    private void initOpenCV()
    {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch(status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.d("init", "OpenCV loaded successfully");
                    camera_view.enableView();
                }
            }
        }
    };

}
