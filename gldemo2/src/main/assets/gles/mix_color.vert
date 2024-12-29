precision highp float;
attribute vec4 aPosition;
attribute vec2 aCoordinate;

varying vec2 vTexCoord;
void main() {
    gl_Position = aPosition;
    vTexCoord = vec2(aCoordinate.x, 1.0 - aCoordinate.y);
}