precision highp float;
attribute vec4 aPosition;

attribute vec2 aTexCoord;
uniform float uPointSize;
varying vec2 vTexCoord;

void main() {
    gl_Position = aPosition;
    gl_PointSize = uPointSize;
    vTexCoord = aTexCoord;
}