#version 300 es
precision mediump float;

uniform sampler2D uTexture;
uniform float uBlurRadius;
uniform vec2 uImageSize;
uniform bool uHorizontal;

const int MAX_KERNEL_SIZE = 61;
uniform float uKernel[MAX_KERNEL_SIZE];

in vec2 vTextureCoordinate;
out vec4 fragColor;

void main() {
    vec4 sum = vec4(0.0);
    float sumGray = 0.0;
    vec2 oneTexel = vec2(1.0) / uImageSize;
    int radiusInt = int(uBlurRadius);
    for (int i = 0; i < MAX_KERNEL_SIZE; i++) {
        if (i >= 2 * radiusInt + 1) break;
        int offsetIndex = i - radiusInt;
        vec2 offset = vec2(0.0);
        if (uHorizontal) {
            offset.x = float(offsetIndex) * oneTexel.x;
        } else {
            offset.y = float(offsetIndex) * oneTexel.y;
        }
        vec2 sampleCoord = clamp(vTextureCoordinate + offset, vec2(0.0), vec2(1.0));
        float gray = dot(texture(uTexture, sampleCoord).rgb, vec3(0.2126, 0.7152, 0.0722));
        sumGray += gray * uKernel[i];
    }
    fragColor = vec4(sumGray, sumGray, sumGray, 1.0);
}