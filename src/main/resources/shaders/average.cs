#version 430 core

layout (binding = 0, rgba32f) uniform image2D source;
layout (binding = 1, rgba32f) uniform image2D dest;

uniform ivec2 samples;

layout (local_size_x = 32, local_size_y = 32) in;
void main() {
    
    vec4 color = vec4(0);
    float total = samples.x * samples.y;
    
    for (int i = 0; i < samples.x; i++) {
        for (int j = 0; j < samples.y; j++) {
            ivec2 coord = ivec2(gl_GlobalInvocationID.xy) * samples + ivec2(i, j);
            color += imageLoad(source, coord) / total;
        }
    }
    
    imageStore(dest, ivec2(gl_GlobalInvocationID.xy), color);
    
}