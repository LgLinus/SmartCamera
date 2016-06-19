package com.example.lenovo.smartcamera;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * MainActivity
 */
public class MainActivity extends Cloud {

    //    ---.---  Fragment ---.--- //
    private FragmentManager fragment_Manager;
    private FragmentTransaction fragment_Transaction;

    private OptionsFragment option_Fragment;
    private MainFragment main_Fragment;

    // ---.--- Triggers ---.---
    //Timer interval
    private int second = 1000;
    private int start_time;
    private int interval;
    private Thread timer_thread = null;

    //On/Off
    private boolean on = false;

    //Clock time;
    private String start_clock, end_clock;

    /**
     * Start the app and create a Options and Main fragment objects
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Create the fragments
        option_Fragment = new OptionsFragment();
        main_Fragment = new MainFragment();
        fragment_Manager = getFragmentManager();
        fragment_Transaction = fragment_Manager.beginTransaction();
        //Load the mainfragment to the display
        fragment_Transaction.replace(R.id.fragment_container, main_Fragment);
        fragment_Transaction.commit();

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD); // Unlock the device if locked
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); // Turn screen on if off
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on
    }
// -------------------------------------- Fragment ---------------------------------------------------
    /**
     * This method changes between the fragments
     * @param id = the name of the fragment that will be displayed
     */
    public void changeFragment(String id)
    {
        Log.d("MAINACTIVITY","chane fragment");
        if(id.equals("option"))
        {
            //Makes sure that no threads are running while changing fragment
            if(timer_thread!=null)
            {
                timer_thread.interrupt();
                timer_thread = null;
            }
            fragment_Manager = getFragmentManager();
            option_Fragment = new OptionsFragment();
            fragment_Transaction = fragment_Manager.beginTransaction();
            fragment_Transaction.replace(R.id.fragment_container, option_Fragment);
            fragment_Transaction.commit();
            option_Fragment.requestFocus();
        }
        else if(id.equals("main"))
        {
            fragment_Manager = getFragmentManager();
            fragment_Transaction = fragment_Manager.beginTransaction();
            fragment_Transaction.replace(R.id.fragment_container, main_Fragment);
            fragment_Transaction.commit();
        }

    }
//-----------------------------  Triggers ---------------------------
    /**
     * This method return if the value if the application is on or off
     * @return: True if on and false if off
     */
    public boolean getStatus()
    {
        return on;
    }
    /**
     * This method change the variable on to false or true
     */
    public void onOff(boolean b)
    {
        on = b;
    }
    /**
     * This method check if the current time is in between the setClocktime period
     * @return: True if it's time to take pictures, false if the picture time have passed.
     */
    public boolean isItTime()
    {
        //No time have been set
        if(start_clock.equals(null) && end_clock.equals(null) )
            return true;
        else
        {
            //Get the time
            Calendar cal = Calendar.getInstance();
            //Change format to european time (24:00)
            SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
            String current_time = sdf.format(cal.getTime());
            try
            {
                Date start = sdf.parse(start_clock);
                Date end = sdf.parse(end_clock);
                Date current = sdf.parse(current_time);
                //If current_time is higher then the start time and lower then the end time, then taking photo is ok.
                if((current.after(start) && current.before(end)) || (current.equals(start)||current.equals(end)))
                    return true;
            }
            catch(ParseException e){}
            //It is not time to take photo
            return false;
        }
    }
    /**
     * This method sets the time when the camera should take pictures
     * @param start: The start time picture will start be taken
     * @param end: The end time picture will no longer be taken ones passed end time
     * @throws ParseException
     */
    public void setClockTime(String start, String end) throws ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
        start_clock = sdf.format(sdf.parse(start));
        end_clock = sdf.format(sdf.parse(end));
    }
    /**
     * This method sets the time how often the camera shall save pictures
     * @param time_interval: how often the camera will take an image
     * @throws InterruptedException
     */
    public void setTime(int time_interval) throws InterruptedException
    {
        start_time = time_interval;
        interval = time_interval;
        timer_thread = new Thread()
        {
            boolean run = true;
            public void run()
            {
                while(run)
                {
                    //Wait until the interval time has passed
                    while (interval > 0 && run)
                    {
                        //Decreases the interval time by one second
                        try
                        {
                            Thread.sleep(second);
                            interval -= 1;
                            Log.d("init", "Count: " + interval);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                            run = false;
                        }
                    }
                    //Reset the interval timer
                    interval = start_time;
                    if(run)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(isItTime()&&getStatus())
                                {
                                    main_Fragment.saveImage();
                                    Toast.makeText(MainActivity.this, "Take Photo", Toast.LENGTH_SHORT).show();
                                }
                                else
                                    Toast.makeText(MainActivity.this,"Not time to take",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        };
        timer_thread.start();
        //Starts the buffer thread
        main_Fragment.setTimerMedianfilter(time_interval);
    }
}
