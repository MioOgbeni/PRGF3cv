#version 330
in vec3 inPosition;
in vec2 inTexCoord;

out vec2 outTexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
    gl_Position = projection * view * model * vec4(inPosition, 1.0);
    outTexCoord = inTexCoord;
}
