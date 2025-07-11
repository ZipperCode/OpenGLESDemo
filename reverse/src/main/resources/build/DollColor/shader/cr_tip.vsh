uniform mat4 vMatrix;

attribute vec3 vPosition;
attribute float vR;
attribute float vAnimationP;

varying float fAnimationP;

void main() {
    fAnimationP = vAnimationP;
    gl_Position = vMatrix * vec4(vPosition, 1.0);
    gl_PointSize = vR;

}