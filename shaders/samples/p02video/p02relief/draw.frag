#version 330
in float grey;
out vec4 outColor;

void main() {
    outColor = vec4(grey, grey, grey, 1.0);
}
