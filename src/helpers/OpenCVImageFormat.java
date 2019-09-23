package helpers;

import lwjglutils.OGLTexImageByte;

import static org.lwjgl.opengl.GL11.GL_RGB8;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL12.GL_BGR;

/**
 * @author PGRF FIM UHK
 * @version 1.0
 * @since 2019-09-01
 */
public class OpenCVImageFormat extends OGLTexImageByte.Format {

    public OpenCVImageFormat(int componentCount) {
        super(componentCount);
    }

    @Override
    public int getInternalFormat() {
        return GL_RGB8;
    }

    @Override
    public int getPixelFormat() {
        return GL_BGR;
    }

    @Override
    public int getPixelType() {
        return GL_UNSIGNED_BYTE;
    }
}