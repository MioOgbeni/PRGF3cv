package myProject;

import helpers.AbstractRenderer;
import helpers.LwjglWindow;
import helpers.OpenCVImageFormat;
import lwjglutils.OGLBuffers;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture2D;
import lwjglutils.ShaderUtils;
import opencvutils.CameraGrabber;
import opencvutils.VideoGrabber;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.*;

public class Renderer extends AbstractRenderer {
    private OGLBuffers buffers;
    private OGLTexture2D videoImage;
    private VideoGrabber videoGrabber;
    private OpenCVImageFormat videoImageFormat;
    private OGLTexture2D cameraImage;
    private CameraGrabber cameraGrabber;
    private OpenCVImageFormat cameraImageFormat;
    private OGLTexture2D bayerMatrixTexture;

    private boolean mousePressed;
    private double oldMx = 0;
    private double oldMy = 0;

    private int locAscii, locGrayScale, locBufferResolution, locMouseXY, locDithering;
    private boolean isCamera = false;
    private boolean ascii = false;
    private boolean grayScale = false;
    private boolean dithering = false;

    private int cameraWidth, cameraHeight;

    private int shaderProgram;

    @Override
    public void init() {
        //camera grab
        cameraImageFormat = new OpenCVImageFormat(3);
        cameraGrabber = new CameraGrabber(width,height,1);

        cameraGrabber.grabImageRaw();

        cameraWidth = cameraGrabber.getWidth();
        cameraHeight = cameraGrabber.getHeight();
        System.out.println("Camera width " + cameraWidth + " ; Camera height " + cameraHeight);

        cameraImage = new OGLTexture2D(cameraWidth, cameraHeight, cameraImageFormat, null);

        //video grab
        videoGrabber = new VideoGrabber("./res/Hello_world_!.mp4");
        System.out.println("Video FPS: " + videoGrabber.getFPS());
        videoImageFormat = new OpenCVImageFormat(3);

        try {
            bayerMatrixTexture = new OGLTexture2D("./bayer.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //load shaders
        shaderProgram = ShaderUtils.loadProgram("/myProject/draw");

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
        loadShader(shaderProgram);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, width, height);

        glUniform2f(locMouseXY, (float) oldMx, (float) oldMy);

        if(isCamera){
            if(cameraWidth == 0 || cameraHeight == 0){
                System.out.println("Camera is corrupted! Width: " + cameraWidth + " Height: " +  cameraHeight);
                System.out.println("Camera forced OFF");
                isCamera = false;
            }else{
                ByteBuffer buffer = cameraGrabber.grabImage();
                cameraImage.setTextureBuffer(cameraImageFormat, buffer);
                cameraImage.bind(shaderProgram, "texture", 0);
            }
        }else{
            ByteBuffer buffer = videoGrabber.grabImage();
            if (buffer != null) {
                if (videoImage == null) {
                    videoImage = new OGLTexture2D(videoGrabber.getWidth(), videoGrabber.getHeight(), videoImageFormat, buffer);
                } else {
                    videoImage.setTextureBuffer(videoImageFormat, buffer);
                }
                videoImage.bind(shaderProgram, "texture", 0);
            } else {
                videoGrabber.rewind();
            }

            glUniform2f(locBufferResolution, videoGrabber.getWidth(), videoGrabber.getHeight());
        }

        bayerMatrixTexture.bind(shaderProgram, "bayerMatrixTexture", 1);

        glUniform1i(locDithering, dithering ? 1 : 0);
        glUniform1i(locAscii, ascii ? 1 : 0);
        glUniform1i(locGrayScale, grayScale ? 1 : 0);

        buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

        String differenceText = "To view difference use mouse";
        String cameraText = "C for toggle camera";
        String asciiText = "A for toggle ascii rendering";
        String grayScaleText = "G for toggle grayscale rendering";
        String ditheringText = "D for toggle dithering render";

        textRenderer.clear();
        textRenderer.addStr2D(3, 20, differenceText);
        textRenderer.addStr2D(3, 40, String.format("%s (%s %s)", cameraText, isCamera, isCamera ? 1 : 0));
        textRenderer.addStr2D(3, 60, String.format("%s (%s %s)", asciiText, ascii, ascii ? 1 : 0));
        textRenderer.addStr2D(3, 80, String.format("%s (%s %s)", grayScaleText, grayScale, grayScale ? 1 : 0));
        textRenderer.addStr2D(3, 100, String.format("%s (%s %s)", ditheringText, dithering, dithering ? 1 : 0));
        textRenderer.addStr2D(width - 90, height - 3, " (c) PGRF UHK");
        textRenderer.draw();
    }

    private void loadShader(int shader) {
        locAscii = glGetUniformLocation(shader, "ascii");
        locGrayScale = glGetUniformLocation(shader, "grayScale");
        locBufferResolution = glGetUniformLocation(shader, "bufferResolution");
        locMouseXY = glGetUniformLocation(shader, "mouseXY");
        locDithering = glGetUniformLocation(shader, "dithering");
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_C:
                        if (isCamera) {
                            isCamera = false;
                            System.out.println("Camera OFF");
                        } else {
                            isCamera = true;
                            System.out.println("Camera ON");
                        }
                        break;
                    case GLFW_KEY_A:
                        if (ascii) {
                            ascii = false;
                            System.out.println("Ascii rendering OFF");
                        } else {
                            ascii = true;
                            dithering = false;
                            System.out.println("Ascii rendering ON");
                        }
                        break;
                    case GLFW_KEY_G:
                        if (grayScale) {
                            grayScale = false;
                            System.out.println("Grayscale rendering OFF");
                        } else {
                            grayScale = true;
                            System.out.println("Grayscale rendering ON");
                        }
                        break;
                    case GLFW_KEY_D:
                        if (dithering) {
                            dithering = false;
                            System.out.println("Dithering render OFF");
                        } else {
                            dithering = true;
                            ascii = false;
                            System.out.println("Dithering render ON");
                        }
                        break;
                }
            }
        }
    };

    @Override
    public GLFWCursorPosCallback getCursorPosCallback() {
        return cursorPosCallback;
    }

    private GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mousePressed) {
                oldMx = x;
                oldMy = y;
            }
        }
    };

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mouseButtonCallback;
    }

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
}
