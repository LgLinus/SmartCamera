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
    private EditText et_i,et_s,et_e;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_option, container,false);
        initiateListeners(view);
        et_i  = (EditText) view.findViewById(R.id.interval_text);
        et_s = (EditText) view.findViewById(R.id.start_text);
        et_e = (EditText) view.findViewById(R.id.end_text);
        et_i.clearFocus();
        et_s.clearFocus();
        et_e.clearFocus();

        et_i.setSelectAllOnFocus(true);
        et_i.requestFocus();

        return view;
    }
    public void mainMenu(View view)
    {
        ((MainActivity)getActivity()).changeFragment("main");
    }

    public void requestFocus(){

    }

    @Override
    public void onAttach(Activity bundle){
        super.onAttach(bundle);
        if(et_i==null||view==null||getActivity()==null)
            return;
        Context context = getActivity().getApplicationContext();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        et_i.requestFocus();
        InputMethodManager imm = (InputMethodManager) (getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.hideSoftInputFromWindow(et_i.getWindowToken(), InputMethodManager.SHOW_IMPLICIT);
        imm.showSoftInput(et_i, InputMethodManager.SHOW_IMPLICIT);

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
            if(!btn_Switch.isChecked()){
                ((MainActivity)getActivity()).onOff(false);
                ((MainActivity) getActivity()).changeFragment("main");
                return;
            }

             if(!isEmpty(et_s)&&(!isEmpty(et_e)))
            {
                ((MainActivity) getActivity()).setClockTime(et_s.getText().toString(), et_e.getText().toString());
            }

            Toast.makeText(((MainActivity) getActivity()), "Confirmed", Toast.LENGTH_SHORT).show();

            ((MainActivity) getActivity()).onOff(true);
                if(!isEmpty(et_i))
                {
                    interval = Integer.parseInt(et_i.getText().toString());
                    ((MainActivity) getActivity()).setTime(interval);
                }

            ((MainActivity) getActivity()).onOff(true);
            ((MainActivity) getActivity()).changeFragment("main");

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
