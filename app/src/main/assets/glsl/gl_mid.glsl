precision mediump float;
varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;
uniform sampler2D fTextureWhite;

void main() {
    gl_FragColor = texture2D(fTextureWhite, aFBOTextureCoord);
}

