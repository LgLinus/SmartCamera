package com.example.lenovo.smartcamera;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//import android.support.v7.app.AppCompatActivity;


/**
 * MainActivity
 */
public class MainActivity extends Cloud implements CameraBridgeViewBase.CvCameraViewListener2 {
    //    ---.---  Camera variables ---.--- //
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
    //    ---.---  Fragment ---.--- //

    private Fragment fragment;
    private FragmentManager fragment_Manager;
    private FragmentTransaction fragment_Transaction;

    private OptionsFragment option_Fragment;
    private MainFragment main_Fragment;

    // ---.--- Triggers ---.---
    //Timer interval
    private int second = 1000;
    private int start_time;
    private int interval;
    private Thread timer_thread;

    //On/Off
    private boolean on = false;

    //Clock time
    private boolean itIsTime = false;
    private Calendar cal;
    private int hour_start, hour_end, minute_start, minute_end;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create the fragments
        option_Fragment = new OptionsFragment();
        main_Fragment = new MainFragment();
        fragment_Manager = getFragmentManager();
        fragment_Transaction = fragment_Manager.beginTransaction();
        //Load the menufragment to the display
        fragment_Transaction.replace(R.id.fragment_container, main_Fragment);
        fragment_Transaction.commit();

    }

// xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx Fragment xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    /**
     * This method changes between the fragments menu
     * @param id = the name of the fragment that will be displayed
     */
    public void changeFragment(String id)
    {
        if(id.equals("option"))
        {
            fragment_Manager = getFragmentManager();
            fragment_Transaction = fragment_Manager.beginTransaction();
            fragment_Transaction.replace(R.id.fragment_container, option_Fragment);
            fragment_Transaction.commit();
        }
        else if(id.equals("main"))
        {
            fragment_Manager = getFragmentManager();
            fragment_Transaction = fragment_Manager.beginTransaction();
            fragment_Transaction.replace(R.id.fragment_container, main_Fragment);
            fragment_Transaction.commit();
        }

    }

// xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx Image Handling xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    public void saveImage(){

        Mat matrix = current_frame;
        // Create bitmap from image
        Bitmap resultBitmap = Bitmap.createBitmap(matrix.cols(),matrix.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matrix, resultBitmap);

        Date date = new Date();

        SimpleDateFormat ft = new SimpleDateFormat ("yyyy/mm/dd_hh/mm/ss");

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
                    //camera_view.enableView();
                }
            }
        }
    };

//-----------------------------  Triggers ---------------------------
    public void clockTime(int h_start,int h_end,int m_start,int m_end)
    {

    }
    /**
     * This method is like an on/off switch, which changes the variable "on" to false/true
     */
    public void onOff()
    {
        if(on)
            on = false;
        else
        on = true;
    }
    public void setTime(int time)
    {
        start_time = time;
        interval = time;

        //If the thread is running, stop it so the new timer can be applied
        if(!timer_thread.isInterrupted())
            timer_thread.interrupt();

        timer_thread = new Thread()
        {
            public void run()
            {
                while(!Thread.interrupted())
                {
                    //Sleep until the interval time has passed
                    while (interval > 0)
                    {
                        //Decreases the interval time by one second
                        try
                        {
                            Thread.sleep(second);
                            interval -= 1;
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    //Reset the interval timer
                    interval = start_time;
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(MainActivity.this, "Take Photo", Toast.LENGTH_SHORT).show();
                            saveImage();
                        }
                    });
                }
            }
        };
        timer_thread.start();
    }
//  GAAUUSS AND SHIIIEEET
    public void setEdgeDetection()
    {
        edgeDetection = !edgeDetection;
    }
    public void setKernel_size(int size)
    {
        kernel_size*= size;
    }

}
