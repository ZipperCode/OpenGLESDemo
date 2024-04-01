precision mediump float;

varying vec3 v_Color;
varying float v_ElapsedTime;
varying float v_ParticleLifeTime;
varying float v_ParticleAngle;

uniform sampler2D uTextuer;

//gl_PointCoord 点模式下的 点的内部坐标
void main() {
    vec4 rgba = texture2D(uTextuer, gl_PointCoord);
    float f = (v_ParticleLifeTime - v_ElapsedTime);//剩余时间
    gl_FragColor = vec4(v_Color, rgba.a * smoothstep(0.0, 0.2, f));
}


