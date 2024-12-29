precision mediump float;

varying vec2 vTexCoord;
uniform sampler2D uTexture;
uniform sampler2D uFrameBufferTexture;
uniform vec4 uSelectColor;
float ratio = 0.5;

vec4 mix_color2(vec4 baseColor, vec4 paintColor) {
    highp float reverseRatio = 1.0 - ratio;
    vec4 resultColor = vec4(0.0);
    resultColor.r = baseColor.r * reverseRatio + paintColor.r * ratio;
    resultColor.g = baseColor.g * reverseRatio + paintColor.g * ratio;
    resultColor.b = baseColor.b * reverseRatio + paintColor.b * ratio;
    resultColor.a = baseColor.a * reverseRatio + paintColor.a * ratio;
    return baseColor * reverseRatio + paintColor * ratio;

}
vec4 mix_color(vec4 base, vec4 blend) {
    return mix(base, blend, 0.5);
}

void main() {
    vec4 mask = texture2D(uTexture, gl_PointCoord);
    vec4 maskColor = mask.a * (vec4(1.0) - ((vec4(1.0)-vec4(uSelectColor.rgb, 1.0)))*(vec4(1.0)-mask));
//    gl_FragColor = mix_color(vec4(1.0, 0.0, 0.0, 1.0), vec4(0.0, 0.0, 1.0, 1.0));
    gl_FragColor = maskColor;
}