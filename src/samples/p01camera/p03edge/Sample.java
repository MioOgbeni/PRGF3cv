package samples.p01camera.p03edge;

import helpers.AbstractRenderer;
import helpers.OpenCVImageFormat;
import lwjglutils.*;
import opencvutils.CameraGrabber;
import opencvutils.FlipCode;
import opencvutils.Grabber;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL20.*;

/**
 * Detect edges using Roberts Cross Edge Detector.
 * http://homepages.inf.ed.ac.uk/rbf/HIPR2/roberts.htm
 *
 * @author PGRF FIM UHK
 * @version 1.0
 * @since 2019-09-01
 */
public class Sample extends AbstractRenderer {

    private OGLBuffers buffers;
    private OGLTexture2D.Viewer textureViewer;
    private OGLTexture2D cameraImage, openCVDetectorTexture;
    private Grabber cameraGrabber;
    private OpenCVImageFormat cameraImageFormat;

    private int shaderProgram;
    private Mat matSrc, matGrey, matFinal;
    private int locImageW, locImageH;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        cameraGrabber = new CameraGrabber();
        matSrc = new Mat();
        matGrey = new Mat();
        matFinal = new Mat();

//        try {
//            image = new OGLTexture2D("ggb.jpg");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        cameraImageFormat = new OpenCVImageFormat(3);

        shaderProgram = ShaderUtils.loadProgram("/samples/p01camera/p03edge/draw");
        locImageW = glGetUniformLocation(shaderProgram, "image_width");
        locImageH = glGetUniformLocation(shaderProgram, "image_height");

        float[] vertexBufferData = {
                -1, 1, 0, 0,
                1, 1, 1, 0,
                -1, -1, 0, 1,
                1, -1, 1, 1
        };
        int[] indexBufferData = {0, 1, 2, 3};

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2, 0),
                new OGLBuffers.Attrib("inTexCoord", 2, 2)
        };
        buffers = new OGLBuffers(vertexBufferData, 4, attributes, indexBufferData);

        textureViewer = new OGLTexture2D.Viewer();
        textRenderer = new OGLTextRenderer(width, height);
    }

    @Override
    public void display() {
        edgeDetectorCustom();
        edgeDetectorOpenCV();
    }

    private void edgeDetectorCustom() {
        glUseProgram(shaderProgram);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, width, height);

        ByteBuffer buffer = cameraGrabber.grabImage();
        if (cameraImage == null) {
            cameraImage = new OGLTexture2D(cameraGrabber.getWidth(), cameraGrabber.getHeight(), cameraImageFormat, buffer);
        } else {
            cameraImage.setTextureBuffer(cameraImageFormat, buffer);
        }
        cameraImage.bind(shaderProgram, "texture", 0);

        glUniform1i(locImageW, cameraImage.getWidth());
        glUniform1i(locImageH, cameraImage.getHeight());

        buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

        textureViewer.view(cameraImage, -1, 0, 0.5);

        textRenderer.clear();
        textRenderer.addStr2D(width - 90, height - 3, " (c) PGRF UHK");
        textRenderer.draw();
    }

    private void edgeDetectorOpenCV() {
        // https://docs.opencv.org/4.1.1/d2/d2c/tutorial_sobel_derivatives.html
        matSrc = cameraGrabber.grabImageRaw(FlipCode.Y_AXIS);

        // convert the image to grayscale
        Imgproc.cvtColor(matSrc, matGrey, Imgproc.COLOR_RGB2GRAY);

        Mat gradX = new Mat(), gradY = new Mat();
        Mat absGradX = new Mat(), absGradY = new Mat();
        Mat gradient = new Mat();

        Imgproc.Sobel(matGrey, gradX, CvType.CV_16S, 1, 0, 3, 1, 0, Core.BORDER_DEFAULT);
        Imgproc.Sobel(matGrey, gradY, CvType.CV_16S, 0, 1, 3, 1, 0, Core.BORDER_DEFAULT);

        // converting back to CV_8U
        Core.convertScaleAbs(gradX, absGradX);
        Core.convertScaleAbs(gradY, absGradY);
        Core.addWeighted(absGradX, 0.5, absGradY, 0.5, 0, gradient);

        // convert back to RGB
        Imgproc.cvtColor(gradient, matSrc, Imgproc.COLOR_GRAY2RGB);

        // flip explanation: https://docs.opencv.org/4.1.1/d2/de8/group__core__array.html#gaca7be533e3dac7feb70fc60635adf441
        Core.flip(matSrc, matFinal, FlipCode.Y_AXIS); // flip around Y

        ByteBuffer buffer = Grabber.mat2Buffer(matFinal);
        if (openCVDetectorTexture == null) {
            openCVDetectorTexture = new OGLTexture2D(matFinal.cols(), matFinal.rows(), GL_RGB, GL_RGB, GL_UNSIGNED_BYTE, buffer);
        } else {
            openCVDetectorTexture.setTextureBuffer(cameraImageFormat, buffer);
        }

        textureViewer.view(openCVDetectorTexture, -1, -1, 1);
    }

    @Override
    public GLFWCursorPosCallback getCursorPosCallback() {
        return null;
    }

}
