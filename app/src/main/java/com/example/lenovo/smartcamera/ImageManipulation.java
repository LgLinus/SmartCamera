package com.example.lenovo.smartcamera;

import org.opencv.core.Core;
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
        Imgproc.threshold(output_frame, output_frame, 100, 255, Imgproc.THRESH_BINARY);


    }
}
