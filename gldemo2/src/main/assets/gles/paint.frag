precision highp float;
uniform sampler2D uTexture;
uniform vec4 outColor;

void main()
{
    vec4 mask = texture2D(uTexture, gl_PointCoord); // texture -> texture2D
    float aTransparent;
    if (mask.a < 1.0) {
        aTransparent = mask.a * outColor.a;
        gl_FragColor = aTransparent * (vec4(1.0) - ((vec4(1.0) - outColor)) * (vec4(1.0) - vec4(mask.rgb, 1.0)));
    }
    else {
        gl_FragColor = outColor.a * (vec4(1.0) - ((vec4(1.0) - vec4(outColor.rgb, 1.0))) * (vec4(1.0) - mask));
    }

}