uniform mat4 vMatrix;

attribute vec3 vPosition;
attribute float vR;
attribute float vNumber;

varying float fNumber;

void main() {
    fNumber = vNumber;

    gl_Position = vMatrix*vec4(vPosition, 1.0);
    gl_PointSize =1.0*vR*vMatrix[0][0];
}


