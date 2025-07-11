
precision mediump float;
varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;
uniform sampler2D fTextureOrg;
void main() {
    gl_FragColor = texture2D(fTextureOrg, aFBOTextureCoord);
}
