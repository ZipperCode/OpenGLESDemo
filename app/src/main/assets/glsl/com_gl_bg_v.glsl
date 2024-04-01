attribute vec4 vPosition;
attribute vec2 vCoordinate;
uniform mat4 vMatrix;
void main() {
    gl_Position = vPosition;
}