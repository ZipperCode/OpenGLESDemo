precision highp float;
const float DX = 0.003922;
varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;

uniform sampler2D uTextureMask;
uniform sampler2D uTextureWhiteOrg;
uniform sampler2D uTextureLoosePowder;

uniform float uUseloosePowder;

uniform vec4 uSelectMaskColor;

uniform vec4 colors[4];

uniform float owh;

uniform vec2 centerPoint;
uniform float centerR;
uniform float progress;


uniform float startY;
uniform float endY;
uniform float sourceHeight;
uniform int gradientColorCount;

vec4 mixcolor(vec4 bottom, vec4 top) {
    return vec4(
    top.r + bottom.r * (1.0 - top.a),
    top.g + bottom.g * (1.0 - top.a),
    top.b + bottom.b * (1.0 - top.a),
    1.0);
}
float isEques(const vec4 colorOne, const vec4 colorTwo) {
    vec2 colorDist = abs(vec2(colorOne.r, colorOne.g) - vec2(colorTwo.r, colorTwo.g));
    return all(lessThan(colorDist, vec2(DX))) ? 1.0 : 0.0;
}

vec4 changeColor(vec4 originColor, vec4 drawColor) {
    highp float Y = (0.257 * originColor.r) + (0.504 * originColor.g) + (0.098 * originColor.b) + 16.0;
    highp float V = (0.439 * originColor.r) - (0.368 * originColor.g) - (0.071 * originColor.b) + 128.0;
    highp float U = -(0.148 * originColor.r) - (0.291 * originColor.g) + (0.439 * originColor.b) + 128.0;

    float Y1 = (0.257 * drawColor.r) + (0.504 * drawColor.g) + (0.098 * drawColor.b) + 16.0;
    U = -(0.148 * drawColor.r) - (0.291 * drawColor.g) + (0.439 * drawColor.b) + 128.0;
    V = (0.439 * drawColor.r) - (0.368 * drawColor.g) - (0.071 * drawColor.b) + 128.0;

    Y = (Y1 * Y + Y * Y1) / (Y1 + Y);

    highp float b = 1.164 * (Y - 16.0) + 2.018 * (U - 128.0);
    highp float g = 1.164 * (Y - 16.0) - 0.813 * (V - 128.0) - 0.391 * (U - 128.0);
    highp float r = 1.164 * (Y - 16.0) + 1.596 * (V - 128.0);

    return vec4(r, g, b, 1.0);
}


vec4 jianbian() {
    vec4 originColor = texture2D(uTextureWhiteOrg, aCoordinate);
    float yPos = aCoordinate.y;
    float newStartY = startY / sourceHeight;
    float newEndY = endY / sourceHeight;
    yPos = clamp(yPos, newStartY, newEndY);
    vec4 finalColor = vec4(0.0);
    if (gradientColorCount == 1) {
        finalColor = changeColor(originColor, colors[0]);
    } else {
        float t = (yPos - newStartY) / (newEndY - newStartY);
        int colorIndex = int(t * float(gradientColorCount - 1));
        vec4 startColor = changeColor(originColor, colors[colorIndex]);
        vec4 endColor = changeColor(originColor, colors[colorIndex + 1]);
        finalColor = mix(startColor, endColor, fract(t * float(gradientColorCount - 1)));
    }
    return finalColor;
}


vec4 shangfeng(vec4 rgbaUse) {
    highp float dw = 1.0 / 4.0;
    highp float dh = 1.0 / 4.0 * owh;
    highp float dx = (aCoordinate.x - floor(aCoordinate.x / dw) * dw) / dw;
    highp float dy = (aCoordinate.y - floor(aCoordinate.y / dh) * dh) / dh;
    vec2 dMosaicCoor = vec2(dx, dy);
    vec4 rgbaShanfeng = texture2D(uTextureLoosePowder, dMosaicCoor);
    return mixcolor(rgbaUse, rgbaShanfeng);
}

float sdCircle(vec2 p, float r) {
    return length(p) - r;
}


void main() {
    vec4 maskRgba = texture2D(uTextureMask, aCoordinate);
    vec4 gResult = jianbian();
    vec4 shangfengColor = shangfeng(gResult);
    gResult = mix(gResult, shangfengColor, uUseloosePowder);
    gResult = mix(vec4(1.0), gResult, colors[0].a);

    vec2 uv = aCoordinate;
    uv -= centerPoint;
    float sd = sdCircle(uv, centerR * progress);
    float a = smoothstep(0.0, 0.005, sd);
    gl_FragColor = mix(vec4(0.0), gResult, (1.0 - a) * isEques(maskRgba, uSelectMaskColor));
}


