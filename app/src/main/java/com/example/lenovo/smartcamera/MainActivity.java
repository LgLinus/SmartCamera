package com.example.lenovo.smartcamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
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
import java.util.logging.Handler;

/**
 * MainActivity
 */
public class MainActivity extends Cloud implements CameraBridgeViewBase.CvCameraViewListener2{

    /* Camera variables */
    // Used to display footage
    CameraBridgeViewBase camera_view;
    int counter=-1;
    // Current image
    Mat current_frame = null;
    Mat old_frame,older_frame;
    Mat output_frame;
    Mat fg,bg;
    int kernel_size = 4;
    int state = 0;
    public static final int NEUTRAL = 0;
    public static final int EDGE_DETECTION = 1;
    public static final int MOTION_DETECTION = 2;
    /* End of camera variables */

    /* GUI elements */
    Button btn_edge_detection, btn_plus_gauss, btn_neg_gauss;
    Button btnCapture;
    Button btn_motion_detection;
    CheckBox cb_people_in_room;
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showTimerDialog();
        initiateComponents();
        initiateListeners();



    }

    /**
     * Funtion used to diplay the tier dialog
     *
     */
    private void showTimerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.timer_dialog, null);
        builder.setView(dialogView);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText etTimer = (EditText) alertDialog.findViewById(R.id.etTimer);

                try {
                    setTimer(Integer.parseInt(etTimer.getText().toString()));
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Error converting time, make sure there's only numbers in the dialog", Toast.LENGTH_LONG).show();
                }
            }
        });


        alertDialog = builder.create();
        alertDialog.show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // alertDialog.show();

            }
        });
      }

    /**
     * Used to set the timer for the photos to be taken
     * @param seconds, intervall between photos
     */
    private void setTimer(int seconds){

        if(seconds<10)
            seconds = 10;

        TimerTask task = new TimerTask(){
            @Override
            public void run(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Take Photo", Toast.LENGTH_SHORT).show();
                        saveImage();
                    }
                });
            }
        };

        new Timer().scheduleAtFixedRate(task, 1000, seconds*1000);
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

        this.uploadFile(file, "filepath", ft.toString(), getPeopleInRoomText(cb_people_in_room.isChecked()));

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

    private void initiateComponents(){

        btnCapture = (Button)findViewById(R.id.btnCapture);
        btn_edge_detection = (Button)findViewById(R.id.btnEdges);
        btn_plus_gauss = (Button)findViewById(R.id.btnPlusgauss);
        btn_neg_gauss = (Button)findViewById(R.id.btnNegGauss);
        btn_motion_detection = (Button)findViewById(R.id.btnMotion);
        cb_people_in_room = (CheckBox)findViewById(R.id.cbPeopleInRoom);
        camera_view = (CameraBridgeViewBase)findViewById(R.id.camera_view);

        camera_view.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);


        // Make the cameraview visible
        camera_view.setVisibility(SurfaceView.VISIBLE);
        // Set the listener t   o the implemented CvCameraViewListener
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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        current_frame = inputFrame.rgba();
        if(output_frame!=null)
            output_frame.release();

        output_frame = current_frame.clone();
        Imgproc.cvtColor(current_frame, output_frame, Imgproc.COLOR_BGR2GRAY);

        if (kernel_size > 0)
            Imgproc.GaussianBlur(output_frame, output_frame, new Size(kernel_size + 1, kernel_size + 1), 0);

        switch (state) {

            case MOTION_DETECTION:
                if (counter == 0 || counter == -1) {
                    older_frame = old_frame;
                    old_frame = current_frame;
                    counter++;
                }
                counter %= 350;

                if (older_frame != null && old_frame != null && (older_frame.width() == old_frame.width())) {

                    ImageManipulation.MotionDetection(older_frame,old_frame,fg,bg,output_frame,kernel_size);
                    fg.release();
                    bg.release();
                    older_frame.release();
                }
                break;

            case EDGE_DETECTION:
                int lowerThreshold = 55; //155
                double ratio = 1.10; // 1.25
                if(old_frame!=null){
                    old_frame.release();
                    old_frame = null;
                }
                if(older_frame!=null){
                    older_frame.release();
                    older_frame = null;
                }
                // Apply edge_detection with the given thresholds and matrixes
                Imgproc.Canny(output_frame, output_frame, lowerThreshold, (int) lowerThreshold * (ratio));
                break;
        }
            return output_frame;

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
     * Init functions for OpenCV
     */
    private void initOpenCV()
    {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
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

    @Override
    public void onPause(){
        super.onPause();

        if(camera_view!=null){
            camera_view.disableView();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(camera_view!=null){
            camera_view.disableView();
        }
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
                if(state==EDGE_DETECTION)
                    state = NEUTRAL;
                else
                    state = EDGE_DETECTION;
            }
        });

        btn_motion_detection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state==MOTION_DETECTION)
                    state = NEUTRAL;
                else
                    state = MOTION_DETECTION;
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
                kernel_size /= 2;
                if (kernel_size <= 1)
                    kernel_size = 1;
            }
        });
    }


}
