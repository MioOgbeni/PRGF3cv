package samples.p01camera.p03edge;

import opencvutils.CameraGrabber;
import opencvutils.Grabber;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

/**
 * https://docs.opencv.org/4.1.1/d2/d2c/tutorial_sobel_derivatives.html
 */
public class EdgeOpenCV {

    public static void main(String[] args) {

        Grabber cameraGrabber = new CameraGrabber();

        while (true) {
            Mat mat = cameraGrabber.grabImageRaw();
//            Mat mat = Imgcodecs.imread("./res/ggb.jpg");
            Mat matGrey = new Mat();

            // Convert the image to grayscale
            Imgproc.cvtColor(mat, matGrey, Imgproc.COLOR_RGB2GRAY);

            Mat gradX = new Mat(), gradY = new Mat();
            Mat absGradX = new Mat(), absGradY = new Mat();
            Mat gradient = new Mat();

            Imgproc.Sobel(matGrey, gradX, CvType.CV_16S, 1, 0, 3, 1, 0, Core.BORDER_DEFAULT);
            Imgproc.Sobel(matGrey, gradY, CvType.CV_16S, 0, 1, 3, 1, 0, Core.BORDER_DEFAULT);

            // converting back to CV_8U
            Core.convertScaleAbs(gradX, absGradX);
            Core.convertScaleAbs(gradY, absGradY);
            Core.addWeighted(absGradX, 0.5, absGradY, 0.5, 0, gradient);

            String windowName = "Sobel Demo - Simple Edge Detector";
            HighGui.imshow(windowName, gradient);
            HighGui.waitKey(16);
        }

    }
}
