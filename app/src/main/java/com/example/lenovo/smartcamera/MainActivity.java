package com.example.lenovo.smartcamera;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * MainActivity
 * Created by Linus on 2016-06-22
 */
public class MainActivity extends Cloud  implements CameraBridgeViewBase.CvCameraViewListener2{

    private Button btn_capture;
    private Mat captured_frame;
    private Mat prev_frame;
    JavaCameraView camera_view;
    private MainActivity mainActivity = this;
    private EditText et_folder_name;

    /* Used for saving the foler name */
    private final String PREFS = "appPrefs";
    private final String KEY_FOLDER = "_folder";

    private GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Create the fragments
        Window window = getWindow();

        // Retrieve the objects from the XML layout (res->activity_main.xml)
        camera_view = (JavaCameraView)findViewById(R.id.camera_view);
        btn_capture = (Button)findViewById(R.id.btn_capture);
        et_folder_name = (EditText)findViewById(R.id.et_folder_name);

        // Call saveImage function when the capture button is pressed
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }

        });

        // Retrieve the saved folder name
        SharedPreferences prefs = this.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        et_folder_name.setText(prefs.getString(KEY_FOLDER,"temp"));

        // Force the screen to stay on (Can be removed if you don't want this )
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD); // Unlock the device if locked
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); // Turn screen on if off
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on
    }

    @Override
    protected void onPause(){
        super.onPause();
        releaseCamera();
    }

    /**
     * Initiate the camera settings
     * @param width :  the width of the frames that will be delivered
     * @param height : the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted(int width, int height)
    {
        this.captured_frame = new Mat(width,height,CvType.CV_8U);
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

        // Avoid memory leaks, need to release matrices manually
        if(captured_frame!=null)
            captured_frame.release();

        // Matrix used for display purposes
        captured_frame = inputFrame.rgba();

        return captured_frame;
    }

    /* Reinitialize OpenCV when activity is in focus again*/
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
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    /**
     * Initialize camera functions and enable JCamerField to work
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch(status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    // Simple debug message, confirming OpenCV loaded succesfully
                    Log.d("init", "OpenCV loaded successfully");

                    // Initialize camera
                    camera_view.enableView();
                    camera_view.setVisibility(SurfaceView.VISIBLE);
                    camera_view.setCvCameraViewListener(mainActivity);
                    camera_view.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);

                }
            }
        }
    };

    /**
     * Save the image currently being displayed (current_frame)
     */
    public void saveImage(){

        // Create GPS object

        gps = new GPSTracker(MainActivity.this);
        double[] coordinates =getGPSCoordinates();


        // Resize image
        Mat frame_to_save = ImageManipulation.resizeImage_Size(224,224,captured_frame);

        //ACquire date
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd_kk_mm_ss");

        uploadMatrix(saveMatrix(frame_to_save), "test", coordinates[1] + ";latitude_" + coordinates[0]);

        if(frame_to_save!=null)
            frame_to_save.release();

    }

    /**
     * Function saving the matrix to the local storage on the device as a .png file
     * @param matrix to save
     * @return
     */
    public File saveMatrix(Mat matrix)
    {
        Bitmap resultBitmap = Bitmap.createBitmap(matrix.cols(),matrix.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matrix, resultBitmap);

        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy/MM/dd_hh:mm:ss");

        String filename = "temp";

        ContextWrapper cw = new ContextWrapper(this.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File file=new File(directory,"bcde"+".png");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            MediaStore.Images.Media.insertImage(this.getContentResolver(), resultBitmap, filename + ".png", "xaxa");
            matrix.release();
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
    public void uploadMatrix(File file, String tag, String file_tag)
    {
        String folder_name = et_folder_name.getText().toString();

        // Remove unallowed characters
        folder_name = folder_name.replace(" ","");
        folder_name = folder_name.replace("\n","");
        folder_name = folder_name.replace("\t","");
        et_folder_name.setText(folder_name);
        SharedPreferences prefs = getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_FOLDER,folder_name);
        editor.commit();

        uploadFile(file, folder_name, file_tag + "_ 1234", tag);
    }

    /**
     * Returns the GPS coordinates if found
     * @return
     */
    public double[] getGPSCoordinates(){
        double[] coordinates = new double[2];
        // Check if GPS enabled
        if(gps.canGetLocation()) {

            coordinates[0] = gps.getLatitude();
            coordinates[1] = gps.getLongitude();

            // Display values (Can be removed if unwanted)
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + coordinates[0] + "\nLong: " + coordinates[1], Toast.LENGTH_LONG).show();
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps.showSettingsAlert();
        }
        return coordinates;
    }
    public void releaseCamera(){
        this.camera_view.disableView();
    }
}