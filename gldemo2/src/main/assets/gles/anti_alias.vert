precision highp float;

attribute vec4 aPosition;
attribute vec2 aTextureCoordinate;
uniform mat4 uMatrix;

varying vec2 vTexCoordinate;

void main() {
    gl_Position = uMatrix * aPosition;
    vTexCoordinate = vec2(aTextureCoordinate.x, 1.0 - aTextureCoordinate.y);
}