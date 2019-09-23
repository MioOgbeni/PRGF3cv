#version 330
in vec2 outTexCoord;
out vec4 outColor;

uniform sampler2D texture;
uniform int image_width;
uniform int image_height;

float toGrey(in vec4 pixel) {
    return pixel.r * 0.33 + pixel.g * 0.33 + pixel.b * 0.33;
}

void main() {
    // distance between 2 pixels in general
    vec2 diff = vec2(1.0/image_width, 1.0/image_height);

    float center_middle = toGrey(texture2D(texture, outTexCoord));
    float left_bottom = toGrey(texture2D(texture, vec2(outTexCoord.s, outTexCoord.t + diff.y)));
    float right_top = toGrey(texture2D(texture, vec2(outTexCoord.s + diff.x, outTexCoord.t)));
    float right_bottom = toGrey(texture2D(texture, vec2(outTexCoord.s + diff.x, outTexCoord.t + diff.y)));

    // http://homepages.inf.ed.ac.uk/rbf/HIPR2/roberts.htm
    // Roberts Cross Edge Detector
    float g_x = center_middle - right_bottom;
    float g_y = left_bottom - right_top;

    float g = sqrt(g_x * g_x + g_y * g_y);

//    float direction = atan(g_y, g_x);

    outColor = vec4(vec3(g * 3), 1);
}
