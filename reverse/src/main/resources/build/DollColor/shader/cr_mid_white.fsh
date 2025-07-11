
precision mediump float;

varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;

uniform sampler2D fTextureMask;
uniform sampler2D fTextureWhiteOrg;
uniform vec4 touchColor;

const float dx = 0.003922;
bool isEques(const vec4 colorOne, const vec4 colorTwo) {
    vec2 colorDist = abs(vec2(colorOne.r, colorOne.g) - vec2(colorTwo.r, colorTwo.g));
    return all(lessThan(colorDist, vec2(dx)));
}

void main() {
    vec4 rgbaMask = texture2D(fTextureMask, aCoordinate);
    vec4 rgbaOrg = texture2D(fTextureWhiteOrg, aCoordinate);
    gl_FragColor = isEques(rgbaMask, touchColor) ? rgbaOrg : vec4(0.0);
}
