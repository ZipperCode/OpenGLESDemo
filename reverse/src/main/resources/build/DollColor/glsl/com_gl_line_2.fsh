precision mediump float;
varying vec2 aCoordinate;
uniform sampler2D lineTexture;

void main() {
    vec4 maskColor = texture2D(lineTexture, aCoordinate);

    highp float lineA = maskColor.b;
    highp float minA = 50.0 / 255.0;
    highp float maxA = 100.0 / 255.0;
    float a = step(minA, lineA);//0,1
    a *= (lineA - minA) / (maxA - minA);
    gl_FragColor = vec4(0, 0, 0, a);
}





