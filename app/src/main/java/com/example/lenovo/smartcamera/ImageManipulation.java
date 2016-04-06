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
public class ImageManipulation {


    /**
     * Method used to apply filter
     * @param matrix
     * @return new matrix
     */
    public static Mat applyFilter(Mat matrix){
        return matrix;
    }

    public static void MotionDetection(Mat older_frame, Mat old_frame,Mat fg, Mat bg, Mat output_frame, int kernel_size){
        fg = older_frame.clone();
        bg = old_frame.clone();
        Imgproc.cvtColor(older_frame, fg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(old_frame, bg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(fg, fg, new Size(kernel_size + 1, kernel_size + 1), 0);
        Imgproc.GaussianBlur(bg, bg, new Size(kernel_size + 1, kernel_size + 1), 0);

        Core.absdiff(fg, bg, output_frame);
        Imgproc.threshold(output_frame, output_frame, 0, 255, Imgproc.THRESH_BINARY);


    }

    public static void useAbsDiff(Mat source, Mat source2, Mat dst){

        for(int i = 0; i < source.width();i++){
            for(int j = 0; j < source.height();j++){
                int diff = 0;
                double[] first = source.get(j,i);
                double[] second = source2.get(j,i);
                int sum = Math.abs((int)(first[0]-second[0]));
                dst.put(j,i,sum);

            }
        }

        Imgproc.threshold(dst, dst, 40, 255, Imgproc.THRESH_BINARY);

       // Imgproc.threshold(dst, dst, 0, 255, Imgproc.THRESH_BINARY);

        // Imgproc.threshold(output_frame, output_frame, 100, 255, Imgproc.THRESH_BINARY);
    }

    /**
     * Function taking gray scaled images as parameters and returns a new image based on the median
     * values of each pixel. Each image must be the same size.
     * @param buffert
     * @return median valued image
     */
    public static Mat acquireMedian(Mat[] buffert){
        Log.d("IMAGEMANIPULATION","length of buffert: " + buffert.length);

        Mat mat = new Mat(buffert[0].height(),buffert[0].width(), CvType.CV_8U);

        int l = 0;
        for(int i = 0; i < buffert[0].width();i++){

            for(int j = 0; j  < buffert[0].height();j++){
                int sum = 0;

                for(int k = 0; k < buffert.length;k++){
                    double[] value = buffert[k].get(j,i);
                    sum+=value[0];
                }
                //l++;
                //l%=2;
                 //double[] value = {l*255};
                 double[] value = {((sum)/(double)buffert.length)};
                 mat.put(j,i,value);
            }

        }
        Log.d("Image size IMNIPULATION", "width: 2nd");

        return mat;
    }

    /**
     * Funciton that returns a new resized image
     * @param factorWidth factor reduce width
     * @param factorHeight factor reduce height
     * @param matrix original matrix
     * @return new resized matrix
     */
    public static Mat resizeImage(int factorWidth, int factorHeight, Mat matrix){
        Mat resizeimage = new Mat(matrix.width(),matrix.height(),CvType.CV_8UC1);
        Size sz = new Size(matrix.width()/factorWidth,matrix.height()/factorHeight);
        Log.d("IMAGEMANIPULATION","Original width: " + matrix.width() + "\theight:"+matrix.height()+"\nResized image width: " + sz.width + "\theight: "+ sz.height);
        Imgproc.resize(matrix, resizeimage, sz);
        return resizeimage;
    }

    /**
     * Function used to resize all the images in matrixes to a given size by using the parameters
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
    public static double whiteBlackRatio(Mat mat){
        Mat m = new Mat();
        Core.extractChannel(mat,m,0);
        int ctr = Core.countNonZero(m);
        m.release();
        return (double)ctr/(mat.width()*mat.height());

    }
}
