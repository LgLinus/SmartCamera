package com.example.lenovo.smartcamera;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * Created by Andreas on 2016-03-30.
 */
public class OptionsFragment extends Fragment
{
    //------ Setting variables ------
    private int interval;
    private boolean on = false;
    private boolean human;

    private ImageButton btn_Back;
    private Button btn_Confirm;

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_option, container,false);
        initiateListeners(view);
        return view;
    }
   /* public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }*/
    public void mainMenu(View view)
    {
        ((MainActivity)getActivity()).changeFragment("main");
    }
    private void initiateListeners(View rootView)
    {
        btn_Back = (ImageButton) rootView.findViewById(R.id.btn_Back);
        btn_Confirm = (Button) rootView.findViewById(R.id.btn_Confirm);

        btn_Back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((MainActivity)getActivity()).changeFragment("main");
            }
        });
        btn_Confirm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((MainActivity) getActivity()).changeFragment("option");
            }

        });
    }
    // Use in order to get view by id
    // Note: you can get the fragment view anywhere in the class by using getView()
    // once onCreateView() has been executed successfully. i.e.
   /* @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.interval_text).setOnClickListener((View.OnClickListener) this);
        view.findViewById(R.id.interval_text).setOnClickListener((View.OnClickListener) this);

    }*/
    public void confirm(View v)
    {
        EditText et = (EditText) getView().findViewById(R.id.interval_text);
        interval = Integer.parseInt(et.getText().toString());
    }
    public void setInterval(int time)
    {

    }
}
