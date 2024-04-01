precision highp float;
const float DX = 0.003922;
varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;

uniform sampler2D uTextureMask;
uniform sampler2D uTextureWhiteOrg;
uniform sampler2D uTextureLoosePowder;

uniform float uUseloosePowder;

uniform vec4 uSelectMaskColor;
uniform vec4 uStartColor;
uniform vec4 uEndColor;

uniform float owh;//图片的宽高比 小于1

uniform vec2 centerPoint;
uniform float centerR;
uniform float progress;

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

vec4 blendMultiply(vec4 base, vec4 blend) {
    return base * blend;
}

vec4 jianbian() {
    vec4 originColor = texture2D(uTextureWhiteOrg, aCoordinate);
    vec4 start = blendMultiply(originColor, uStartColor);
    vec4 end = blendMultiply(originColor, uEndColor);
    return mix(start, end, 2.0 * distance(aCoordinate, vec2(0.5, 0.5)));
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
    gResult = mix(vec4(1.0), gResult, uStartColor.a);//如果是undo的最后一个 传递的颜色是透明色，取白色

    vec2 uv = aCoordinate;
    uv -= centerPoint;
    float sd = sdCircle(uv, centerR * progress);
    float a = smoothstep(0.0, 0.005, sd);
    gl_FragColor = mix(vec4(0.0), gResult, (1.0 - a) * isEques(maskRgba, uSelectMaskColor));
}

