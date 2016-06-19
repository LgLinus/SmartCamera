package com.example.lenovo.smartcamera;

import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class handles takes images, put them in a buffer, decide if people is in the room and upploads/save labeled images
 * Created by Linus & Andreas on 2016-04-05.
 */
public class MainFragment extends Fragment  implements CameraBridgeViewBase.CvCameraViewListener2
{
    public static boolean CLOUD = true;
    private ImageButton btn_Option;
    private View view;
    private Timer buffert_timer;
    Mat current_frame = null;
    Mat old_frame,older_frame;
    Mat output_frame;
    Mat fg,bg;
    private int BUFFERT_SIZE = 2;
    Mat[] buffer;
    private TimerTask buffert_task;
    private MainFragment frag;
    double LIGHT_THRESHOLD = 0.85;
    CameraBridgeViewBase camera_view;

    final double PEOPLE_LOW_THRESHOLD = 0.001;
    ImageButton btn_OnOff;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_main, container,false);
        initiateListeners(view);
        this.frag = this;
        return view;
    }

    /**
     * Initiate the camera settings
     * @param width :  the width of the frames that will be delivered
     * @param height : the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted(int width, int height)
    {
        Log.d("camerastart","camerastarted");
        this.output_frame = new Mat(width,height, CvType.CV_8U);
        this.bg = new Mat(width,height, CvType.CV_8U);
        this.fg = new Mat();
        this.older_frame = new Mat();
        this.old_frame = new Mat();
    }
    /**
     * Adds an image to the buffer and release the last image in buffer
     * @param matrix: The image that will be added.
     */
    public void addBuffer(Mat matrix)
    {
        if(matrix==null)
            return;
        //Buffer is empty
        if(buffer ==null)
        {
            buffer= new Mat[BUFFERT_SIZE];
            return;
        }
        // IF there is no image :(
        else if(matrix==null)
            return;

        //If the buffer is full, release the last image
        if(buffer[BUFFERT_SIZE-1]!=null)
            buffer[BUFFERT_SIZE-1].release();

        //Moves all the images one step in the buffer
        for(int i = BUFFERT_SIZE-2;i>-1;i--)
            buffer[i+1] = buffer[i];

        Mat toAdd = matrix.clone();
        Imgproc.cvtColor(toAdd, toAdd, Imgproc.COLOR_BGR2GRAY);

        buffer[0] = toAdd;
    }

    public void onCameraViewStopped() {}

    /**
     * This method take pictures
     * @param inputFrame
     * @return
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        //Give the image colors
        current_frame = inputFrame.rgba();

        if(output_frame!=null)
            output_frame.release();


        output_frame = current_frame.clone();
        //The this image will be put the the mainfragment JCameraField
        return output_frame;
    }
    @Override
    public void onResume()
    {
        super.onResume();
        initOpenCV();
    }
    /**
     * Initiate openCV
     */
    private void initOpenCV()
    {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, ((MainActivity) getActivity()), mLoaderCallback);
    }
    /**
     * Iniaiate camera functions and enable JCamerField to work
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(((MainActivity) getActivity()))
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

                    // Make the cameraview visible
                    camera_view.setVisibility(SurfaceView.VISIBLE);
                    camera_view.setCvCameraViewListener(frag);
                    camera_view.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
                }
            }
        }
    };
    /**
     * Change Fragment
     * @param view
     */
    public void optionMenu(View view)
    {
        ((MainActivity)getActivity()).changeFragment("option");
    }

    /**
     * Initiate listeners
     * @param rootView
     */
    private void initiateListeners(View rootView)
    {
        btn_Option = (ImageButton) rootView.findViewById(R.id.btn_Option);
        btn_OnOff = (ImageButton) rootView.findViewById(R.id.btn_OnOff);

        camera_view = (CameraBridgeViewBase)rootView.findViewById(R.id.camera_view);

        btn_Option.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gotoOptions();
            }

        });
        btn_OnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity)getActivity();
                if ((mainActivity.getStatus() == true)){
                  //  btn_OnOff.setBackground(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.on));
                    mainActivity.onOff(false);
                }
                else{
                  //  btn_OnOff.setBackground(getActivity().getApplicationContext().getResources().getDrawable(R.drawable.off));
                    mainActivity.onOff(true);}

            }

        });
    }
      /**
     * Save the image currently being displayed
     */
    public void saveImage(){
        Log.d("MAINFRAGMENT","save image");
        if(this.buffer ==null)
            return;
        Mat median_matrix,current_low_res_frame;

        // Acquire medaian image
        Mat[] buffer = new Mat[this.buffer.length];

        for(int i = 0; i < buffer.length;i++){
            buffer[i] = this.buffer[i].clone();
        }

        median_matrix = ImageManipulation.acquireMedian(ImageManipulation.resizeImage(2, 2, buffer));
        // matrix = ImageManipulation.acquireMedian(buffer);
        // Resize current image
        current_low_res_frame = ImageManipulation.resizeImage(2, 2, current_frame);
        Imgproc.cvtColor(current_low_res_frame, current_low_res_frame, Imgproc.COLOR_BGR2GRAY);

        Mat absdiff_output = new Mat(median_matrix.height(),median_matrix.width(), CvType.CV_8UC1);

        // Apply absdiff on new img + median image
        ImageManipulation.useAbsDiff(median_matrix, current_low_res_frame, absdiff_output,60);
        double ratio = ImageManipulation.whiteBlackRatio(absdiff_output);

        /* Save the relevant matrixes */
        saveImage(ratio);

        // Release the created matrix to avoid memory leaks
        median_matrix.release();
        absdiff_output.release();

        for(int i = 0; i < buffer.length;i++){
            buffer[i].release();
        }
        buffer = null;
    }

    /**
     * Function saving the matrix to the local storage on the device as a .png file
     * @param matrix to save
     * @param code ending of file
     * @return
     */
    public File saveMatrix(Mat matrix, int code)
    {
        Bitmap resultBitmap = Bitmap.createBitmap(matrix.cols(),matrix.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matrix, resultBitmap);

        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy/MM/dd_hh:mm:ss");

        String filename = "temp";

        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File file=new File(directory,ft.format(date)+".png");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), resultBitmap, filename+".png", "xaxa");
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {

        }
        return file;
    }

    private void saveImage(double ratio){
        Log.d("Mainfragment","choice is: " + MainActivity.choice);
        if(ratio>=PEOPLE_LOW_THRESHOLD && ratio <LIGHT_THRESHOLD)
            if(MainActivity.choice == MainActivity.CLOUD)
                uploadMatrix(saveMatrix(current_frame,0),"people in room ratio:"+ratio,"people_in_room"+"_ratio:"+ratio);
            else
                saveMatrix(current_frame,0);

        else if(ratio<LIGHT_THRESHOLD)
            if(MainActivity.choice == MainActivity.CLOUD)
                uploadMatrix(saveMatrix(current_frame, 1),"empty room ratio:"+ratio,"empty_room"+"_ratio:"+ratio);
            else
                saveMatrix(current_frame,1);
    }
    /**
     * Function used to upload the given matrix to google drive
     * @param file to upload
     * @param tag to add to file ending
     */
    public void uploadMatrix(File file, String tag, String file_tag)
    {
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd_kk_mm_ss");
        ((MainActivity)getActivity()).uploadFile(file, "filepath", file_tag+"_"+ft.format(date) + "1234", tag);
    }

    /**
     * Sets the timmer for the average buffer that will add a photo to the buffer in a period of time.
     * @param timer_seconds
     */
    public void setTimerMedianfilter(int timer_seconds)
    {

        Log.d("MainActivity","made it:6");

        this.buffer = new Mat[BUFFERT_SIZE];
        if(buffert_task!=null)
        {
            buffert_task.cancel();
            buffert_task = null;
        }
        if(buffert_timer!=null)
        {
            buffert_timer.cancel();
            buffert_timer=null;
        }
        buffert_task = new TimerTask()
        {
            @Override
            public void run(){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addBuffer(current_frame);
                        Log.d("MAINACTIVITY", "added image to buffer");
                    }
                });
            }
        };

        Log.d("MainActivity", "made it: 7");
        buffert_timer = new Timer();
        buffert_timer.scheduleAtFixedRate(buffert_task, (long) (((double) timer_seconds / (double) BUFFERT_SIZE) * 1000), (long) ((double) timer_seconds / (double) (BUFFERT_SIZE + 1)) * 1000); /// 4

       // if(current_frame!=null)
         //   addBuffer(current_frame);
        Log.d("MainActivity", "made it: 9");
    }

    /**
     * Go to the Optionfragment
     */
    private void gotoOptions()
    {
        if(this.buffert_task!=null)
            buffert_task.cancel();
        if(buffert_timer!=null)
            buffert_timer.cancel();

        getView().clearFocus();
        ((MainActivity) getActivity()).changeFragment("option");
    }

    public void releaseCamera(){
        this.camera_view.disableView();
    }
}

