
// 顶点坐标
attribute vec4 vPosition;
// 纹理坐标
attribute vec2 vCoordinate;
// 传递给片元着色器的纹理坐标
varying vec2 aCoordinate;

uniform mat4 vMatrix;

void main() {
    gl_Position = vMatrix * vPosition;
    aCoordinate = vCoordinate;
}