precision highp float;
attribute vec4 aPosition;
attribute vec2 aTextureCoordinate;

uniform mat4 uMatrix;
varying vec2 vTextureCoord;

void main() {
    gl_Position = uMatrix * aPosition;
    vTextureCoord = aTextureCoordinate;
}