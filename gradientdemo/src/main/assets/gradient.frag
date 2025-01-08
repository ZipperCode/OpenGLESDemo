precision mediump float;

// 从顶点着色器传入的变量
varying vec4 vPosition;

// 点的数量
uniform int uPointCount;

// 原始传入的变量
uniform vec4 uColor;
uniform vec2 uPoint;
uniform vec4 uRColor;
uniform vec2 uRPoint;
uniform vec4 uRColor2;
uniform vec2 uRPoint2;
float squaredDistance(vec2 a, vec2 b) {
    return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
}

vec4 process2Point() {
    float d1 = distance(vPosition.xy, uPoint);
    float d2 = distance(vPosition.xy, uRPoint);
    if (d1 == 0.0 || d2 == 0.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);
    }
    float ratio = d1 / (d1 + d2);
    return mix(uColor, uRColor, smoothstep(0.0, 1.0, ratio));
}

vec4 process3Point() {
    // 三个点之间的长度
    float d1 = distance(vPosition.xy, uPoint);
    float d2 = distance(vPosition.xy, uRPoint);
    float d3 = distance(vPosition.xy, uRPoint2);
    if (d1 == 0.0 || d2 == 0.0 || d3 == 0.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);
    }
    // 颜色1到颜色2的比例
    float ratio = d1 / (d1 + d2);
    vec4 color1 = mix(uColor, uRColor, smoothstep(0.0, 1.0, ratio));
    // 颜色2到颜色3的比例
    ratio = d2 / (d2 + d3);
    vec4 color2 = mix(uRColor, uRColor2, smoothstep(0.0, 1.0, ratio));
    // 颜色3到颜色1的比例
    ratio = d3 / (d3 + d1);
    vec4 color3 = mix(uRColor2, uColor, smoothstep(0.0, 1.0, ratio));

    // Calculate weights based on inverse distance

    float w1 = 1.0 / (d1 );
    float w2 = 1.0 / (d2 );
    float w3 = 1.0 / (d3 );

    // 计算每个点的权重
    float total = w1 + w2 + w3;
    w1 /= total;
    w2 /= total;
    w3 /= total;

    // 根据权重混合颜色
    return w1 * color1 + w2 * color2 + w3 * color3;
}

void main() {
    if (uPointCount == 1) {
        float d1 = distance(vPosition.xy, uPoint);
        if (d1 == 0.0) {
            gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
        } else {
            gl_FragColor = uColor;
        }
    } else if (uPointCount == 2) {
        gl_FragColor = process2Point();
    } else if (uPointCount == 3) {
        gl_FragColor = process3Point();
    }
}