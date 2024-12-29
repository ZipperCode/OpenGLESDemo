precision mediump float;

varying vec2 vTexCoord;
uniform sampler2D uTexture;
uniform vec4 uSelectColor;

void main() {
    vec4 mask = texture2D(uTexture, gl_PointCoord);
    gl_FragColor = vec4(uSelectColor.rgb, mask.a);
}