#version 330
in vec2 outTexCoord;
out vec4 outColor;

uniform int faceX;
uniform int faceY;
uniform int faceW;
uniform int faceH;
uniform int image_w;
uniform int image_h;
uniform int window_w;
uniform int window_h;
uniform sampler2D texture;

void main() {
    int currentX = int(window_w * outTexCoord.s);
    int currentY = int(window_h * outTexCoord.t);

    int lineW = 2;
    float ratioW = window_w / float(image_w);
    float ratioH = window_h / float(image_h);
    int coordX = int(faceX * ratioW);
    int coordY = int(faceY * ratioH);
    int width = int(faceW * ratioW);
    int height = int(faceH * ratioH);

    if (
        // left line
        ((currentX - lineW) < coordX         && (currentX + lineW) > coordX         && currentY > coordY && currentY < coordY + height) ||
        // right line
        ((currentX - lineW) < coordX + width && (currentX + lineW) > coordX + width && currentY > coordY && currentY < coordY + height) ||
        // top line
        ((currentY - lineW) < coordY          && (currentY + lineW) > coordY         && currentX > coordX && currentX < coordX + width) ||
        // bottom line
        ((currentY - lineW) < coordY + height && (currentY + lineW) > coordY + height && currentX > coordX && currentX < coordX + width)
        ) {
        outColor = vec4(1.0);
    } else {
        outColor = texture2D(texture, outTexCoord);
    }
}
