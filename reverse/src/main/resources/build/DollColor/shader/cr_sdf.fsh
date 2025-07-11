
precision highp float;
varying vec2 aCoordinate;

uniform sampler2D fTextureLine;
uniform sampler2D fTextureOrg;
uniform float colorLine;// 0,1;
uniform float scalePercent;// 0-1;

void main() {
    vec4 sdfRGBA = (texture2D(fTextureLine, aCoordinate));
    float p = 0.01 * scalePercent;

    float maxS = 0.53 + p;
    float minS = 0.5 + p;
    float a = smoothstep(minS, maxS, sdfRGBA.a);
    float alpha = 1. - a;

    vec4 orgRGBA = texture2D(fTextureOrg, aCoordinate);
        vec4 newOrg = (orgRGBA * orgRGBA);


    gl_FragColor = mix(vec4(0.0), vec4(newOrg.rgb * colorLine, 1.0), alpha);
}




