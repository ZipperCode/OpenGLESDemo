precision mediump float;
uniform sampler2D vTexture;
// 顶点传递的纹理坐标
varying vec2 aCoordinate;

void main() {
    // 取纹理颜色
    vec4 color = texture2D(vTexture, aCoordinate);
    gl_FragColor = color;
}