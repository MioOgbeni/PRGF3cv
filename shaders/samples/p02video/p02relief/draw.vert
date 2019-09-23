#version 330
in vec2 inPosition;

out vec2 outTexCoord;
out float grey;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform sampler2D texture;

void main() {
	vec4 color = texture2D(texture, vec2(1 - inPosition.x, inPosition.y));
	grey = color.r * 0.3 + color.g * 0.59 + color.b * 0.11;

//	float zpos = (pow(grey - 0.5, 3) + 0.125) / 0.25;
//	float zpos = abs(grey - 0.5) * 2;
	float zpos = grey;
//	if (zpos > 0.4 && zpos < 0.6) zpos = 0.5;
	gl_Position = projection * view * model * vec4(inPosition, zpos, 1.0);
	outTexCoord = inPosition;
}
