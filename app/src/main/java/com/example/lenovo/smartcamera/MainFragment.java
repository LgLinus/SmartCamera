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
 * Created by Andreas on 2016-04-05.
 */
public class MainFragment extends Fragment  implements CameraBridgeViewBase.CvCameraViewListener2
{


    public static boolean CLOUD = true;
    private ImageButton btn_Option;
    private Button btnCapture, btn_edge_detection, btn_plus_gauss, btn_neg_gauss;
    private View view;
    private Timer buffert_timer;
    Mat current_frame = null;
    Mat old_frame,older_frame;
    Mat output_frame;
    Mat fg,bg;
    private final int BUFFERT_SIZE = 2;
    Mat[] buffer;
    final int ORIGINAL = 0;
    final int ABSDIFF = 1;
    public static final int BUFFERUPDATESECONDS = 5;
    private SendInfo send;
    private TimerTask buffert_task;
    private MainFragment frag;
    int counter=-1;
    int ratioCounter = 1;
    double LIGHT_THRESHOLD = 0.85;

    public static final int NEUTRAL = 0;
    public static final int EDGE_DETECTION = 1;
    public static final int MOTION_DETECTION = 2;
    final double PEOPLE_LOW_THRESHOLD = 0.001;
    // Current image
    boolean edgeDetection = false;
    int kernel_size = 4;

    CameraBridgeViewBase camera_view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_main, container,false);
        initiateListeners(view);
        this.frag = this;
        return view;
    }

    @Override
    public void onCameraViewStarted(int width, int height)
    {
        Log.d("camerastart","camerastarted");
        this.output_frame = new Mat(width,height, CvType.CV_8U);
        this.bg = new Mat(width,height, CvType.CV_8U);
        this.fg = new Mat();
        this.older_frame = new Mat();
        this.old_frame = new Mat();
        this.buffer = new Mat[BUFFERT_SIZE];
        send = new SendInfo();
    }
    /**
     * Adds the given matrix to the buffer and release the last matrix in buffer
     * @param matrix
     */
    public void addBuffer(Mat matrix){
        if(buffer ==null){
            buffer= new Mat[BUFFERT_SIZE];
            return;}
        else if(matrix==null){
            return;
        }

        if(buffer[BUFFERT_SIZE-1]!=null)
            buffer[BUFFERT_SIZE-1].release();

        for(int i = BUFFERT_SIZE-2;i>-1;i--){
            buffer[i+1] = buffer[i];
        }

        Mat toAdd = matrix.clone();
        Imgproc.cvtColor(toAdd,toAdd,Imgproc.COLOR_BGR2GRAY);

        buffer[0] = toAdd;
    }

    public void onCameraViewStopped()
    {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {

        current_frame = inputFrame.rgba();

        // Apply blurring
        if (kernel_size > 0)
            Imgproc.GaussianBlur(current_frame, current_frame, new Size(kernel_size + 1, kernel_size + 1), 0);

        if(output_frame!=null)
            output_frame.release();

        output_frame = current_frame.clone();

        // Turn output frame matrix gray
        Imgproc.cvtColor(output_frame, output_frame, Imgproc.COLOR_BGR2GRAY);

       /* switch (state) {

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
        }*/
        return output_frame;

    }

    @Override
    public void onResume()
    {
        super.onResume();
        initOpenCV();
    }
    private void initOpenCV()
    {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, ((MainActivity) getActivity()), mLoaderCallback);
    }
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

                    // Set the listener t   o the implemented CvCameraViewListener
                    //camera_view.setCvCameraViewListener(this);


                }
            }
        }
    };
    public void optionMenu(View view)
    {
        ((MainActivity)getActivity()).changeFragment("option");
    }
    public void changeStatus(View rv)
    {
        TextView text_view;
        LinearLayout ll;
        ll = (LinearLayout) rv.findViewById(R.id.color_layout);
        text_view = (TextView) rv.findViewById(R.id.text_status);
        if(((MainActivity)getActivity()).getStatus())
        {
            ll.setBackgroundColor(Color.GREEN);
            text_view.setText("On");
            Toast.makeText(((MainActivity) getActivity()), "On", Toast.LENGTH_SHORT).show();
        }
        else
        {
            ll.setBackgroundColor(Color.RED);
            text_view.setText("Off");
            Toast.makeText(((MainActivity) getActivity()), "Off", Toast.LENGTH_SHORT).show();
        }
    }
    private void initiateListeners(View rootView)
    {
        btn_Option = (ImageButton) rootView.findViewById(R.id.btn_Option);

        btnCapture = (Button)rootView.findViewById(R.id.btnCapture);
        btn_edge_detection = (Button)rootView.findViewById(R.id.btnEdges);
        btn_plus_gauss = (Button)rootView.findViewById(R.id.btnPlusGauss);
        btn_neg_gauss = (Button)rootView.findViewById(R.id.btnNegGauss);

        camera_view = (CameraBridgeViewBase)rootView.findViewById(R.id.camera_view);

        camera_view.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        changeStatus(rootView);


        btn_Option.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gotoOptions();
            }

        });/*
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
            }
        });
        btn_edge_detection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //((MainActivity) getActivity()).apa();
                //((MainActivity) getActivity()).setEdgeDetection();
            }
        });
        btn_plus_gauss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setKernel_size(-2);
            }
        });

        btn_neg_gauss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setKernel_size(2);
            }
        });*/
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

        median_matrix = ImageManipulation.acquireMedian(ImageManipulation.resizeImage(2,2, buffer));
        // matrix = ImageManipulation.acquireMedian(buffer);
        // Resize current image
        current_low_res_frame = ImageManipulation.resizeImage(2, 2, current_frame);
        Imgproc.cvtColor(current_low_res_frame, current_low_res_frame, Imgproc.COLOR_BGR2GRAY);

        Mat absdiff_output = new Mat(median_matrix.height(),median_matrix.width(), CvType.CV_8UC1);

        // Apply absdiff on new img + median image
        ImageManipulation.useAbsDiff(median_matrix, current_low_res_frame, absdiff_output,60);
        double ratio = ImageManipulation.whiteBlackRatio(absdiff_output);
        ratioCounter++;

        //uploadMatrix(saveMatrix(output,1),1);
        //uploadMatrix(saveMatrix(current_low_res_frame,0),0);

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

    private void saveImage(double ratio){
    //    saveMatrix(absdiff_output,1);
    //   saveMatrix(current_low_res_frame,0);
    //    saveMatrix(median_matrix,2);


        if(ratio>=PEOPLE_LOW_THRESHOLD && ratio <LIGHT_THRESHOLD)
            if(MainActivity.choice == MainActivity.CLOUD)
            uploadMatrix(saveMatrix(current_frame,0),"people in room id:15 ratio:"+ratio,"people_in_room"+"_ratio:"+ratio);
            else
                saveMatrix(current_frame,0);

        else if(ratio<LIGHT_THRESHOLD)
            if(MainActivity.choice == MainActivity.CLOUD)
                uploadMatrix(saveMatrix(current_frame,1),"empty room id:15 ratio:"+ratio,"empty_room"+"_ratio:"+ratio);
            else
                saveMatrix(current_frame,1);
    }

    /**
     * Save the image currently being displayed
     */
    public void saveImageTetes(){
        Log.d("MAINFRAGMENT","save image");
        if(this.buffer ==null)
            return;
        Mat median_matrix,current_low_res_frame;

        // Acquire medaian image
        Mat[] buffer = new Mat[this.buffer.length];

        for(int i = 0; i < buffer.length;i++){
            buffer[i] = this.buffer[i].clone();
        }

        median_matrix = ImageManipulation.acquireMedian(ImageManipulation.resizeImage(2,2, buffer));
        // matrix = ImageManipulation.acquireMedian(buffer);
        // Resize current image
        current_low_res_frame = ImageManipulation.resizeImage(2, 2, current_frame);
        Imgproc.cvtColor(current_low_res_frame, current_low_res_frame, Imgproc.COLOR_BGR2GRAY);

        for(int i = 0; i <7;i++) {
            Mat absdiff_output = new Mat(median_matrix.height(), median_matrix.width(), CvType.CV_8UC1);

            // Apply absdiff on new img + median image
            ImageManipulation.useAbsDiff(median_matrix, current_low_res_frame, absdiff_output, 40 + (i*10));
            double ratio = ImageManipulation.whiteBlackRatio(absdiff_output);
            double LIGHT_THRESHOLD = 1.00;

            //send.sendMessage("ID," + ratioCounter + "\t,Whiteblack ratio,"+ratio);
            ratioCounter++;
            //uploadMatrix(saveMatrix(output,1),1);
            //uploadMatrix(saveMatrix(current_low_res_frame,0),0);

        /* Save the relevant matrixes */
          //  saveMatrix(absdiff_output, 1);
            //saveMatrix(current_low_res_frame, 0);
            //saveMatrix(median_matrix, 2);


            if (ratio >= PEOPLE_LOW_THRESHOLD && ratio < LIGHT_THRESHOLD)
                uploadMatrix(saveMatrix(current_frame, i+1), "people in room id:13 ratio:" + ratio, "people_in_room_b"+(40+(i*10))+"_0.1");
            else if (ratio < LIGHT_THRESHOLD)
                uploadMatrix(saveMatrix(current_frame, i+1), "empty room id ratio:" + ratio, "empty_room"+(40+(i*10))+"_0.1");

            if(ratio>=0.01&& ratio<LIGHT_THRESHOLD)
                uploadMatrix(saveMatrix(current_frame, i), "people in id:3 ratio:" + ratio, "people_in_room_b"+(40+(i*10))+"_1");
            else if (ratio < LIGHT_THRESHOLD)
                uploadMatrix(saveMatrix(current_frame, i+1), "empty room id:3 ratio:" + ratio, "empty_room"+(40+(i*10))+"_1");


            Log.d("MAINFRAGMENT","went through once");


            absdiff_output.release();
        }
        // Release the created matrix to avoid memory leaks
        median_matrix.release();

        for (int j = 0; j < buffer.length; j++) {
            buffer[j].release();
        }
        buffer = null;
    }

    /**
     * Save the image currently being displayed
     */
    public void saveImageTestResize() {
        int size = 1;
        Log.d("MAINFRAGMENT", "save image");
        for(size = 1; size < 8; size++){
        long startTime = System.currentTimeMillis();

        if (this.buffer == null)
            return;
        Mat median_matrix, current_low_res_frame;

        // Acquire medaian image
        Mat[] buffer = new Mat[this.buffer.length];

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = this.buffer[i].clone();
        }

        median_matrix = ImageManipulation.acquireMedian(ImageManipulation.resizeImage(size, size, buffer));
        // matrix = ImageManipulation.acquireMedian(buffer);
        // Resize current image
        current_low_res_frame = ImageManipulation.resizeImage(size, size, current_frame);
        Imgproc.cvtColor(current_low_res_frame, current_low_res_frame, Imgproc.COLOR_BGR2GRAY);

        Mat absdiff_output = new Mat(median_matrix.height(), median_matrix.width(), CvType.CV_8UC1);

        // Apply absdiff on new img + median image
        ImageManipulation.useAbsDiff(median_matrix, current_low_res_frame, absdiff_output,40);
        double ratio = ImageManipulation.whiteBlackRatio(absdiff_output);
        double LIGHT_THRESHOLD = 1.00;

            this.uploadMatrix(this.saveMatrix(current_low_res_frame,size),"rescale:1","rescale:1");

        //send.sendMessage("ID," + ratioCounter + "\t,Whiteblack ratio,"+ratio);
        ratioCounter++;

        // Release the created matrix to avoid memory leaks
        median_matrix.release();
        absdiff_output.release();

        for (int i = 0; i < buffer.length; i++) {
            buffer[i].release();
        }
        buffer = null;

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Log.d("MainFragment", "time taken for: " + size + "\t" + elapsedTime);
    }
    }

    // ------------------------------- Image Handling
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
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy/MM/dd_hh:mm:ss");

        String filename = "test"+String.valueOf(code);

        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
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
            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), resultBitmap, filename+".png", "xaxa");
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
    public void uploadMatrix(File file, String tag, String file_tag){
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd_kk_mm_ss");
        ((MainActivity)getActivity()).uploadFile(file, "filepath", file_tag+"_"+ft.format(date) + "1234", tag);

    }


    public void setTimerMedianfilter(int timer_seconds){


        if(buffert_task!=null) {
            buffert_task.cancel();
            buffert_task = null;
        }
        if(buffert_timer!=null)
        {
            buffert_timer.cancel();
            buffert_timer=null;
        }
        buffert_task = new TimerTask(){
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

   buffert_timer = new Timer();
        Log.d("MAINFRAGMENT", "Value timer: " +  (timer_seconds / ((double)BUFFERT_SIZE + 1)) * 1000);
        buffert_timer.scheduleAtFixedRate(buffert_task,(long)( (timer_seconds / ((double)BUFFERT_SIZE+1)) * 1000),(long) (timer_seconds / ((double)BUFFERT_SIZE + 1)) * 1000); /// 4
    }

    private void gotoOptions(){
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
