precision mediump float;

uniform sampler2D uTexture;
uniform vec2 uResolution;
varying vec2 vTexCoordinate;
#define FXAA_REDUCE_MIN   (1.0 / 128.0)
#define FXAA_REDUCE_MUL   (1.0 / 8.0)
#define FXAA_SPAN_MAX     32.0

void main() {
    vec3 rgbNW = texture2D(uTexture, vTexCoordinate).xyz;
    vec3 rgbNE = texture2D(uTexture, vTexCoordinate + vec2(1.0, 0.0) * uResolution).xyz;
    vec3 rgbSW = texture2D(uTexture, vTexCoordinate + vec2(0.0, 1.0) * uResolution).xyz;
    vec3 rgbSE = texture2D(uTexture, vTexCoordinate + vec2(1.0, 1.0) * uResolution).xyz;
    vec3 rgbM = texture2D(uTexture, vTexCoordinate).xyz;

    // 亮度计算
    float lumaNW = dot(rgbNW, vec3(0.299, 0.587, 0.114));
    float lumaNE = dot(rgbNE, vec3(0.299, 0.587, 0.114));
    float lumaSW = dot(rgbSW, vec3(0.299, 0.587, 0.114));
    float lumaSE = dot(rgbSE, vec3(0.299, 0.587, 0.114));
    float lumaM = dot(rgbM, vec3(0.299, 0.587, 0.114));

    // 边缘检测
    vec2 dir;
    dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
    dir.y = ((lumaNW + lumaSW) - (lumaNE + lumaSE));

    // 计算边缘方向
    float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * 0.25 * FXAA_REDUCE_MUL, FXAA_REDUCE_MIN);
    float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
    dir = min(vec2(FXAA_SPAN_MAX, FXAA_SPAN_MAX),
              max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX), dir * rcpDirMin)) * uResolution;

    // 混合计算
    vec3 rgbA = 0.5 * (
    texture2D(uTexture, vTexCoordinate + dir * (1.0 / 3.0 - 0.5)).xyz +
    texture2D(uTexture, vTexCoordinate + dir * (2.0 / 3.0 - 0.5)).xyz);

    vec3 rgbB = rgbA * 0.5 + 0.25 * (
    texture2D(uTexture, vTexCoordinate + dir * -0.5).xyz +
    texture2D(uTexture, vTexCoordinate + dir * 0.5).xyz);

    // 子像素抗锯齿
    float lumaB = dot(rgbB, vec3(0.299, 0.587, 0.114));
    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

    if ((lumaB < lumaMin) || (lumaB > lumaMax)) {
        gl_FragColor = vec4(rgbA, 1.0);
    } else {
        gl_FragColor = vec4(rgbB, 1.0);
    }
}