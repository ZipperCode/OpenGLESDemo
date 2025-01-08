attribute vec4 aPosition;

varying vec4 vPosition;

void main() {
    gl_Position = aPosition;
    vPosition = aPosition;
}