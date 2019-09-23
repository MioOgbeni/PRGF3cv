package samples.p02video.p02relief;

import helpers.AbstractRenderer;
import helpers.LwjglWindow;
import helpers.OpenCVImageFormat;
import lwjglutils.*;
import opencvutils.VideoGrabber;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import transforms.*;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Sample that uses video feed and its pixel colors to change coordinate of vertices in a grid.
 *
 * @author PGRF FIM UHK
 * @version 1.0
 * @since 2019-09-01
 */
public class Sample extends AbstractRenderer {

    private OGLBuffers buffers;
    private OGLTexture2D.Viewer textureViewer;
    private OGLTexture2D videoImage;
    private VideoGrabber videoGrabber;
    private OpenCVImageFormat videoImageFormat;

    private int shaderProgram, locModel, locView, locProjection;

    private Mat4 projection;
    private Camera camera;
    private double oldMx, oldMy;
    private boolean mousePressed;

    private GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mousePressed) {
                camera = camera.addAzimuth(Math.PI * (oldMx - x) / LwjglWindow.WIDTH);
                camera = camera.addZenith(Math.PI * (y - oldMy) / LwjglWindow.HEIGHT);
                oldMx = x;
                oldMy = y;
            }
        }
    };

    private GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(window, xPos, yPos);
                oldMx = xPos[0];
                oldMy = yPos[0];
                mousePressed = action == GLFW_PRESS;
            }
        }
    };

    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_A:
                        camera = camera.left(0.1);
                        break;
                    case GLFW_KEY_D:
                        camera = camera.right(0.1);
                        break;
                    case GLFW_KEY_W:
                        camera = camera.forward(0.1);
                        break;
                    case GLFW_KEY_S:
                        camera = camera.backward(0.1);
                        break;
                    case GLFW_KEY_R:
                        camera = camera.up(0.1);
                        break;
                    case GLFW_KEY_F:
                        camera = camera.down(0.1);
                        break;
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

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        shaderProgram = ShaderUtils.loadProgram("/samples/p02video/p02relief/draw");

        camera = new Camera()
                .withPosition(new Vec3D(0.6, 1.4, 1.7))
                .addAzimuth(1.5 * Math.PI)
                .addZenith(-0.25 * Math.PI);

        locModel = glGetUniformLocation(shaderProgram, "model");
        locView = glGetUniformLocation(shaderProgram, "view");
        locProjection = glGetUniformLocation(shaderProgram, "projection");

        double ratio = LwjglWindow.HEIGHT / (double) LwjglWindow.WIDTH;
        projection = new Mat4PerspRH(Math.PI / 3, ratio, 0.1, 200.0);

        videoGrabber = new VideoGrabber("./res/Hello_world_!.mp4");
        videoImageFormat = new OpenCVImageFormat(3);

        buffers = GridFactory.createBuffers(100, 100);

        textureViewer = new OGLTexture2D.Viewer();
        textRenderer = new OGLTextRenderer(width, height);
    }

    public void display() {
        glUseProgram(shaderProgram);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, width, height);

        glUniformMatrix4fv(locModel, false, new Mat4Transl(0, 0, 0).floatArray());
        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection, false, projection.floatArray());

        ByteBuffer videoBuffer = videoGrabber.grabImage();
        if (videoImage == null) {
            videoImage = new OGLTexture2D(videoGrabber.getWidth(), videoGrabber.getHeight(), videoImageFormat, videoBuffer);
        } else {
            if (videoBuffer == null) videoGrabber.rewind();
            else videoImage.setTextureBuffer(videoImageFormat, videoBuffer);
        }
        videoImage.bind(shaderProgram, "texture", 0);
        buffers.draw(GL_TRIANGLES, shaderProgram);

        textureViewer.view(videoImage, -1, -1, 0.5);

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
        return cursorPosCallback;
    }

    @Override
    public GLFWScrollCallback getScrollCallback() {
        return null;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mouseButtonCallback;
    }

}