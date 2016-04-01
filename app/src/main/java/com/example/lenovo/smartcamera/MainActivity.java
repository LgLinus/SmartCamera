package com.example.lenovo.smartcamera;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

//import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * MainActivity
 */
public class MainActivity extends Cloud implements CameraBridgeViewBase.CvCameraViewListener2 {

    private Toolbar toolbar;
    /* Camera variables */
    // Used to display footage
    CameraBridgeViewBase camera_view;

    // Current image
    Mat current_frame = null;
    Mat old_frame = null;
    boolean first = true;
    Mat test;
    int current=0;
    boolean edgeDetection = false;
    int kernel_size = 4;
    /* End of camera variables */

    Button btn_edge_detection, btn_plus_gauss, btn_neg_gauss;
    Button btnCapture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       toolbar = (Toolbar) findViewById(R.id.toolbar);
       setSupportActionBar(toolbar);

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
        btn_edge_detection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edgeDetection = !edgeDetection;
            }
        });
        btn_plus_gauss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kernel_size*=2;
            }
        });

        btn_neg_gauss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kernel_size/=2;
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
        File file=new File(directory,filename+".png");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
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

        this.uploadFile(file, "filepath", "testing", "blabla");

    }

    private void initiateComponents(){

        btnCapture = (Button)findViewById(R.id.btnCapture);
        btn_edge_detection = (Button)findViewById(R.id.btnEdges);
        btn_plus_gauss = (Button)findViewById(R.id.btnPlusGauss);
        btn_neg_gauss = (Button)findViewById(R.id.btnNegGauss);

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
        if(first){
            first = false;
            old_frame = inputFrame.rgba();
            test = old_frame;
        }
        else
            current_frame = inputFrame.rgba();
        if(old_frame!=null && current_frame!=null)
        {
            Log.d("crash", "pre crash 2");
            Mat bg = new Mat();
            Mat cg= new Mat();
            test = new Mat();

            // cvT Color works
            Imgproc.cvtColor(old_frame, bg, Imgproc.COLOR_BGR2GRAY);
            Imgproc.cvtColor(current_frame, cg, Imgproc.COLOR_BGR2GRAY);
            Imgproc.cvtColor(current_frame, test, Imgproc.COLOR_BGR2GRAY);

            Log.d("crash", "pre crash 3");
            // TODO
            Mat test2 = new Mat();

            Imgproc.GaussianBlur(current_frame,test,new Size(kernel_size+1,kernel_size+1),0);
            int lowerThreshold = 55 ; //155
            double ratio = 1.10; // 1.25
            if(edgeDetection)
                Imgproc.Canny(test, test, lowerThreshold, (int)lowerThreshold*(ratio));
            // Core.absdiff(bg,cg, test);

            Log.d("crash", "pre crash 3");

           // Imgproc.cvtColor(test2, test, Imgproc.COLOR_BGR2GRAY);
            // Convert to BW
          //  Imgproc.threshold(test, test, 35, 255, Imgproc.THRESH_BINARY_INV);

            Log.d("crash", "pre crash 41213");
            // TODO size needs to be odd positive number
            }

        Log.d("crash","pre crash 4");
        Log.d("crash","pre crash 5");
        return test;
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
                    // Load native libs after OpenCV initialization
                    camera_view.enableView();
                }
            }
        }
    };

}
