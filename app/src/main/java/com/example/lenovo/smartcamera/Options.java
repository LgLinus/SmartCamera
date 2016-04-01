package com.example.lenovo.smartcamera;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.WindowDecorActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.res.Configuration;
import android.widget.ImageView;

import java.util.zip.Inflater;


/**
 * Created by Andreas on 2016-03-30.
 */
public class Options extends Fragment
{
    //------ Setting variables ------
    private int interval;
    private boolean on = false;
    private boolean human;

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_menu, container, false);
        return view;
        //return inflater.inflate(R.layout.fragment_menu, container,false);
    }
    // Use in order to get view by id
    // Note: you can get the fragment view anywhere in the class by using getView()
    // once onCreateView() has been executed successfully. i.e.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.interval_text).setOnClickListener((View.OnClickListener) this);
        view.findViewById(R.id.interval_text).setOnClickListener((View.OnClickListener) this);

    }
    public void confirm(View v)
    {
        EditText et = (EditText) getView().findViewById(R.id.interval_text);
        interval = Integer.parseInt(et.getText().toString());
    }
    public void setInterval(int time)
    {

    }
}
