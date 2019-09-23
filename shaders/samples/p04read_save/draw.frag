#version 330
in vec2 outTexCoord;
out vec4 outColor;

uniform sampler2D texture;

void main() {
    vec4 pixel = texture2D(texture, outTexCoord);
    float color = 1 - (pixel.r * 0.3 + pixel.g * 0.59 + pixel.b * 0.11);
    outColor = vec4(color, color, color, 1.0);
}
