precision highp float;
attribute vec4 aPosition;
attribute vec2 aTextureCoordinate;

varying vec2 vTexCoordinate;

void main() {
    gl_Position = vPosition;
    vTexCoordinate = aTextureCoordinate;
}