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
     * Calculate the absolute pixel difference between each pixel in two images (in order to detect movement)
     * @param average_image: The average calculated image from the buffer that represent the movement that have occurred under time_interval
     * @param current_image: The images taken afte time_interval
     * @param binary_image: The calculated abbsDiff image that will show the movement that has occurred.
     * @param binary_threshold: The theshold that decide if movement have occured
     */
    public static void useAbsDiff(Mat average_image, Mat current_image, Mat binary_image, int binary_threshold)
    {
        for (int i = 0; i < average_image.width(); i++) { // Go through each pixel in the image
            for (int j = 0; j < average_image.height(); j++) {
                double[] first = average_image.get(j, i); // Retrieve pixel values
                double[] second = current_image.get(j, i);
                int sum = Math.abs((int) (first[0] - second[0])); // Calculate absolute difference
                binary_image.put(j, i, sum); // Put difference value in the result image
            }
        }
        //Turn the images to black and white, where white is movement and black is none movment
        Imgproc.threshold(binary_image, binary_image, binary_threshold, 255, Imgproc.THRESH_BINARY);
    }
    /**
     * Calculate the average image of a buffer by adding all the images pixel in each pixel position together to one and divide it by the buffer size
     * Each image must be the same size.
     * @param buffert: The buffer containing all the images
     * @return average_image (median valued image)
     */
    public static Mat acquireMedian(Mat[] buffert)
    {
        Mat average_image = new Mat(buffert[0].height(),buffert[0].width(), CvType.CV_8U);
        int l = 0;
        // Go through each pixel in in each image
        for(int i = 0; i < buffert[0].width();i++)
            for(int j = 0; j  < buffert[0].height();j++)
            {
                int sum = 0;
                for(int k = 0; k < buffert.length;k++)
                {
                    double[] value = buffert[k].get(j,i); //the value need to be stored in an array
                    sum+=value[0];  //adds all the pixel values together at pixel position (i, j).
                }
                double[] average_value = {((sum)/(double)buffert.length)};
                average_image.put(j, i, average_value); // Put the calculated average pixel value in result image
            }
        return average_image;
    }
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
     * Function used to resize all the images in a buffer to a given size by using the parameters
     * factorwidth and factorheight
     * @param factorWidth factor to reduce with
     * @param factorHeight factor to reduce height
     * @param matrixes original images
     * @return new matrix with resized images
     */
    public static Mat[] resizeImage(int factorWidth, int factorHeight, Mat[] matrixes){
        Mat[] mats = new Mat[matrixes.length];
        for(int i = 0; i < matrixes.length;i++){
            mats[i] = resizeImage(factorWidth,factorHeight,matrixes[i]);
        }
        return mats;
    }
    /**
     * Function used to return the ratio of white pixels in the image
     * @param mat
     * @return ratio
     */
    public static double whiteBlackRatio(Mat mat)
    {
        Mat m = new Mat();
        Core.extractChannel(mat,m,0);
        int ctr = Core.countNonZero(m); // counts all the pixel that doesn't have the value 0
        m.release();
        return (double)ctr/(mat.width()*mat.height());
    }
}
