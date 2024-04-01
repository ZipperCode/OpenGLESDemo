precision mediump float;
uniform sampler2D vTexture;
// 顶点传递的纹理坐标
varying vec2 aCoordinate;

void main() {
    // 取纹理颜色
    vec4 color = texture2D(vTexture, aCoordinate);

    highp float lineA = color.b;
    highp float minA = 50.0 / 255.0;
    highp float maxA = 100.0 / 255.0;
    float a = step(minA, lineA);//0,1
    a *= (lineA - minA) / (maxA - minA);
    gl_FragColor = vec4(0, 0, 0, a);
//    gl_FragColor = color;
}