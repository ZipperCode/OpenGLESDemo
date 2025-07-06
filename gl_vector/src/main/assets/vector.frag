uniform sampler2D uTexture;


varying vec2 vTexCoordinate;
float rgbToLuminance(vec3 rgb) {
    return dot(rgb, vec3(0.2126, 0.7152, 0.0722));
}
void main() {
    vec4 color = texture2D(uTexture, vTexCoordinate);
    float luminance = clamp(rgbToLuminance(color.rgb), 0.0, 1.0);
    gl_FragColor = luminance == 0.0 ? vec4(0.0, 0.0, 0.0, 1.0) : luminance == 1.0 ? vec4(0.0): vec4(0.0, 0.0, 0.0, 1.0 - luminance);
}