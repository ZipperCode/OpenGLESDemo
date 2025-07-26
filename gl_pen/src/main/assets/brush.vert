attribute vec2 aPosition;
attribute float aAngle;
attribute float aPointSize;
attribute vec3 aColor;

varying float vAngle;
varying vec3 vColor;

void main() {
    gl_Position = vec4(aPosition.x, aPosition.y, 0.0, 1.0);
    gl_PointSize = aPointSize;
    vAngle = aAngle;
    vColor = aColor;
}

