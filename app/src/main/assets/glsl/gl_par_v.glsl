uniform mat4 u_Matrix;
uniform float u_Time;//代表程序运行时间

attribute vec3 aPos;//z
attribute vec3 aCol;
attribute vec3 aDir;
attribute float aStartTime;//粒子开始时间。
attribute float aLifeTime;//粒子生命时间。
attribute float aSize;//粒子的大小。
attribute float aAngle;//粒子的角度为负时逆时针旋转,反之为顺时针。

varying vec3  v_Color;
varying float v_ElapsedTime;//流逝时间
varying float v_ParticleLifeTime;//流逝时间
varying float v_ParticleAngle;//粒子的角度为负时逆时针旋转,反之为顺时针。

void main() {
    v_Color = aCol;
    v_ParticleLifeTime = aLifeTime;
    v_ElapsedTime = u_Time - aStartTime;
    v_ParticleAngle = aAngle;

    float residueP = (v_ParticleLifeTime - v_ElapsedTime) / v_ParticleLifeTime;//剩余时间百分比
    vec3 currentPosition = aPos + (aDir * sqrt(0.05 * v_ParticleLifeTime + v_ElapsedTime)); //0.05的参数是为了增加起始位置 避免出现的一刻太拥挤
    gl_Position = u_Matrix * vec4(currentPosition.x, currentPosition.y, 0.0, 1.0);//点的位置 内置变量
    gl_PointSize = (0.3 + 0.7 * residueP) * aSize *sqrt(u_Matrix[0][0]);//设置点的大小 内置变量
//    gl_PointSize = (0.6 + 0.4 * residueP) * a_ParticleSize * sqrt(u_Matrix[0][0]);//设置点的大小 内置变量
}