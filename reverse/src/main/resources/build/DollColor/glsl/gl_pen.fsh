precision highp float;

varying vec3 v_Color;
varying float v_ParticleAngle;

uniform sampler2D fUnitTexture;
varying float fFboSize;
varying float fMaskSize;


float myradians(float degrees) {
    float result=degrees * 3.14159265358979323846;
    return result/ 180.0;
}

mat4 rotateZ(float angle) {
    float c = cos(myradians(angle));
    float s = sin(myradians(angle));
    return mat4(
    c, -s, 0.0, 0.0,
    s, c, 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
    );
}

void main() {
    float vWH = 1.0 - fFboSize / fMaskSize;
    vec2 particleCenter = vec2(0.5);
    vec2 temp = vec2((gl_PointCoord.x - vWH * 0.5) / (1.0 - vWH), gl_PointCoord.y);
    vec2 rotatedCoords = (temp - particleCenter) * 2.0;
    mat4 rotationMatrix = rotateZ(v_ParticleAngle);
    vec4 pos = vec4(rotatedCoords, 0.0, 1.0);
    pos =rotationMatrix * pos;
    temp = particleCenter + pos.xy * 0.5;
    float a = texture2D(fUnitTexture, temp).a;
    gl_FragColor = vec4(vec3(0.0), a);
}


