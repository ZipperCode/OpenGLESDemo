uniform mat4 matrix;
uniform float fTime;

attribute vec3 a_Position;
attribute vec3 bgColor;
attribute float fParticleSize;
attribute float fParticleAngle;

varying vec3 v_Color;
varying float v_ParticleAngle;  // 始终为0
uniform float vFboSize;   // fboW / fboH
uniform float vMaskSize;  // modelW / modelH
varying float fFboSize;
varying float fMaskSize;



void main(){
    v_Color = bgColor;
    v_ParticleAngle = fParticleAngle;
    fFboSize=vFboSize;
    fMaskSize=vMaskSize;
    gl_Position = vec4(a_Position.x, a_Position.y, 0.0, 1.0);
    float aspectRatio = vMaskSize / vFboSize;
    gl_PointSize = fParticleSize*aspectRatio;
}