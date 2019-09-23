package samples.p01camera.p02face;

import helpers.AbstractRenderer;
import helpers.OpenCVImageFormat;
import lwjglutils.*;
import opencvutils.CameraGrabber;
import opencvutils.Grabber;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;

/**
 * Use built-in function for face detection. Show the rectangle using shader program.
 *
 * @author PGRF FIM UHK
 * @version 1.0
 * @since 2019-09-01
 */
public class Sample extends AbstractRenderer {

    private OGLBuffers buffers;
    private OGLTexture2D cameraImage;
    private Grabber cameraGrabber;
    private OpenCVImageFormat cameraImageFormat;

    private CascadeClassifier classifier;

    private int shaderProgram;
    private int locX, locY, locW, locH, locImageW, locImageH, locWinW, locWinH;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        cameraGrabber = new CameraGrabber();
        cameraImageFormat = new OpenCVImageFormat(3);

        shaderProgram = ShaderUtils.loadProgram("/samples/p01camera/p02face/draw");

        locX = glGetUniformLocation(shaderProgram, "faceX");
        locY = glGetUniformLocation(shaderProgram, "faceY");
        locW = glGetUniformLocation(shaderProgram, "faceW");
        locH = glGetUniformLocation(shaderProgram, "faceH");
        locImageW = glGetUniformLocation(shaderProgram, "image_w");
        locImageH = glGetUniformLocation(shaderProgram, "image_h");
        locWinW = glGetUniformLocation(shaderProgram, "window_w");
        locWinH = glGetUniformLocation(shaderProgram, "window_h");

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
        textRenderer = new OGLTextRenderer(width, height);

        classifier = new CascadeClassifier();
        classifier.load("D:\\Java\\lib\\opencv-411\\build\\etc\\haarcascades\\haarcascade_frontalface_alt.xml");
    }

    @Override
    public void display() {
        glUseProgram(shaderProgram);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, width, height);

        // X flip must be done in shader because detection does not work with flipped face (flipped from the OpenCV point of view)
        ByteBuffer buffer = cameraGrabber.grabImage();
        if (cameraImage == null) {
            cameraImage = new OGLTexture2D(cameraGrabber.getWidth(), cameraGrabber.getHeight(), cameraImageFormat, buffer);
        } else {
            cameraImage.setTextureBuffer(cameraImageFormat, buffer);
        }
        cameraImage.bind(shaderProgram, "texture", 0);

        MatOfRect matOfRect = new MatOfRect();
        classifier.detectMultiScale(cameraGrabber.grabImageRaw(), matOfRect);
        Rect[] rects = matOfRect.toArray();
        for (Rect rect : rects) {
            glUniform1i(locX, rect.x);
            glUniform1i(locY, rect.y);
            glUniform1i(locW, rect.width);
            glUniform1i(locH, rect.height);

            glUniform1i(locImageW, cameraGrabber.getWidth());
            glUniform1i(locImageH, cameraGrabber.getHeight());

            glUniform1i(locWinW, width);
            glUniform1i(locWinH, height);
        }
        if (rects.length == 0) {
            glUniform1i(locX, 0);
            glUniform1i(locY, 0);
            glUniform1i(locW, 0);
            glUniform1i(locH, 0);
        }

        buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

        String text;
        if (rects.length == 0) text = "No faces detected";
        else if (rects.length < 2) text = "Detected 1 face";
        else text = "Detected " + rects.length + " faces";

        textRenderer.clear();
        textRenderer.addStr2D(20, 20, text);
        textRenderer.addStr2D(width - 90, height - 3, " (c) PGRF UHK");
        textRenderer.draw();
    }

    @Override
    public GLFWCursorPosCallback getCursorPosCallback() {
        return null;
    }

}
