package com.example.lenovo.smartcamera;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;


/**
 * MainActivity
 */
public class MainActivity extends Cloud {
     //    ---.---  Fragment ---.--- //

    private FragmentManager fragment_Manager;
    private FragmentTransaction fragment_Transaction;

    private MainFragment main_Fragment;

     @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Create the fragments
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
    }

    @Override
    protected void onPause(){
        super.onPause();
        this.main_Fragment.releaseCamera();
    }
//-----------------------------  Triggers ---------------------------

}