
precision mediump float;
const float dx = 0.003922;
varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;
uniform sampler2D fTextureMask;
uniform sampler2D fTextureWhiteOrg;

uniform vec4 touchColor;
uniform vec4 touchColorNext;

float isEques(const vec4 colorOne, const vec4 colorTwo) {
    vec2 colorDist = abs(vec2(colorOne.r, colorOne.g) - vec2(colorTwo.r, colorTwo.g));
    return all(lessThan(colorDist, vec2(dx))) ? 1.0 : 0.0;
}

void main() {
    vec4 rgbaMask = texture2D(fTextureMask, aCoordinate);
    vec4 rgbaOrg = texture2D(fTextureWhiteOrg, aCoordinate);
    vec4 touchRGBA = mix(vec4(0.0), rgbaOrg, isEques(rgbaMask, touchColor));
    gl_FragColor = mix(touchRGBA, rgbaOrg, isEques(rgbaMask, touchColorNext));
}





