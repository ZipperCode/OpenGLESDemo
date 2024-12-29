attribute vec4 vPosition;
uniform float brushSize;

void main()
{
    gl_Position = vPosition;
    gl_PointSize = brushSize;
}