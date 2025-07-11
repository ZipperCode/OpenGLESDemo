attribute vec4 vPosition;
attribute vec2 vCoordinate;
varying vec2 aCoordinate;
uniform mat4 vMatrix;
varying vec2 aFBOTextureCoord;

void main() {
    gl_Position = vMatrix * vPosition;
    aCoordinate = vCoordinate;
    aFBOTextureCoord = vec2(vCoordinate.x, 1.0 - vCoordinate.y);
}
