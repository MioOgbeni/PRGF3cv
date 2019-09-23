package samples.p04read_save;

import helpers.AbstractRenderer;
import helpers.LwjglWindow;
import helpers.OpenCVImageFormat;
import lwjglutils.*;
import opencvutils.CameraGrabber;
import opencvutils.FlipCode;
import opencvutils.Grabber;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

import java.awt.*;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * Read back rendered image into OpenCV mat object.
 * Save the mat objects as a sequence into a video file.
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
    private VideoWriter videoWriter;

    private int shaderProgram;
    private int counter = 0;
    private boolean saveNow = false, saved = false;

    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                if (key == GLFW_KEY_S) {
                    saveNow = true;
                }
            }
        }
    };

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        cameraGrabber = new CameraGrabber();
        cameraImageFormat = new OpenCVImageFormat(3);

        // https://docs.opencv.org/master/dd/d9e/classcv_1_1VideoWriter.html#ac3478f6257454209fa99249cc03a5c59
        // http://www.fourcc.org/codecs.php
        videoWriter = new VideoWriter(
                "test.mp4",
                VideoWriter.fourcc('F', 'M', 'P', '4'),
                20, // TODO
                new Size(LwjglWindow.WIDTH, LwjglWindow.HEIGHT)
        );

        shaderProgram = ShaderUtils.loadProgram("/samples/p04read_save/draw");

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
    }

    @Override
    public void display() {
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

        buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

        textRenderer.clear();
        textRenderer.setColor(Color.GRAY);
        textRenderer.addStr2D(width - 90, height - 3, " (c) PGRF UHK");
        textRenderer.draw();

        getResultToMat();
    }

    private void getResultToMat() {
        int width = LwjglWindow.WIDTH;
        int height = LwjglWindow.HEIGHT;

        ByteBuffer pixelBuffer = BufferUtils.createByteBuffer(width * height * 3);
        glReadPixels(0, 0, width, height, GL_BGR, GL_UNSIGNED_BYTE, pixelBuffer);

        byte[] pixelValues = new byte[pixelBuffer.remaining()];
        pixelBuffer.get(pixelValues);

        Mat image = new Mat(height, width, CvType.CV_8UC3);
        image.put(0, 0, pixelValues);
        Mat image2 = new Mat();
        Core.flip(image, image2, FlipCode.X_AXIS);

        if (saveNow && !saved) {
            videoWriter.release();
            System.out.println("saved");
            saved = true;
        } else if (!saved) {
            videoWriter.write(image2);
            System.out.println(counter++);
        }
    }

    @Override
    public GLFWCursorPosCallback getCursorPosCallback() {
        return null;
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

}
