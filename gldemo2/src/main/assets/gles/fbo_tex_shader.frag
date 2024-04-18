precision mediump float;

uniform sampler2D uTexture;
varying vec2 vTexCoordinate;

void main() {
    vec4 color = texture2D(uTexture, vTexCoordinate);
    gl_FragColor = color;
}