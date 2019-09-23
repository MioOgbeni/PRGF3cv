#version 330
in vec2 outTexCoord;
out vec4 outColor;

uniform sampler2D texture;

void main() {
    outColor = texture2D(texture, outTexCoord);
}
