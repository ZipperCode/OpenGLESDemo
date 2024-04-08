precision highp float;



uniform sampler2D uMaskTexture;
uniform sampler2D uOrgTexture;
uniform vec4 uSelectColor;
uniform vec4 uSelectMaskColor;

varying vec2 vTextureCoord;

const float DX = 0.003922;

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
    highp float Y = 0.2627 * originColor.r + 0.6780 * originColor.g + 0.0593 * originColor.b + 16.0;
    highp float U = -0.1396 * originColor.r - 0.3604 * originColor.g + 0.5 * originColor.b + 128.0;
    highp float V = 0.5 * originColor.r - 0.4818 * originColor.g - 0.0182 * originColor.b + 128.0;

    highp float Y1 = 0.2627 * drawColor.r + 0.6780 * drawColor.g + 0.0593 * drawColor.b + 16.0;
    U = -0.1396 * drawColor.r - 0.3604 * drawColor.g + 0.5 * drawColor.b + 128.0;
    V = 0.5 * drawColor.r - 0.4818 * drawColor.g - 0.0182 * drawColor.b + 128.0;

    // Y = (Y1 * Y + Y * Y1) / (Y1 + Y);
//    Y = (Y1 + Y ) / 2.0;

    highp float b = 1.164 * (Y - 16.0) + 2.018 * (U - 128.0);
    highp float g = 1.164 * (Y - 16.0) - 0.813 * (V - 128.0) - 0.391 * (U - 128.0);
    highp float r = 1.164 * (Y - 16.0) + 1.596 * (V - 128.0);
    return vec4(r, g, b, 1.0);
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

//vec4 combineColors(vec4 originColor, vec4 drawColor) {
//    highp float r1 = originColor.r * 255;
//    highp float g1 = originColor.g * 255;
//    highp float b1 = originColor.b * 255;
//
//    highp float y1 = ((66 * r1 + 129 * g1 + 25 * b1 + 128) >> 8) + 16;
//
//    highp float r2 = drawColor.r * 255;
//    highp float g2 = drawColor.g * 255;
//    highp float b2 = drawColor.b * 255;
//    highp float y2 = ((66 * r2 + 129 * g2 + 25 * b2 + 128) >> 8) + 16;
//    highp float u2 = ((-38 * r2 - 74 * g2 + 112 * b2 + 128) >> 8) + 128;
//    highp float v2 = ((112 * r2 - 94 * g2 - 18 * b2 + 128) >> 8) + 128;
//
//    highp float Y = (y1 + y2) / 2.0;
//
//    highp float r = 298 * (Y - 16.0) + 459 * (U - 128.0);
//    highp float g = 298 * (Y - 16.0) - 91 * (V - 128.0) - 45 * (U - 128.0);
//    highp float b = 298 * (Y - 16.0) + 230 * (V - 128.0);
//    r = clamp(r / 255.0, 0.0, 1.0);
//    g = clamp(g / 255.0, 0.0, 1.0);
//    b = clamp(b / 255.0, 0.0, 1.0);
//    return vec4(r, g, b, 1.0);
//}

bool isEques(const vec4 colorOne, const vec4 colorTwo) {
    vec2 colorDist = abs(vec2(colorOne.r, colorOne.g) - vec2(colorTwo.r, colorTwo.g));
    return all(lessThan(colorDist, vec2(DX)));
}

void main() {
    vec4 orgColor = texture2D(uOrgTexture, vTextureCoord);
    vec4 maskColor = texture2D(uMaskTexture, vTextureCoord);
    if (isEques(maskColor, uSelectMaskColor)) {
        gl_FragColor = conmbineColorsBT2020(orgColor, uSelectColor);
    } else {
        gl_FragColor = vec4(0.0);
    }
}