
precision mediump float;
uniform vec4 bgColor;
void main() {
    gl_FragColor = vec4(bgColor.r, bgColor.g, bgColor.b, 1);
}
