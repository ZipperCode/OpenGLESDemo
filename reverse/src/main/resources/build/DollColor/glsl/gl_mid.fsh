precision mediump float;
varying vec2 aCoordinate;
varying vec2 aFBOTextureCoord;
uniform sampler2D fTextureWhite;
uniform float fboModel;

void main() {
    vec2 tempV = vec2(aCoordinate.x, abs(fboModel - aCoordinate.y));
    gl_FragColor = texture2D(fTextureWhite, tempV);
}

