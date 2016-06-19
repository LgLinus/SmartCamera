package com.example.lenovo.smartcamera;

import android.util.Log;

import java.io.File;

/**
 * Class used to store various data regarding details about the file to be uploaded
 * Created by Linus on 2016-03-11.
 */
public class FileDetails
{
    private String file_name;
    private File file;
    private String path;
    private String label;
    /**
     * Create the object with given name
     * @param file file to upload
     * @param path path to the file in drive
     * @param name name of file to upload
     * @param label tag for the image
     */
    public FileDetails(File file, String path, String name, String label)
    {
        this.file = file;
        this.path = path;
        this.label = label;
        // Make sure the file always have a name
        if (name.equals(""))
            this.file_name = "image";
        else
            this.file_name = name;
    }
    // Get methods below
    public String getFile_name(){
        return this.file_name;
    }

    public File getFile(){
        return this.file;
    }

    public String getFullName(){
        return this.file_name;
    }

    public String getLabel(){
        return label;
    }

    public String getPath(){
        return path;
    }

}
