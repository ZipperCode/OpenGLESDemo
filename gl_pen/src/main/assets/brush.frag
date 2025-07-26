precision highp float;
uniform sampler2D uBrushTexture;

uniform float uViewRatio;
uniform float uModelSize;

varying float vAngle;
varying vec3 vColor;

void main() {
    float vwh = 1.0 - uViewRatio / uModelSize;
    vec2 coord = vec2((gl_PointCoord.x - vwh * 0.5) / (1.0 - vwh), gl_PointCoord.y);
    if (coord.x < 0.0 || coord.x > 1.0) {
        discard;
    }
    float angleRad = vAngle * 3.141592653589793 / 180.0;
    float s = sin(angleRad);
    float c = cos(angleRad);
    coord = coord - vec2(0.5);
    mat2 rotationMatrix = mat2(c, -s, s, c);
    coord = rotationMatrix * coord + vec2(0.5);

    vec4 color = texture2D(uBrushTexture, coord);
    gl_FragColor = vec4(vColor.rgb, color.a);
}

