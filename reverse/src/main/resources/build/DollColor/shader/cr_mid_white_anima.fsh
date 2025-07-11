
precision highp float;


varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;

uniform sampler2D fTextureMask;
uniform sampler2D fTextureWhiteOrg;
uniform vec4 touchColor;

uniform vec2 centerPoint;
uniform float centerR;
uniform float progress;

const float dx = 0.003922;
float isEques(const vec4 colorOne, const vec4 colorTwo) {
    vec2 colorDist = abs(vec2(colorOne.r, colorOne.g) - vec2(colorTwo.r, colorTwo.g));
    return all(lessThan(colorDist, vec2(dx))) ? 1.0 : 0.0;
}

float sdCircle(vec2 p, float r) {
    return length(p) - r;
}

void main() {
    vec4 rgbaMask = texture2D(fTextureMask, aCoordinate);
    vec4 rgbaOrg = texture2D(fTextureWhiteOrg, aCoordinate);
   

    vec2 uv = aCoordinate;
    uv -= centerPoint;
    float sd = sdCircle(uv, centerR * progress);
    float a = smoothstep(0.0, 0.005, sd);
    gl_FragColor = mix(vec4(0.0), rgbaOrg, (1.0 - a) * isEques(rgbaMask, touchColor));
}
