precision highp float;

varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;
uniform sampler2D fTextureFboBg;
uniform sampler2D fTextureFboFg;
uniform sampler2D uTextureMask;

uniform vec4 fSelectMaskColor;
uniform vec4 clickColor;
uniform sampler2D fTextureLoosePowder;
uniform float owh;
uniform float uUseloosePowder;

const float dx = 0.003922;


uniform vec4 colors[4];

uniform int gradientColorCount;

uniform float startY;
uniform float endY;
uniform float sourceHeight;

bool isEques(const vec4 colorOne, const vec4 colorTwo) {
    vec2 colorDist = abs(vec2(colorOne.r, colorOne.g) - vec2(colorTwo.r, colorTwo.g));
    return all(lessThan(colorDist, vec2(dx)));
}

vec4 mixcolor(vec4 bottom, vec4 top) {
    return vec4(
    top.r + bottom.r * (1.0 - top.a),
    top.g + bottom.g * (1.0 - top.a),
    top.b + bottom.b * (1.0 - top.a),
    1.0);
}

vec4 shangfeng(vec4 rgbaUse) {
    highp float dw = 1.0 / 4.0;
    highp float dh = 1.0 / 4.0 * owh;
    highp float dx = (aCoordinate.x - floor(aCoordinate.x / dw) * dw) / dw;
    highp float dy = (aCoordinate.y - floor(aCoordinate.y / dh) * dh) / dh;
    vec2 dMosaicCoor = vec2(dx, dy);
    vec4 rgbaShanfeng = texture2D(fTextureLoosePowder, dMosaicCoor);
    return mixcolor(rgbaUse, rgbaShanfeng);
}


vec4 jianbian() {
    float yPos = aCoordinate.y;
    float newStartY = startY / sourceHeight;
    float newEndY = endY / sourceHeight;
    yPos = clamp(yPos, newStartY, newEndY);
    vec4 finalColor = vec4(0.0);
    if (gradientColorCount == 1) {
        finalColor = colors[0];
    } else {
        float t = (yPos - newStartY) / (newEndY - newStartY);
        int colorIndex = int(t * float(gradientColorCount - 1));
        vec4 startColor = colors[colorIndex];
        vec4 endColor = colors[colorIndex + 1];
        finalColor = mix(startColor, endColor, fract(t * float(gradientColorCount - 1)));
    }
    return finalColor;
}


void main() {
    vec4 bgColor = texture2D(fTextureFboBg, vec2(aCoordinate.x, 1.0 - aCoordinate.y));
    vec4 fgColor = texture2D(fTextureFboFg, vec2(aCoordinate.x, 1.0 - aCoordinate.y));
    vec4 maskRgba = texture2D(uTextureMask, aCoordinate);

    vec4 shangfengColor = shangfeng(clickColor);
    vec4 newCurColor = mix(clickColor, shangfengColor, uUseloosePowder);

    float fgColorA = 1.0 - (fgColor.r + fgColor.g + fgColor.b) / 3.0;
    fgColorA = isEques(maskRgba, fSelectMaskColor) ? fgColorA : 0.0;

    if (gradientColorCount>1){
        newCurColor = jianbian();
    }
    vec4 finalColor = vec4(newCurColor.rgb, fgColorA);
    vec3 mixedColor = mix(bgColor.rgb, finalColor.rgb, finalColor.a);
    gl_FragColor = vec4(mixedColor, 1.0);

}

