precision mediump float;

varying vec2 vTexCoord;
uniform sampler2D uTexture;
uniform vec4 uSelectColor;

void main() {
    vec4 mask = texture2D(uTexture, gl_PointCoord);
    // 如果alpha值太小，直接丢弃该片段
    if (mask.a < 1.0) {
        discard;
    }
    
    gl_FragColor = vec4(uSelectColor.rgb, mask.a * uSelectColor.a);
}