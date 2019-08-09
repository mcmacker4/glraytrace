#version 420 core

in vec2 _uvcoords;
out vec4 FragColor;

uniform sampler2D tex;

void main() {
    FragColor = texture(tex, _uvcoords);
}