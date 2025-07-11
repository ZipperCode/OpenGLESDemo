
precision mediump float;
varying vec2 aCoordinate;

uniform sampler2D fTextureMask;
uniform sampler2D fTextureMosaic;

uniform float owh;
uniform vec4 selectMaskColor1;
uniform vec4 selectMaskColor2;
uniform vec4 selectMaskColor3;
uniform vec4 selectMaskColor4;

const float dx = 0.003922;
float isEques(const vec4 colorOne, const vec4 colorTwo) {
    vec2 colorDist = abs(vec2(colorOne.r, colorOne.g) - vec2(colorTwo.r, colorTwo.g));
    return all(lessThan(colorDist, vec2(dx))) ? 1.0 : 0.0;
}

void main() {
    vec4 maskRgba = texture2D(fTextureMask, aCoordinate);

    highp float dw = 1.0 / 16.0;
    highp float dh = 1.0 / 16.0 * owh;
    highp float dx = (aCoordinate.x - floor(aCoordinate.x / dw) * dw) / dw;
    highp float dy = (aCoordinate.y - floor(aCoordinate.y / dh) * dh) / dh;
    vec2 dMosaicCoor = vec2(dx, dy);
    vec4 mosaicRGBA = texture2D(fTextureMosaic, dMosaicCoor);
    
    vec4 color1 = mix(vec4(0.0), mosaicRGBA, isEques(maskRgba, selectMaskColor1));
    vec4 color2 = mix(color1, mosaicRGBA, isEques(maskRgba, selectMaskColor2));
    vec4 color3 = mix(color2, mosaicRGBA, isEques(maskRgba, selectMaskColor3));
    gl_FragColor = mix(color3, mosaicRGBA, isEques(maskRgba, selectMaskColor4));
}
