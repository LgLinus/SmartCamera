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

import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



/**
 * MainActivity
 */
public class MainActivity extends Cloud {
    //    ---.---  Camera variables ---.--- //
    // Used to display footage
    //CameraBridgeViewBase camera_view;

    final int ORIGINAL = 0;
    final int ABSDIFF = 1;
    public static final int BUFFERUPDATESECONDS = 5;
    public static int timer_seconds=10;
    private SendInfo send;

    int counter=-1;
    int ratioCounter = 1;

    Mat current_frame = null;
    Mat old_frame,older_frame;
    Mat output_frame;
    Mat fg,bg;
    private final int BUFFERT_SIZE = 2;
    Mat[] buffert;

    public static final int NEUTRAL = 0;
    public static final int EDGE_DETECTION = 1;
    public static final int MOTION_DETECTION = 2;
    final double PEOPLE_LOW_THRESHOLD = 0.01;

    // Current image
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
    private boolean run = false;
    private Thread timer_thread = null;

    //On/Off
    private boolean on = false;

    //Clock time
    private boolean itIsTime = false;
    private String start_clock, end_clock;


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
//-----------------------------  Triggers ---------------------------

    /**
     * This method return if the value of the onOff switch
     * @return: True if on and false if off
     */
    public boolean getStatus()
    {
        return on;
    }
    /**
     * This method is like an on/off switch, which changes the variable "on" to false/true
     */
    public void onOff(boolean b)
    {
        on = b;
    }
    /**
     * This method check if the current time is in between the setClocktime period
     * @return: True if it's time to take pictures, false if the picture time have passed.
     */
    public boolean isItTime()
    {
        //No time have been set
        if(start_clock.equals(null) && end_clock.equals(null) )
            return true;
        else
        {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
            String current_time = sdf.format(cal.getTime());

            //If current_time is higher then the start time and lower then the end time, then taking photo is ok.
            if(current_time.compareTo(start_clock) == 1 && current_time.compareTo(end_clock) == -1)
                return true;

            return false;
        }

    }
    /**
     * This method sets the time when the camera should take pictures
     * @param start: The start time picture will start be taken
     * @param end: The end time picture will no longer be taken ones passed end time
     * @throws ParseException
     */
    public void setClockTime(String start, String end) throws ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
        start_clock = sdf.format(sdf.parse(start));
        end_clock = sdf.format(sdf.parse(end));
    }

    /**
     * This method sets the time how often the camera shall save pictures
     * @param time: The intervall time
     * @throws InterruptedException
     */
    public void setTime(int time) throws InterruptedException
    {
        //If the thread is running, stop it so the new timer can be applied
        if(run)
        {
            run = false;
            //Wait for the current thread to end
            timer_thread.join();
        }
        run = true;
        start_time = time;
        interval = time;

        timer_thread = new Thread()
        {
            public void run()
            {
                while(run)
                {
                    //Sleep until the interval time has passed
                    while (interval > 0 && run)
                    {
                        //Decreases the interval time by one second
                        try
                        {
                            Thread.sleep(second);
                            interval -= 1;
                            Log.d("init", "Count: " + interval);
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    //Reset the interval timer
                    interval = start_time;
                    if(run)
                    {
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

// ------------------------------- Image Handling
    /**
     * Save the image currently being displayed
     */
    public void saveImage(){
        Mat median_matrix,current_low_res_frame;

        // Acquire medaian image
        median_matrix = ImageManipulation.acquireMedian(ImageManipulation.resizeImage(2,2,buffert));
        // matrix = ImageManipulation.acquireMedian(buffert);
        // Resize current image
        current_low_res_frame = ImageManipulation.resizeImage(2, 2, current_frame);
        Imgproc.cvtColor(current_low_res_frame, current_low_res_frame, Imgproc.COLOR_BGR2GRAY);

        Mat absdiff_output = new Mat(median_matrix.height(),median_matrix.width(), CvType.CV_8UC1);

        // Apply absdiff on new img + median image
        ImageManipulation.useAbsDiff(median_matrix, current_low_res_frame, absdiff_output);
        double ratio = ImageManipulation.whiteBlackRatio(absdiff_output);


        send.sendMessage("ID," + ratioCounter + "\t,Whiteblack ratio,"+ratio);
        ratioCounter++;
        //uploadMatrix(saveMatrix(output,1),1);
        //uploadMatrix(saveMatrix(current_low_res_frame,0),0);

        /* Save the relevant matrixes */
        saveMatrix(absdiff_output,1);
        saveMatrix(current_low_res_frame,0);
        saveMatrix(median_matrix,2);

        if(ratio>=PEOPLE_LOW_THRESHOLD)
            uploadMatrix(saveMatrix(current_frame,0),"people in room");
        else
            uploadMatrix(saveMatrix(current_frame,1),"empty room");

        // Release the created matrix to avoid memory leaks
        median_matrix.release();
        absdiff_output.release();
    }
    /**
     * Function saving the matrix to the local storage on the device as a .png file
     * @param matrix to save
     * @param code ending of file
     * @return
     */
    public File saveMatrix(Mat matrix, int code){
        Bitmap resultBitmap = Bitmap.createBitmap(matrix.cols(),matrix.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matrix, resultBitmap);

        Date date = new Date();
        Log.d("MAINACTIVITY", "code: " + code);
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy/mm/dd_hh:mm:ss");

        String filename = "test"+String.valueOf(code);

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
        return file;
    }
    /**
     * Function used to upload the given matrix to google drive
     * @param file to upload
     * @param tag to add to file ending
     */
    public void uploadMatrix(File file, String tag){
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/mm/dd:hh:ss");
        this.uploadFile(file, "filepath", ft.format(date)+"1234", tag);

    }
    /**
     * Method used to return a string given the given input
     * @param people_in_room, boolean value
     * @return "people" or "empty"
     */
    private String getPeopleInRoomText(boolean people_in_room)
    {
        if(people_in_room)
            return "people";
        else
            return "empty";
    }
    /**
     * Called when the cameraview is setup
     * @param width -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    public void onCameraViewStarted(int width, int height)
    {
        Log.d("camerastart","camerastarted");
        this.output_frame = new Mat(width,height, CvType.CV_8U);
        this.bg = new Mat(width,height, CvType.CV_8U);
        this.fg = new Mat();
        this.older_frame = new Mat();
        this.old_frame = new Mat();
        this.buffert = new Mat[BUFFERT_SIZE];
    }
    /**
     * Adds the given matrix to the buffert and release the last matrix in buffert
     * @param matrix
     */
    public void addBuffert(Mat matrix){

        if(buffert[BUFFERT_SIZE-1]!=null)
            buffert[BUFFERT_SIZE-1].release();

        for(int i = BUFFERT_SIZE-2;i>-1;i--){
            buffert[i+1] = buffert[i];
        }
        Mat toAdd = matrix.clone();
        Imgproc.cvtColor(toAdd,toAdd,Imgproc.COLOR_BGR2GRAY);


        buffert[0] = toAdd;
    }


}
