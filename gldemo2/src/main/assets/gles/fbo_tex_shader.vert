precision highp float;

attribute vec4 aPosition;
attribute vec2 aCoordinate;

uniform mat4 uMatrix;
varying vec2 vTexCoordinate;

void main() {
    gl_Position = uMatrix * aPosition;
    // 纹理坐标倒置
    vTexCoordinate = vec2(aCoordinate.x, 1.0 - aCoordinate.y);
}