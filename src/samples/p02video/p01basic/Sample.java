package samples.p02video.p01basic;

import helpers.AbstractRenderer;
import helpers.OpenCVImageFormat;
import lwjglutils.*;
import opencvutils.VideoGrabber;
import org.lwjgl.glfw.GLFWCursorPosCallback;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * Sample showing basic video grabbing.
 *
 * @author PGRF FIM UHK
 * @version 1.0
 * @since 2019-09-01
 */
public class Sample extends AbstractRenderer {

    private OGLBuffers buffers;
    private OGLTexture2D videoImage;
    private VideoGrabber videoGrabber;
    private OpenCVImageFormat videoImageFormat;

    private int shaderProgram;
    private long grabTime;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        videoGrabber = new VideoGrabber("./res/Hello_world_!.mp4");
        System.out.println("Video FPS: " + videoGrabber.getFPS());
        videoImageFormat = new OpenCVImageFormat(3);

        shaderProgram = ShaderUtils.loadProgram("/samples/p02video/p01basic/draw");

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

//        new Thread(() -> {
//            while (true) {
//                System.out.println("1 s");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

    }

//    double msSinceLastGrab = 0;

    @Override
    public void display() {
        glUseProgram(shaderProgram);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, width, height);

//        long currentTime = System.currentTimeMillis();
//        long diff = currentTime - grabTime;
//        double frameTime = 1000 / videoGrabber.getFPS();
//
//        if (diff - msSinceLastGrab > frameTime) {
        ByteBuffer buffer = videoGrabber.grabImage();
        if (buffer != null) {
            System.out.format("%.1f s / %.1f s", videoGrabber.getCurrentVideoTime(), videoGrabber.getTotalVideoTime());
            System.out.println();
            if (videoImage == null) {
                videoImage = new OGLTexture2D(videoGrabber.getWidth(), videoGrabber.getHeight(), videoImageFormat, buffer);
            } else {
                videoImage.setTextureBuffer(videoImageFormat, buffer);
            }
            videoImage.bind(shaderProgram, "texture", 0);
        } else {
            videoGrabber.rewind();
        }
//            msSinceLastGrab = diff - frameTime;
//            grabTime = currentTime;
//        } else {
//            msSinceLastGrab += diff;
//        }

        buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

        textRenderer.clear();
        textRenderer.addStr2D(width - 90, height - 3, " (c) PGRF UHK");
        textRenderer.draw();
    }

    @Override
    public GLFWCursorPosCallback getCursorPosCallback() {
        return null;
    }

}
