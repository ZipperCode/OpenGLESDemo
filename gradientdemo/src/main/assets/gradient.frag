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
    // 计算到每个点的距离
    float d1 = distance(vPosition.xy, uPoint);
    float d2 = distance(vPosition.xy, uRPoint);
    float d3 = distance(vPosition.xy, uRPoint2);

    // 避免除以零，加入一个小常数
    float w1 = 1.0 / pow(d1 + 0.001, 2.0); // 修改: 对距离进行幂运算以平滑权重
    float w2 = 1.0 / pow(d2 + 0.001, 2.0); // 修改: 对距离进行幂运算以平滑权重
    float w3 = 1.0 / pow(d3 + 0.001, 2.0); // 修改: 对距离进行幂运算以平滑权重

    // 计算总权重
    float total = w1 + w2 + w3;

    // 归一化权重
    w1 /= total;
    w2 /= total;
    w3 /= total;

    // 直接根据权重混合三个颜色
    return w1 * uColor + w2 * uRColor + w3 * uRColor2;
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