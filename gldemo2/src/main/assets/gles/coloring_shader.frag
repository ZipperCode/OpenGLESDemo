precision highp float;



uniform sampler2D uMaskTexture;
uniform sampler2D uOrgTexture;
uniform vec4 uSelectColor;
uniform vec4 uSelectMaskColor;

varying vec2 vTextureCoord;

const float DX = 0.003922;

vec3 rgbToYuv(vec4 color) {
    highp float y = 0.257 * color.r + 0.504 * color.g + 0.098 * color.b;
    highp float u = -0.148 * color.r - 0.291 * color.g + 0.439 * color.b;
    highp float v = 0.439 * color.r - 0.368 * color.g - 0.071 * color.b;
    return vec3(y, u, v);
}

vec4 yuvToRgb(vec3 yuv) {
    float r = 1.164 * yuv.x + 1.596 * yuv.z;
    float g = 1.164 * yuv.x - 0.392 * yuv.y - 0.813 * yuv.z;
    float b = 1.164 * yuv.x + 2.017 * yuv.y;
    return vec4(r, g, b, 1.0);
}

//vec4 combineColors(vec4 originColor, vec4 drawColor) {
//    highp float Y = (0.257 * originColor.r) + (0.504 * originColor.g) + (0.098 * originColor.b) + 16.0;
//    float Y1 = (0.257 * drawColor.r) + (0.504 * drawColor.g) + (0.098 * drawColor.b) + 16.0;
//    highp float U  = -(0.148 * drawColor.r) - (0.291 * drawColor.g) + (0.439 * drawColor.b) + 128.0;
//    highp float V = (0.439 * drawColor.r) - (0.368 * drawColor.g) - (0.0714 * drawColor.b) + 128.0;
//
//    Y = (Y1 * Y + Y * Y1) / (Y1 + Y);
//
//    highp float b = 1.164 * (Y - 16.0) + 2.018 * (U - 128.0);
//    highp float g = 1.164 * (Y - 16.0) - 0.813 * (V - 128.0) - 0.391 * (U - 128.0);
//    highp float r = 1.164 * (Y - 16.0) + 1.596 * (V - 128.0);
//    return vec4(r, g, b, 1.0);
//}

vec4 combineColors(vec4 originColor, vec4 drawColor) {
    vec3 YUV = rgbToYuv(originColor);
    vec3 YUV1 = rgbToYuv(drawColor);
    highp float Y = YUV.x * YUV1.x * 2.0 / (YUV.x + YUV1.x);
    highp float U = YUV1.y;
    highp float V = YUV1.z;

    return yuvToRgb(vec3(Y, U, V));
}



vec4 conmbineColorsBT2020(vec4 originColor, vec4 drawColor) {
    highp float Y = 0.2256 * originColor.r + 0.5823 * originColor.g + 0.05093 * originColor.b + 16.0;
    highp float Y1 = 0.2256 * drawColor.r + 0.5823 * drawColor.g + 0.05093 * drawColor.b + 16.0;
    highp float U = -0.1222 * drawColor.r - 0.3154 * drawColor.g + 0.4375 * drawColor.b + 128.0;
    highp float V = 0.4375 * drawColor.r - 0.4023 * drawColor.g - 0.0352 * drawColor.b + 128.0;

    Y = (Y1 * Y + Y * Y1) / (Y1 + Y) - 0.3;

    highp float r = 1.164 * (Y - 16.0) + 1.6853 * (V - 128.0);
    highp float g = 1.164 * (Y - 16.0) - 0.1881 * (U - 128.0) - 0.6529 * (V - 128.0);
    highp float b = 1.164 * (Y - 16.0) + 2.1501 * (U - 128.0);
    r = clamp(r, 0.0, 1.0);
    g = clamp(g, 0.0, 1.0);
    b = clamp(b, 0.0, 1.0);

    return vec4(r, g, b, 1.0);
}

bool isEques(const vec4 colorOne, const vec4 colorTwo) {
    vec2 colorDist = abs(vec2(colorOne.r, colorOne.g) - vec2(colorTwo.r, colorTwo.g));
    return all(lessThan(colorDist, vec2(DX)));
}

void main() {
    vec4 orgColor = texture2D(uOrgTexture, vTextureCoord);
    vec4 maskColor = texture2D(uMaskTexture, vTextureCoord);
    if (isEques(maskColor, uSelectMaskColor)) {
        gl_FragColor = combineColors(orgColor, uSelectColor);
    } else {
        gl_FragColor = vec4(0.0);
    }
}