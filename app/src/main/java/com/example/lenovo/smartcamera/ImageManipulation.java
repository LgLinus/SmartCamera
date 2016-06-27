package com.example.lenovo.smartcamera;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * CLass used to pre-process images
 * Created by Linus on 2016-03-14.
 */
public class ImageManipulation
{

    /**
     * @param matrix The image that shall be resized
     * @return new resized matrix
     */
    public static Mat resizeImage_Size(int width, int height, Mat matrix)
    {
        Mat resizedimage = new Mat(matrix.width(),matrix.height(),CvType.CV_8UC1);
        Size sz = new Size(width,height);
        Imgproc.resize(matrix, resizedimage, sz);
       // matrix.release();
        return resizedimage;
    }

}
