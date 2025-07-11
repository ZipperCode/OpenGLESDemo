precision highp float;


varying float fAnimationP;//0.6-1.0

float sdCircle(vec2 p, float r) {
    return length(p) - r;
}

void main() {
    float centerR = 0.45 * fAnimationP;
    vec2 uv = gl_PointCoord - vec2(0.5);
    float sdfR = sdCircle(uv, centerR);
    sdfR = abs(sdfR) - 0.1 * centerR;
    float alpha = 1.0 - smoothstep(0.0, 0.01, sdfR);

    vec4 rgba = mix(vec4(0.94, 0.28, 0.96, 1.0), vec4(0.95, 0.45, 0.96, 1.0), (fAnimationP - 0.6) / (1.0 - 0.6));
    gl_FragColor = mix(vec4(0.0), rgba, alpha);
}