package samples.p01camera.p01basic;

import helpers.AbstractRenderer;
import helpers.OpenCVImageFormat;
import lwjglutils.*;
import opencvutils.CameraGrabber;
import opencvutils.Grabber;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Sample showing basic camera grabbing.
 *
 * @author PGRF FIM UHK
 * @version 1.0
 * @since 2019-09-01
 */
public class Sample extends AbstractRenderer {

    private OGLBuffers buffers;
    private OGLTexture2D.Viewer textureViewer;
    private OGLTexture2D cameraImage;
    private Grabber cameraGrabber;
    private OpenCVImageFormat cameraImageFormat;

    private int shaderProgram, locMode, mode;
    private int cameraWidth, cameraHeight;

    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                if (key == GLFW_KEY_M) {
                    mode++;
                    mode %= 2;
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

        cameraImageFormat = new OpenCVImageFormat(3);
        cameraGrabber = new CameraGrabber();
        cameraGrabber.grabImageRaw();

        cameraWidth = cameraGrabber.getWidth();
        cameraHeight = cameraGrabber.getHeight();
        System.out.println("camera width " + cameraWidth + " ; camera height " + cameraHeight);

        cameraImage = new OGLTexture2D(cameraWidth, cameraHeight, cameraImageFormat, null);

        shaderProgram = ShaderUtils.loadProgram("/samples/p01camera/p01basic/draw");

        mode = 1;
        locMode = glGetUniformLocation(shaderProgram, "mode");

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
        glUseProgram(shaderProgram);
        glViewport(0, 0, width, height);

        ByteBuffer buffer = cameraGrabber.grabImage();
        cameraImage.setTextureBuffer(cameraImageFormat, buffer);
        cameraImage.bind(shaderProgram, "texture", 0);

        glUniform1i(locMode, mode);

        buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

        textureViewer.view(cameraImage);

        // create and draw text
        String text = "Image size: " + cameraWidth + " X " + cameraHeight + " FPS: " + cameraGrabber.getFPS() + " [M]ode";
        textRenderer.clear();
        textRenderer.addStr2D(3, 20, text);
        textRenderer.addStr2D(width - 90, height - 3, " (c) PGRF UHK");
        textRenderer.draw();
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    @Override
    public GLFWCursorPosCallback getCursorPosCallback() {
        return null;
    }

    @Override
    public GLFWScrollCallback getScrollCallback() {
        return null;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return null;
    }
}
