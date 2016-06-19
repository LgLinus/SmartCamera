package com.example.lenovo.smartcamera;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class used to handle all of the communication to google drive
 *
 * Created by Linus on 2016-03-11.
 */
public class Cloud extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    private FileDetails fileDetails;
    private static String folder = "Test_folder_2";
    private static final String TAG = "Drive_class";
    private static DriveFolder parent_folder;


    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Called when activity gets visible. A connection to Drive services need to
     * be initiated as soon as the activity is visible. Registers
     * {@code ConnectionCallbacks} and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    /**
     * Handles conncetion callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when activity gets invisible. Connection to Drive service needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution is
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;

        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");

        DriveFolder parent = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        this.folder_exists(this.folder, parent);

    }

    /**
     * Creates the folder to store images in
     * @param folder_name
     * @param parent, folder to store the new folder in
     */
    public void createFolders(String folder_name, DriveFolder parent){
        MetadataChangeSet dataSet = new MetadataChangeSet.Builder().setTitle(folder_name).build();
        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, dataSet).setResultCallback(folderCreatedCallback);

    }

    /*
     * Functions which returns true if the given folder @folder_name exists in
     * parent folder
     * @param file_name
     * @param parent
     * @return
     */
    private void folder_exists(String folder_name, DriveFolder parent){

        Query query = new Query.Builder().build();
        parent.queryChildren(mGoogleApiClient,query).setResultCallback(childrenRetrievedCallback);

    }

    /* Callback used to handle the response from searching the folder */
    ResultCallback<DriveApi.MetadataBufferResult> childrenRetrievedCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Problem while retrieving files");
                        return;
                    }
                    if((parent_folder=checkFolder(result.getMetadataBuffer(),folder))!=null){
                        //howMessage("Found folder");
                    }
                    else{
                        //showMessage("Folder not found, creating it");
                        DriveFolder parent = Drive.DriveApi.getRootFolder(mGoogleApiClient);
                        createFolders(folder,parent);
                    }
                }
            };

    /**
     * Returns true if the given file name is found in the dataset
     * @param data to check through
     * @param folder_name
     * @return
     */
    private DriveFolder checkFolder(MetadataBuffer data, String folder_name){

        for(int i = 0; i < data.getCount();i++){
            Log.d("CLOUD","title of file:"+ data.get(i).getTitle() + "\n"+folder_name);
            if(folder_name.equals(data.get(i).getTitle())){
                return Drive.DriveApi.getFolder(mGoogleApiClient,data.get(i).getDriveId());
            }
        }
        return null;
    }
    /**
     * Called when {@code mGoogleApiClient} is disconnected.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Getter for the {@code GoogleApiClient}.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Uploads the given file to the drive
     *
     * @param file  to upload
     * @param name  filename
     * @param label description
     */
    public void uploadFile(File file, String path, String name, String label) {
        fileDetails = new FileDetails(file, path, name + ".png", label);
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);
    }

    /**
     * Called when creting new folder
     */
    ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new
            ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the folder");
                        return;
                    }
                    parent_folder = result.getDriveFolder();
                    showMessage("Created a folder: " + result.getDriveFolder().getDriveId());
                }
            };

    /*  Callback uesd to upload a file, called from method uploadFile() */
    final protected ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    // Perform I/O off the UI thread.
                    new Thread() {
                        @Override
                        public void run() {
                            // write content to DriveContents
                            OutputStream outputStream = driveContents.getOutputStream();

                            try {

                                FileInputStream is = new FileInputStream(fileDetails.getFile());
                                byte[] buf = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = is.read(buf)) > 0) {
                                    outputStream.write(buf, 0, bytesRead);
                                }

                                outputStream.close();
                                is.close();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }

                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle(fileDetails.getFullName())
                                    .setMimeType("image/jpg")
                                    .setDescription(fileDetails.getLabel())
                                    .setStarred(true).build();


                            // create a file in seperate folder if it exists
                            if(parent_folder!=null) {

                                parent_folder.createFile(getGoogleApiClient(), changeSet, driveContents)
                                        .setResultCallback(fileCallback);
                            }
                            else{

                                Drive.DriveApi.getRootFolder(getGoogleApiClient())
                                        .createFile(getGoogleApiClient(), changeSet, driveContents)
                                        .setResultCallback(fileCallback);
                            }
                        }
                    }.start();
                }
            };

    /* Callback used to handle file created responses*/
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the file");
                        return;
                    }
                    showMessage("Created a file with content: " + result.getDriveFile().getDriveId());
                }
            };


    /**
     * Funciton used to display a message
     * @param msg
     */
    private void showMessage(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
}
