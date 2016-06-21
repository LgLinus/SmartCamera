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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
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
    private Button btn_Capture;
    private View view;
    private MainFragment frag;
    private Mat captured_frame,current_frame;
    CameraBridgeViewBase camera_view;

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
        this.captured_frame = new Mat(width,height, CvType.CV_8U);
        this.current_frame = new Mat(width,height,CvType.CV_8U);
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
        // Avoid memory leaks
        if(current_frame!=null)
            current_frame.release();

        current_frame = inputFrame.rgba();
        Mat rot_matrix = Imgproc.getRotationMatrix2D(new Point(current_frame.cols()/2.0f,current_frame.rows()/2.0f),90,1.0);
        int val = Math.max(current_frame.cols(),current_frame.rows());
        Imgproc.warpAffine(current_frame,current_frame,rot_matrix,new Size(val,val));
        return current_frame;
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
                    camera_view.setRotation((float)((1*Math.PI)/2));
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
     * Initiate listeners
     * @param rootView
     */
    private void initiateListeners(View rootView)
    {
        btn_Capture = (Button) rootView.findViewById(R.id.btn_capture);
        camera_view = (CameraBridgeViewBase)rootView.findViewById(R.id.camera_view);
        btn_Capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                saveImage();
            }

        });
    }
      /**
     * Save the image currently being displayed
     */
    public void saveImage(){
        Log.d("MAINFRAGMENT","save image");
        this.captured_frame = current_frame.clone();

        this.captured_frame = ImageManipulation.resizeImage_Size(224,224,captured_frame);

        if(captured_frame==null||captured_frame.width()<1||captured_frame.height()<1)
            return;

        uploadMatrix(saveMatrix(captured_frame), "test", "filename_temp");


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

        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
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
            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), resultBitmap, filename + ".png", "xaxa");
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
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd_kk_mm_ss");
        ((MainActivity) getActivity()).uploadFile(file, "filepath", file_tag + "_" + ft.format(date) + "1234", tag);
    }


    public void releaseCamera(){
        this.camera_view.disableView();
    }
}

