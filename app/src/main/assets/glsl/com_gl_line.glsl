precision mediump float;
varying vec2 aCoordinate;
uniform sampler2D lineTexture;

void main() {
    gl_FragColor = texture2D(lineTexture, aCoordinate);
}





