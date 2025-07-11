
precision mediump float;
uniform sampler2D fTextureLine;
uniform sampler2D fTextureOrg;
varying vec2 aCoordinate;

void main() {
    vec4 lineRGBA = texture2D(fTextureLine, aCoordinate);
    vec4 orgRGBA = texture2D(fTextureOrg, aCoordinate);

    gl_FragColor = mix(vec4(0.0), orgRGBA, lineRGBA.a);
}