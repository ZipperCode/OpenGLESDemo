attribute vec2 aPosition;
attribute vec3 aColor;

varying vec3 vColor;
varying vec2 vPosition;

void main() {
    gl_PointSize = 400.0;
    vPosition = aPosition;
    gl_Position = vec4(aPosition.x, aPosition.y, 0.0, 1.0);
    vColor = aColor;
}