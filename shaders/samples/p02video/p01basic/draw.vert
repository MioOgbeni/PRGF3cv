#version 330
in vec2 inPosition;
in vec2 inTexCoord;
out vec2 outTexCoord;

void main() {
	gl_Position = vec4(inPosition, 0.0, 1.0);
	outTexCoord = inTexCoord;
}
