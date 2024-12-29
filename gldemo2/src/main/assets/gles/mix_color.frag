precision highp float;
uniform sampler2D uBaseTexture;
uniform sampler2D uPaintTexture;

varying vec2 vTexCoordinate;

float ratio = 0.5;

void main() {
    vec4 baseColor = texture2D(uBaseTexture, vTexCoordinate);
    vec4 paintColor = texture2D(uPaintTexture, vTexCoordinate);
    if (baseColor.a == 0.0 && paintColor.a == 0.0) {
        discard;
    } else if (baseColor.a == 0.0) {
        gl_FragColor = paintColor;
    } else if (paintColor.a == 0.0) {
        gl_FragColor = baseColor;
    } else {
        gl_FragColor = mix(baseColor, paintColor, ratio);
    }
}