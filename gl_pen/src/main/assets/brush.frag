uniform sampler2D uBrushTexture;
uniform float uFboRatio;
uniform float uModelRatio;

varying vec3 vColor;

void main() {
    float vwh = 1.0 - uFboRatio / uModelRatio;
    vec4 color = texture2D(uBrushTexture, gl_PointCoord);
    gl_FragColor = vec4(vColor.rgb, color.a);
}