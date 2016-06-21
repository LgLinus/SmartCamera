package com.example.lenovo.smartcamera;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * CLass used to pre-process images
 * Created by Linus & Andreas  on 2016-03-14.
 */
public class ImageManipulation
{

    /**
     * Funciton that returns a new resized image
     * @param factorWidth factor reduce width
     * @param factorHeight factor reduce height
     * @param matrix The image that shall be resized
     * @return new resized matrix
     */
    public static Mat resizeImage(int factorWidth, int factorHeight, Mat matrix)
    {
        Mat resizeimage = new Mat(matrix.width(),matrix.height(),CvType.CV_8UC1);
        Size sz = new Size(matrix.width()/factorWidth,matrix.height()/factorHeight);
        Log.d("IMAGEMANIPULATION","Original width: " + matrix.width() + "\theight:"+matrix.height()+"\nResized image width: " + sz.width + "\theight: "+ sz.height);
        Imgproc.resize(matrix, resizeimage, sz);
        return resizeimage;
    }

    /**
     * @param matrix The image that shall be resized
     * @return new resized matrix
     */
    public static Mat resizeImage_Size(int width, int height, Mat matrix)
    {
        Mat resizeimage = new Mat(matrix.width(),matrix.height(),CvType.CV_8UC1);
        Size sz = new Size(width,height);
        Log.d("IMAGEMANIPULATION","Original width: " + matrix.width() + "\theight:"+matrix.height()+"\nResized image width: " + sz.width + "\theight: "+ sz.height);
        Imgproc.resize(matrix, resizeimage, sz);
        matrix.release();
        return resizeimage;
    }

}
