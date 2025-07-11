precision mediump float;

varying vec3 v_Color;
varying float v_ElapsedTime;
varying float v_ParticleLifeTime;
varying float v_ParticleAngle;

uniform sampler2D uTextuer;

void main() {
    vec4 rgba = texture2D(uTextuer, gl_PointCoord);
    float f = (v_ParticleLifeTime - v_ElapsedTime);
    gl_FragColor = vec4(v_Color, rgba.a * smoothstep(0.0, 0.2, f));
}


