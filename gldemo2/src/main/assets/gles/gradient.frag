precision mediump float;

uniform vec2 uResolution;
uniform int uPointCount;
uniform vec2 uPoints[4];
uniform vec3 uColors[4];

varying vec2 vTexCoord;

float distanceToSegment(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h);
}

void main() {
    vec2 fragCoord = vTexCoord * uResolution;
    vec3 finalColor = vec3(0.0);
    
    if (uPointCount == 1) {
        // Single point - simple radial gradient
        float dist = distance(fragCoord, uPoints[0] * uResolution);
        float intensity = 1.0 - smoothstep(0.0, length(uResolution), dist);
        finalColor = uColors[0] * intensity;
    } else {
        // Multiple points - linear interpolation between points
        float totalWeight = 0.0;
        for (int i = 0; i < uPointCount - 1; i++) {
            vec2 p1 = uPoints[i] * uResolution;
            vec2 p2 = uPoints[i+1] * uResolution;
            
            float dist = distanceToSegment(fragCoord, p1, p2);
            float weight = 1.0 - smoothstep(0.0, length(uResolution)/2.0, dist);
            
            vec3 color = mix(uColors[i], uColors[i+1], 
                smoothstep(0.0, 1.0, distance(fragCoord, p1)/distance(p2, p1)));
                
            finalColor += color * weight;
            totalWeight += weight;
        }
        
        if (totalWeight > 0.0) {
            finalColor /= totalWeight;
        }
    }
    
    gl_FragColor = vec4(finalColor, 1.0);
}
