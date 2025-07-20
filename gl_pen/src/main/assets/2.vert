attribute vec2 aPosition;
attribute float aPointSize;

void main() {
    gl_Position = vec4(aPosition.x, aPosition.y, 0.0, 1.0);
    gl_PointSize = aPointSize;
}