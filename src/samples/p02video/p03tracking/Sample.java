package samples.p02video.p03tracking;

import helpers.AbstractRenderer;
import helpers.OpenCVImageFormat;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture2D;
import lwjglutils.OGLUtils;
import opencvutils.Grabber;
import opencvutils.VideoGrabber;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeDetector;

import java.awt.*;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Use OpenCV function to detect and decode QR codes in input image.
 *
 * @author PGRF FIM UHK
 * @version 1.0
 * @since 2019-09-01
 */
public class Sample extends AbstractRenderer {

    private OGLTexture2D.Viewer textureViewer;
    private OGLTexture2D videoImage;
    private VideoGrabber videoGrabber;
    private OpenCVImageFormat videoImageFormat;
    private QRCodeDetector qrCodeDetector;

    private Mat matSrc, matGrey;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        videoGrabber = new VideoGrabber("./res/Hello_world_!.mp4");
//        matSrc = Imgcodecs.imread("./res/qr.png"); // "hello"
        videoImageFormat = new OpenCVImageFormat(3);
        matSrc = new Mat();
        matGrey = new Mat();
        qrCodeDetector = new QRCodeDetector();
        textureViewer = new OGLTexture2D.Viewer();
        textRenderer = new OGLTextRenderer(width, height);
    }

    @Override
    public void display() {
        glViewport(0, 0, width, height);
        matSrc = videoGrabber.grabImageRaw();
        if (matSrc == null) {
            videoGrabber.rewind();
            matSrc = videoGrabber.grabImageRaw();
        }

        Imgproc.cvtColor(matSrc, matGrey, Imgproc.COLOR_RGB2GRAY);

        // https://docs.opencv.org/4.1.1/de/dc3/classcv_1_1QRCodeDetector.html
        String output = qrCodeDetector.detectAndDecode(matGrey);
//        System.out.println(output);

        ByteBuffer buffer = Grabber.mat2Buffer(matSrc);
        if (videoImage == null) {
            videoImage = new OGLTexture2D(videoGrabber.getWidth(), videoGrabber.getHeight(), videoImageFormat, buffer);
        } else {
            videoImage.setTextureBuffer(videoImageFormat, buffer);
        }
        textureViewer.view(videoImage, -1, -1, 2);

        textRenderer.clear();
        textRenderer.setColor(Color.BLUE);
        textRenderer.addStr2D(10, 25, output);
        textRenderer.setColor(Color.WHITE);
        textRenderer.addStr2D(width - 90, height - 3, " (c) PGRF UHK");
        textRenderer.draw();
    }

    @Override
    public GLFWCursorPosCallback getCursorPosCallback() {
        return null;
    }

}
