varying vec3 vColor;
varying vec2 vPosition;

void main() {

    vec2 center = vec2(0.0, 0.0); // 圆心坐标，这里假设中心点在原点
    float distance = distance(vPosition.xy, center); // 计算片元与圆心的距离

    float radius = 0.5; // 圆的半径，根据你的需要调整
    float smoothRadius = 0.1; // 平滑半径，控制渐变过渡的范围

    float factor = smoothstep(radius - smoothRadius, radius, distance); // 使用 smoothstep 函数进行平滑插值

    vec4 color = mix(vec4(vColor, 1.0), vec4(0.0, 0.0, 0.0, 0.0), factor); // 根据 factor 进行颜色混合

//    gl_FragColor = distance > 1.0 ? vec4(1.0, 0.0, 0.0, 1.0) : vec4(1.0, 1.0, 0.0, 1.0); // 输出颜色
    gl_FragColor = vec4(1.0, 0.0, 0.0, abs(vPosition.x) - 0.9);
}