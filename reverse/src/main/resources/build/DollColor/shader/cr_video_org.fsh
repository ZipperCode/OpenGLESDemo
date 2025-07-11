
precision mediump float;
varying vec2 aCoordinate;
uniform sampler2D fTextureOrg;
void main() {
    gl_FragColor = texture2D(fTextureOrg, aCoordinate);
}
