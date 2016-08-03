package com.example.lenovo.smartcamera;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


/**
 * This class contains the settings option that will control how often an image is taken, when an image is taken and where
 * the images should be stored.
 * Created by Andreas on 2016-03-30.
 */
public class OptionsFragment extends Fragment
{
    private ImageButton btn_back;
    private CheckBox cb_storage;
    private Button btn_confirm;
    private View view;
    private EditText et_i,et_s,et_e;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_option, container,false);
        et_i  = (EditText) view.findViewById(R.id.interval_text);
        et_s = (EditText) view.findViewById(R.id.start_text);
        et_e = (EditText) view.findViewById(R.id.end_text);
         initiateListeners(view);

        return view;
    }
    public void requestFocus(){

    }
    private void initiateListeners(final View rootView)
    {
        MainActivity mainActivity = (MainActivity)getActivity();
        btn_back = (ImageButton) rootView.findViewById(R.id.btn_back);
        cb_storage = (CheckBox) rootView.findViewById(R.id.cb_storage);
        btn_confirm = (Button)rootView.findViewById(R.id.btn_confirm);

        et_s.setText(mainActivity.start_clock);
        et_e.setText(mainActivity.end_clock);
        et_i.setText(""+mainActivity.interval);

        if(((MainActivity)getActivity()).getCloudStorage()==MainActivity.CLOUD)
            cb_storage.setChecked(true);
        else
            cb_storage.setChecked(false);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).changeFragment("main");

            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
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

    /**
     * Confirms and execute the chosen settings
     * @throws InterruptedException
     */
    public void confirm() throws InterruptedException
    {
        try
        {
            MainActivity mainActivity = (MainActivity)getActivity();
            if(!isEmpty(et_s)&&(!isEmpty(et_e)))
            {
               mainActivity.setClockTime(et_s.getText().toString(), et_e.getText().toString());
            }

            Toast.makeText(((MainActivity)getActivity()), "Confirmed", Toast.LENGTH_SHORT).show();

            if(!isEmpty(et_i))
            {
                int interval = Integer.parseInt(et_i.getText().toString());
                mainActivity.setTime(interval);
            }
            else
                throw new Exception();

            if(cb_storage.isChecked())
                mainActivity.setCloudStorage(true);
            else
                mainActivity.setCloudStorage(false);

            mainActivity.onOff(false);
            mainActivity.changeFragment("main");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(((MainActivity) getActivity()), "Fill in all", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * Check if an option is empty
     * @param etText: The edit text that contains a setting.
     * @return True if options empty, false if option contain text.
     */
    private boolean isEmpty(EditText etText)
    {
        return etText.getText().toString().trim().length() == 0;
    }

}
