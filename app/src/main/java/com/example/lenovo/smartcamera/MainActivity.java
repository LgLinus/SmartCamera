package com.example.lenovo.smartcamera;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
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
    //    ---.---  Camera variables ---.--- //
    // Used to display footage
    //CameraBridgeViewBase camera_view;

    private final String PREFS_NAME ="myPrefs";
    private final String KEY_CHOICE = "choice";
    private final String KEY_INTERVAL = "interval";
    private final String KEY_START_TIME = "start_time";
    private final String KEY_END_TIME = "end_time";

    public static final int CLOUD = 1;
    public static final int LOCAL = 0;
    //    ---.---  Fragment ---.--- //

    private Fragment fragment;
    private FragmentManager fragment_Manager;
    private FragmentTransaction fragment_Transaction;

    private OptionsFragment option_Fragment;
    private MainFragment main_Fragment;

    // ---.--- Triggers ---.---
    //Timer interval
    private int second = 1000;
    private int start_time;
    public int interval;
    private Thread timer_thread = null;

    //On/Off
    private boolean on = false;

    //Clock time
    private boolean itIsTime = false;
    public String start_clock, end_clock;
    private Date start, end;

    public static int choice = -1;
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
        //Load the menufragment to the display
        fragment_Transaction.replace(R.id.fragment_container, main_Fragment);
        fragment_Transaction.commit();

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD); // Unlock the device if locked
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); // Turn screen on if off
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on

        setupPrefs();

    }
    private void setupPrefs(){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        this.end_clock = prefs.getString(this.KEY_END_TIME,"23:59");
        this.start_clock = prefs.getString(this.KEY_START_TIME,"00:00");
        this.interval = prefs.getInt(this.KEY_INTERVAL,5);
        checkStorageType(prefs);
    }
    /**
     * Checks if the user have chosen a storage type, 0 = Local, 1 = cloud
     */
    private void checkStorageType(SharedPreferences prefs){

        int choice = prefs.getInt(KEY_CHOICE,-1);
        this.choice = choice;
        Log.d("MAINACTIVITY", "CHOICE: " + choice);
        // If the user havn't made a choice before, force him to do it
        if(choice == -1){
            showChoice(prefs);
        }
    }

    // Show the choice of storage type
    private void showChoice(SharedPreferences prefs){
        final SharedPreferences.Editor editor = prefs.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Would you like to store files locally or on the cloud?\nThis can be changed again in options");
        builder.setCancelable(false);

        builder.setPositiveButton("Local", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                editor.putInt(KEY_CHOICE, LOCAL);
                editor.commit();
                choice = LOCAL;
                dialog.cancel();

                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        builder.setNegativeButton("Cloud", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                editor.putInt(KEY_CHOICE, CLOUD);
                editor.commit();
                choice = CLOUD;
                dialog.cancel();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        builder.create().show();
    }

// xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx Fragment xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    /**
     * This method changes between the fragments menu
     * @param id = the name of the fragment that will be displayed
     */
    public void changeFragment(String id)
    {
        Log.d("MAINACTIVITY","chane fragment");
        if(id.equals("option"))
        {
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
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();

            edit.putString(this.KEY_START_TIME,this.start_clock);
            edit.putString(this.KEY_END_TIME,this.end_clock);
            edit.putInt(this.KEY_INTERVAL,this.interval);
            edit.commit();
            fragment_Manager = getFragmentManager();
            fragment_Transaction = fragment_Manager.beginTransaction();
            fragment_Transaction.replace(R.id.fragment_container, main_Fragment);
            fragment_Transaction.commit();
        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        this.main_Fragment.releaseCamera();
    }
//-----------------------------  Triggers ---------------------------

    /**
     * This method return if the value of the onOff switch
     * @return: True if on and false if off
     */
    public boolean getStatus()
    {
        return on;
    }
    /**
     * This method is like an on/off switch, which changes the variable "on" to false/true
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
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
            String current_time = sdf.format(cal.getTime());
            try{
                Date start = sdf.parse(start_clock);
                Date end = sdf.parse(end_clock);
                Date current = sdf.parse(current_time);
                //If current_time is higher then the start time and lower then the end time, then taking photo is ok.
                if((current.after(start) && current.before(end))
                        ||(current.equals(start)||current.equals(end)))
                    return true;}
            catch(ParseException e){

            }

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
     * @param time: The intervall time
     * @throws InterruptedException
     */
    public void setTime(int time) throws InterruptedException
    {
        start_time = time;
        interval = time;
        Log.d("MainActivity","made it: 0");
        timer_thread = new Thread()
        {
            boolean run = true;

            public void run()
            {
                Log.d("MainActivity","made it: 1");
                while(run)
                {
                    //Sleep until the interval time has passed
                    while (interval > 0 && run)
                    {
                        //Decreases the interval time by one second
                        try
                        {
                            Thread.sleep(second);
                            interval -= 1;
                            Log.d("init", "Count: " + interval);
                        } catch (InterruptedException e)
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


                                Calendar cal = Calendar.getInstance();
                                SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
                                String current_time = sdf.format(cal.getTime());
                                Log.d("MAINACTIVITY","Time start:" + start_clock + "\tTime end:" + end_clock+"\tCurrent_time: "+current_time
                                        +"\nisTrue:"+isItTime());
                                if(isItTime()&&getStatus()){
                                    // main_Fragment.saveImageTetes();
                                    main_Fragment.saveImage();
                                    //main_Fragment.saveImageTestResize();
                                    Toast.makeText(MainActivity.this, "Take Photo", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(MainActivity.this,"Not time to take",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        };
        timer_thread.start();
        main_Fragment.setTimerMedianfilter(time);
        Log.d("MainActivity", "made it: 2");
    }

    /**
     * Method used to return a string given the given input
     * @param people_in_room, boolean value
     * @return "people" or "empty"
     */
    private String getPeopleInRoomText(boolean people_in_room)
    {
        if(people_in_room)
            return "people";
        else
            return "empty";
    }

    /**
     * Set storage type
     * @param cloud
     */
    public void setCloudStorage(boolean cloud){
        if(cloud == true){
            choice = CLOUD;
            Log.d("MAINACTIVITY","cloud");}
        else{
             choice = LOCAL;
        Log.d("MAINACTIVITY","local");}
    }

    /**
     * Retrieve storage type
     * @return cloud = 1,local = 0
     */
    public int getCloudStorage(){
        return this.choice;
    }
}