#version 300 es
precision mediump float;
uniform sampler2D uTexture;

in vec2 vTextureCoordinate;
out vec4 fragColor;

void main() {
    vec4 color = texture(uTexture, vTextureCoordinate);
    float luminance = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    if (luminance < 0.9) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        fragColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
}