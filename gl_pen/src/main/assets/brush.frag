precision highp float;
uniform sampler2D uBrushTexture;

uniform float uViewRatio;
uniform float uModelSize;

varying vec3 vColor;

void main() {
    float vwh = 1.0 - uViewRatio / uModelSize;
    vec2 coord = vec2((gl_PointCoord.x - vwh * 0.5) / (1.0 - vwh), gl_PointCoord.y);
    if (coord.x < 0.0 || coord.x > 1.0) {
        discard;
    }
    vec4 color = texture2D(uBrushTexture, coord);
    gl_FragColor = vec4(vColor.rgb, color.a);
}

