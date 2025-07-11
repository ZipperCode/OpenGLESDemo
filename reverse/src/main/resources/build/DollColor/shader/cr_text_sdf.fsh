
precision highp float;

varying float fNumber;
uniform sampler2D fTextureTextNumber;

int modMy(int x, int y) {
    return int(x - y * int(x / y));
}

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float sdNumber(vec3 rgb) {
    float sd = median(rgb.r, rgb.g, rgb.b);//0.5
    float maxS = 0.52;
    float minS = 0.48;
    float a = smoothstep(minS, maxS, sd);
    return a;
}

void main() {

    int numberInt = int(fNumber)+ 1;


    float n = 1.0;
    n += step(10.0, float(numberInt));
    n += step(100.0, float(numberInt));
    float ystart = (1.0 - 1.0 / n) * 0.5;
    float yend = ystart + 1.0 / n;
    float y = clamp(gl_PointCoord.y, ystart, yend);
    float numberTextureY = (y - ystart) / (yend - ystart);//0-1

    float position = 0.0;
    position += step(1.0 / n, gl_PointCoord.x);
    position += step(2.0 / n, gl_PointCoord.x);

    float xstart = position / n;
    float xend = (position + 1.0) / n;
    float numberTextureX = (gl_PointCoord.x - xstart) / (xend - xstart);//0-1

    float numberFloat = float(numberInt);
    float zhusu = max(n - position - 1.0, 0.0);
    float number = float(modMy(int(numberFloat / pow(10.0, zhusu)), 10));

    numberTextureX = (numberTextureX / 10.0 + number / 10.0);

    vec4 numberRGBA = texture2D(fTextureTextNumber, vec2(numberTextureX, numberTextureY));
    float alpha = sdNumber(numberRGBA.rgb);
    gl_FragColor = vec4(0., 0., 0., alpha);
}
