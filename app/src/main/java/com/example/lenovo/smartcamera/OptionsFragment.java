package com.example.lenovo.smartcamera;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Andreas on 2016-03-30.
 */
public class OptionsFragment extends Fragment
{
    //------ Setting variables ------
    private int interval;
    private boolean on = false;
    private boolean human;

    private String hour_start, hour_end, minute_start, minute_end;

    private ImageButton btn_Back;
    private Button btn_Confirm;

    private Switch btn_Switch;
    private TextView text_view;

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_option, container,false);
        initiateListeners(view);
        return view;
    }
    public void mainMenu(View view)
    {
        ((MainActivity)getActivity()).changeFragment("main");
    }
    private void initiateListeners(final View rootView)
    {
        btn_Back = (ImageButton) rootView.findViewById(R.id.btn_Back);
        btn_Confirm = (Button) rootView.findViewById(R.id.btn_Confirm);

        btn_Switch = (Switch) rootView.findViewById(R.id.btn_status);
        text_view = (TextView)rootView.findViewById(R.id.text_status);

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
            public void onClick(View v) {
                try {
                    confirm();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
    }
    public void confirm() throws InterruptedException
    {
        try
        {
            EditText et_i = (EditText) getView().findViewById(R.id.interval_text);

            if(!isEmpty(et_i))
            {
                interval = Integer.parseInt(et_i.getText().toString());
                ((MainActivity) getActivity()).setTime(interval);
            }

            EditText et_s = (EditText) getView().findViewById(R.id.start_text);
            EditText et_e = (EditText) getView().findViewById(R.id.end_text);
            if(!isEmpty(et_s))
            {
                ((MainActivity) getActivity()).setClockTime(et_s.getText().toString(), et_e.getText().toString());
            }

            Toast.makeText(((MainActivity) getActivity()), "Confirmed", Toast.LENGTH_SHORT).show();

            if(btn_Switch.isChecked())
                ((MainActivity) getActivity()).onOff(true);

            else
                ((MainActivity) getActivity()).onOff(false);

        }
        catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(((MainActivity) getActivity()), "Fill in all", Toast.LENGTH_SHORT).show();
        }

    }
    private boolean isEmpty(EditText etText)
    {
        return etText.getText().toString().trim().length() == 0;
    }

}
