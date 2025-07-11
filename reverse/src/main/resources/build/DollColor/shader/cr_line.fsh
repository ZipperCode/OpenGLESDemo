precision mediump float;
varying vec2 aCoordinate;
const float DX = 0.003922;

uniform sampler2D fTextureLine;
uniform sampler2D fTextureOrg;
uniform sampler2D fTextureMask;
uniform float colorLine;
uniform vec4 fliterMaskPixel;
uniform float saveFinal;

bool isEques(const vec4 colorOne, const vec4 colorTwo) {
    vec2 colorDist = abs(vec2(colorOne.r, colorOne.g) - vec2(colorTwo.r, colorTwo.g));
    return all(lessThan(colorDist, vec2(DX)));
}

void main() {
    vec4 lineRGBA = texture2D(fTextureLine, aCoordinate);
    vec4 orgRGBA = texture2D(fTextureOrg, aCoordinate);
    vec4 currentMaskPixel = texture2D(fTextureMask, aCoordinate);

    vec4 finalSaveColor = mix(orgRGBA, vec4(0.0), float(isEques(fliterMaskPixel, currentMaskPixel)));
    vec4 normalColor=mix(vec4(0.0), vec4(orgRGBA.rgb * colorLine, 1.0), lineRGBA.a);
    gl_FragColor = mix(normalColor, finalSaveColor, saveFinal);

}




