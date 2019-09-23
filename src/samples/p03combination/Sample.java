package samples.p03combination;

import helpers.AbstractRenderer;
import helpers.LwjglWindow;
import helpers.OpenCVImageFormat;
import lwjglutils.*;
import opencvutils.CameraGrabber;
import opencvutils.Grabber;
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
 * Sample using combination of camera and video feeds in one scene.
 *
 * @author PGRF FIM UHK
 * @version 1.0
 * @since 2019-09-01
 */
public class Sample extends AbstractRenderer {

    private OGLBuffers buffers, buffersVideo;
    private OGLTexture2D.Viewer textureViewer;
    private OGLTexture2D cameraImage, videoImage;
    private Grabber cameraGrabber;
    private VideoGrabber videoGrabber;
    private OpenCVImageFormat cameraImageFormat;

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

        cameraGrabber = new CameraGrabber();
        videoGrabber = new VideoGrabber("./res/Hello_world_!.mp4");
        cameraImageFormat = new OpenCVImageFormat(3);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        shaderProgram = ShaderUtils.loadProgram("/samples/p03combination/draw");

        camera = new Camera()
                .withPosition(new Vec3D(-0.7, 4.3, 1.8))
                .addAzimuth(1.75 * Math.PI)
                .addZenith(-0.07 * Math.PI);

        locModel = glGetUniformLocation(shaderProgram, "model");
        locView = glGetUniformLocation(shaderProgram, "view");
        locProjection = glGetUniformLocation(shaderProgram, "projection");

        double ratio = LwjglWindow.HEIGHT / (double) LwjglWindow.WIDTH;
        projection = new Mat4PerspRH(Math.PI / 3, ratio, 0.1, 200.0);

        float[] vertexBufferDataCamera = {
                1, -1, 1, 1, 0,
                -1, -1, 1, 0, 0,
                -1, -1, -1, 0, 1,
                1, -1, -1, 1, 1,

                -1, -1, 1, 1, 0,
                -1, 1, 1, 0, 0,
                -1, 1, -1, 0, 1,
                -1, -1, -1, 1, 1
        };
        float[] vertexBufferDataVideo = {
                1, 1, 1, 1, 0,
                1, -1, 1, 0, 0,
                1, -1, -1, 0, 1,
                1, 1, -1, 1, 1,

                -1, 1, 1, 1, 0,
                1, 1, 1, 0, 0,
                1, 1, -1, 0, 1,
                -1, 1, -1, 1, 1
        };
        int[] indexBufferDataCamera = {0, 1, 2, 0, 2, 3, /**/ 4, 5, 6, 4, 6, 7};
        int[] indexBufferDataVideo = {0, 1, 2, 0, 2, 3, /**/ 4, 5, 6, 4, 6, 7};

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3, 0),
                new OGLBuffers.Attrib("inTexCoord", 2, 3)
        };
        buffers = new OGLBuffers(vertexBufferDataCamera, 5, attributes, indexBufferDataCamera);
        buffersVideo = new OGLBuffers(vertexBufferDataVideo, 5, attributes, indexBufferDataVideo);

        textureViewer = new OGLTexture2D.Viewer();
        textRenderer = new OGLTextRenderer(width, height);
    }

    public void display() {
        glEnable(GL_DEPTH_TEST);
        glUseProgram(shaderProgram);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, width, height);

        // 1. display camera image
        glUniformMatrix4fv(locModel, false, new Mat4Transl(3, 0, 0).floatArray());
        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection, false, projection.floatArray());

        ByteBuffer cameraBuffer = cameraGrabber.grabImage();
        if (cameraImage == null) {
            cameraImage = new OGLTexture2D(cameraGrabber.getWidth(), cameraGrabber.getHeight(), cameraImageFormat, cameraBuffer);
        } else {
            cameraImage.setTextureBuffer(cameraImageFormat, cameraBuffer);
        }
        cameraImage.bind(shaderProgram, "texture", 0);
        buffers.draw(GL_TRIANGLES, shaderProgram);

        // 2. display video image
        ByteBuffer videoBuffer = videoGrabber.grabImage();
        if (videoBuffer == null) {
            videoGrabber.rewind();
            videoBuffer = videoGrabber.grabImage();
        }
        if (videoImage == null) {
            videoImage = new OGLTexture2D(videoGrabber.getWidth(), videoGrabber.getHeight(), cameraImageFormat, videoBuffer);
        } else {
            videoImage.setTextureBuffer(cameraImageFormat, videoBuffer);
        }
        videoImage.bind(shaderProgram, "texture", 0);
        buffersVideo.draw(GL_TRIANGLES, shaderProgram);

        // display textures and text
        textureViewer.view(cameraImage, -1, -1, 0.5);
        textureViewer.view(videoImage, -1, -0.5, 0.5);

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