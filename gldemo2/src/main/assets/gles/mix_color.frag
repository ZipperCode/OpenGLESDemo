precision highp float;
uniform sampler2D uBaseTexture;
uniform sampler2D uPaintTexture;
uniform float uMixRatio;  

varying vec2 vTexCoordinate;

// 平滑过渡函数
float smoothTransition(float edge0, float edge1, float x) {
    float t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

void main() {
    vec4 baseColor = texture2D(uBaseTexture, vTexCoordinate);
    vec4 paintColor = texture2D(uPaintTexture, vTexCoordinate);
    
    // 使用更平滑的alpha过渡
    float baseAlpha = smoothTransition(0.05, 0.95, baseColor.a);
    float paintAlpha = smoothTransition(0.05, 0.95, paintColor.a);
    
    // 如果两个纹理在该位置都没有颜色，则丢弃该片段
    if (baseAlpha < 0.01 && paintAlpha < 0.01) {
        discard;
    } 
    // 如果只有新绘制的颜色
    else if (baseAlpha < 0.01) {
        gl_FragColor = vec4(paintColor.rgb, 1.0);
    } 
    // 如果只有缓存的颜色
    else if (paintAlpha < 0.01) {
        gl_FragColor = vec4(baseColor.rgb, 1.0);
    } 
    // 如果两个位置都有颜色，进行颜色混合
    else {
        // 使用传入的混合比例
        vec3 mixedColor = mix(baseColor.rgb, paintColor.rgb, uMixRatio);
        float mixedAlpha = mix(baseAlpha, paintAlpha, uMixRatio);
        
        gl_FragColor = vec4(mixedColor, 1.0);
    }
}