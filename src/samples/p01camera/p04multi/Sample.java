package samples.p01camera.p04multi;

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
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Sample using more cameras in one scene.
 *
 * @author PGRF FIM UHK
 * @version 1.0
 * @since 2019-09-01
 */
public class Sample extends AbstractRenderer {

    private OGLBuffers buffers;
    private OGLTexture2D.Viewer textureViewer;
    private OGLTexture2D image0, image1;
    private Grabber cameraGrabber0, cameraGrabber1;
    private OpenCVImageFormat cameraImageFormat;

    private int shaderProgram;
    private int locMode, mode;

    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                // We will detect this in our rendering loop
                glfwSetWindowShouldClose(window, true);
            if (action == GLFW_RELEASE) {
                System.out.println("Key release " + key);
            }
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                if (key == GLFW_KEY_M) {
                    mode++;
                    mode %= 2;
                }
            }
        }
    };

    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        cameraGrabber0 = new CameraGrabber(0);
        cameraGrabber1 = new CameraGrabber(1);
        cameraImageFormat = new OpenCVImageFormat(3);

        shaderProgram = ShaderUtils.loadProgram("/samples/p01camera/p04multi/draw");

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

    public void display() {
        glViewport(0, 0, width, height);

        pass++;
        // set the clear color
        glClearColor((float) (Math.sin(pass / 100.) / 2 + 0.5),
                (float) (Math.cos(pass / 200.) / 2 + 0.5),
                (float) (Math.sin(pass / 300.) / 2 + 0.5), 0.0f);
        // clear the framebuffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(shaderProgram);

        System.out.println(mode);
        glUniform1i(locMode, mode);

        ByteBuffer buffer0 = cameraGrabber0.grabImage();//.order(ByteOrder.LITTLE_ENDIAN);
        if (image0 == null) {
            image0 = new OGLTexture2D(cameraGrabber0.getWidth(), cameraGrabber0.getHeight(), cameraImageFormat, buffer0);
        } else {
            image0.setTextureBuffer(cameraImageFormat, buffer0);
        }

        ByteBuffer buffer1 = cameraGrabber1.grabImage();//.order(ByteOrder.LITTLE_ENDIAN);
        if (image1 == null) {
            image1 = new OGLTexture2D(cameraGrabber1.getWidth(), cameraGrabber1.getHeight(), cameraImageFormat, buffer1);
        } else {
            image1.setTextureBuffer(cameraImageFormat, buffer1);
        }

        image0.bind(shaderProgram, "texture", 0);

        glUniform1i(locMode, mode);

        buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

        textureViewer.view(image0, -1, -1, 0.5);
        textureViewer.view(image1, -1, 0, 0.5);

        textRenderer.clear();
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