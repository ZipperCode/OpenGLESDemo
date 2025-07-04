
uniform sampler2D uMaskTexture;
uniform vec4 uColor;
uniform vec4 uMaskColor;

varying vec2 vTexCoordinate;

const float threashold = 0.02;

void main() {
    vec4 maskColor = texture2D(uMaskTexture, vTexCoordinate);
    if (distance(maskColor, uMaskColor) <= threashold) {
        gl_FragColor = uColor;
    }
}