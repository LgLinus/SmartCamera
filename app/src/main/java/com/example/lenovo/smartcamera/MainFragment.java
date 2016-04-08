package com.example.lenovo.smartcamera;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by Andreas on 2016-04-05.
 */
public class MainFragment extends Fragment
{
    private ImageButton btn_Option;
    private Button btnCapture, btn_edge_detection, btn_plus_gauss, btn_neg_gauss;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_main, container,false);
        initiateListeners(view);
        return view;
    }
   /* public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }*/
    public void optionMenu(View view)
    {
        ((MainActivity)getActivity()).changeFragment("option");
    }
    private void initiateListeners(View rootView)
    {
        btn_Option = (ImageButton) rootView.findViewById(R.id.btn_Option);
        btnCapture = (Button)rootView.findViewById(R.id.btnCapture);
        btn_edge_detection = (Button)rootView.findViewById(R.id.btnEdges);
        btn_plus_gauss = (Button)rootView.findViewById(R.id.btnPlusGauss);
        btn_neg_gauss = (Button)rootView.findViewById(R.id.btnNegGauss);

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
            public void onClick(View v) {
                ((MainActivity) getActivity()).saveImage();
            }
        });
        btn_edge_detection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setEdgeDetection();
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
