uniform mat4 u_Matrix;
uniform float u_Time;

attribute vec3 aPos;
attribute vec3 aCol;
attribute vec3 aDir;
attribute float aStartTime;
attribute float aLifeTime;
attribute float aSize;
attribute float aAngle;

varying vec3  v_Color;
varying float v_ElapsedTime;
varying float v_ParticleLifeTime;
varying float v_ParticleAngle;

void main() {
    v_Color = aCol;
    v_ParticleLifeTime = aLifeTime;
    v_ElapsedTime = u_Time - aStartTime;
    v_ParticleAngle = aAngle;

    float residueP = (v_ParticleLifeTime - v_ElapsedTime) / v_ParticleLifeTime;
    vec3 currentPosition = aPos + (aDir * sqrt(0.05 * v_ParticleLifeTime + v_ElapsedTime));
    gl_Position = u_Matrix * vec4(currentPosition.x, currentPosition.y, 0.0, 1.0);
    gl_PointSize = (0.3 + 0.7 * residueP) * aSize *sqrt(u_Matrix[0][0]);
}