package com.example.lenovo.smartcamera;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

/**
 * Created by Andreas on 2016-04-05.
 */
public class MainFragment extends Fragment  implements CameraBridgeViewBase.CvCameraViewListener2
{


    private ImageButton btn_Option;
    private Button btnCapture, btn_edge_detection, btn_plus_gauss, btn_neg_gauss;
    private View view;
    CameraBridgeViewBase camera_view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_main, container,false);
        initiateListeners(view);
        return view;
    }
    public void onCameraViewStarted(int width, int height)
    {
    }

    public void onCameraViewStopped()
    {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        return null;
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
                    //camera_view.enableView();
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

        changeStatus(rootView);


        btn_Option.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((MainActivity) getActivity()).changeFragment("option");
            }

        });
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
        });
    }
}
