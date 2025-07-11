
precision mediump float;
const float dx = 0.003922;
varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;
uniform sampler2D fTextureMosaic;
uniform sampler2D fTextureMask;

uniform vec4 tipMaskColor;

float isEques(const vec4 colorOne, const vec4 colorTwo) {
    vec2 colorDist = abs(vec2(colorOne.r, colorOne.g) - vec2(colorTwo.r, colorTwo.g));
    return all(lessThan(colorDist, vec2(dx))) ? 1.0 : 0.0;
}

void main() {
    vec4 rgbaMosaic = texture2D(fTextureMosaic, aFBOTextureCoord);
    vec4 rgbaMask = texture2D(fTextureMask, aCoordinate);

    float eq = isEques(rgbaMask, tipMaskColor);

    vec4 tipColor = vec4(0.52157, 0.96863, 0.50588, 1.0) * rgbaMosaic;
    gl_FragColor = mix(rgbaMosaic, tipColor, eq);
}