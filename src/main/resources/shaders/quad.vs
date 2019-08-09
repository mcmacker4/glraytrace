#version 420 core

layout (location = 0) in vec2 pos;

out vec2 _uvcoords;

void main() {
    gl_Position = vec4(pos, 0, 1);
    _uvcoords = pos * 0.5 + 0.5;
}