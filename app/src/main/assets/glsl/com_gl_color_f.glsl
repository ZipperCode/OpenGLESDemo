precision mediump float;
uniform vec4 bg_Color;
void main() {
    gl_FragColor = vec4(bg_Color.r, bg_Color.g, bg_Color.b, bg_Color.a);
}
