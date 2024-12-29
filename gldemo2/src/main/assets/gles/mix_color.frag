precision highp float;
uniform sampler2D uBaseTexture;
uniform sampler2D uPaintTexture;

varying vec2 vTexCoord;

float ratio = 0.5;

vec4 mix_color(vec4 baseColor, vec4 paintColor) {
    highp float reverseRatio = 1.0 - ratio;
    vec4 resultColor = vec4(0.0);
    resultColor.r = baseColor.r * reverseRatio + paintColor.r * ratio;
    resultColor.g = baseColor.g * reverseRatio + paintColor.g * ratio;
    resultColor.b = baseColor.b * reverseRatio + paintColor.b * ratio;
    resultColor.a = baseColor.a * reverseRatio + paintColor.a * ratio;
    return baseColor * reverseRatio + paintColor * ratio;

}

vec4 mix2(vec4 baseColor, vec4 paintColor) {
    return baseColor - (baseColor - paintColor) * (1.0 - baseColor.a);
}

void main() {
    vec4 baseColor = texture2D(uBaseTexture, vTexCoord);
    vec4 paintColor = texture2D(uPaintTexture, vTexCoord);
    if (baseColor.a < 1.0 && paintColor.a < 1.0) {
        // 空白部分填充透明
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    } else if (baseColor.a > 0.0 && paintColor.a < 1.0){
        // 画笔透明部分填充base
        gl_FragColor = baseColor;
    } else if (baseColor.a > 0.0 && paintColor.a > 0.0) {
        // 重合部分混合
        gl_FragColor = mix_color(baseColor, paintColor);
//        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
}