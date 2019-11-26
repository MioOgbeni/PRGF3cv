#version 330
in vec2 outTexCoord;
out vec4 outColor;

uniform sampler2D texture;
uniform sampler2D bayerMatrixTexture;

uniform bool ascii;
uniform bool grayScale;
uniform bool dithering;

uniform vec2 bufferResolution;
uniform vec2 mouseXY;

float character(int n, vec2 p)
{
    p = floor(p*vec2(4.0, -4.0) + 2.5);
    if (clamp(p.x, 0.0, 4.0) == p.x)
    {
        if (clamp(p.y, 0.0, 4.0) == p.y)
        {
            int a = int(round(p.x) + 5.0 * round(p.y));
            if (((n >> a) & 1) == 1) return 1.0;
        }
    }
    return 0.0;
}

void main() {
    //vec3 color = texture2D(texture, (floor(gl_FragCoord.xy/16.0)*16.0/bufferResolution.xy)).rgb;
    vec3 color = texture2D(texture, outTexCoord).rgb;
    vec3 oldColor = color;

    int n;
    vec2 p;
    float gray = 0.3 * color.r + 0.59 * color.g + 0.11 * color.b;
    if(ascii){
        //bitmap generated in http://www.thrill-project.com/archiv/coding/bitmap/
        n =  4096;                // .
        if (gray > 0.2) n = 65600;    // :
        if (gray > 0.3) n = 332772;   // *
        if (gray > 0.4) n = 15255086; // o
        if (gray > 0.5) n = 23385164; // &
        if (gray > 0.6) n = 15252014; // 8
        if (gray > 0.7) n = 13199452; // @
        if (gray > 0.8) n = 11512810; // #

        p = mod(gl_FragCoord.xy/4.0, 2.0) - vec2(1.0);

        if(grayScale){
            color = gray * vec3(character(n, p));
        }else{
            color = color * character(n, p);
        }

    }else if(dithering){
        vec3 value;

        if(grayScale){
            value = vec3(gray).rgb;
        }else{
            value = color.rgb;
        }

        vec3 oldcolor = value + (value * texture2D(bayerMatrixTexture, (mod(gl_FragCoord.xy, 8.0) / 8.0)).rgb);
        vec3 newcolor = floor(oldcolor);

        color = newcolor;
    }else{
        if(grayScale){
            color = vec3(gray);
        }
    }

    if(gl_FragCoord.x > mouseXY.x){
        outColor = vec4(color, 1.0);
    }else{
        outColor = vec4(oldColor, 1.0);
    }
}
