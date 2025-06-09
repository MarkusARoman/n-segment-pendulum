#version 330 core
out vec4 FragColor;

uniform vec2 u_resolution;
uniform vec2 u_c;

const int max_iter = 300;

void main() {
    vec2 uv = (gl_FragCoord.xy / u_resolution) * 2.0 - 1.0;
    uv.x *= u_resolution.x / u_resolution.y;

    // Basic Julia set
    vec2 z = uv;
    int i;
    for (i = 0; i < max_iter; i++) {
        float x = (z.x * z.x - z.y * z.y) + u_c.x;
        float y = (2.0 * z.x * z.y) + u_c.y;
        z = vec2(x, y);
        if (dot(z, z) > 4.0) break;
    }

    // Smooth iteration count using log
    float m = dot(z, z);
    float logIter = i < max_iter ? float(i) - log2(log2(m)) + 4.0 : float(max_iter);
    float norm = clamp(logIter / float(max_iter), 0.0, 1.0);

    // Logarithmic gradient
    vec3 colorA = vec3(0.0, 0.0, 0.1);
    vec3 colorB = vec3(1.0, 0.8, 0.5);
    vec3 color = mix(colorA, colorB, sqrt(norm));

    FragColor = vec4(color, 2.0);
}
