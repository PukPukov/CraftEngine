#version 460 core

in vec2 vUV;
in float vAO;
out vec4 color;

uniform sampler2D tex;

void main() {
    vec4 albedo = texture(tex, vUV);
    color = vec4(albedo.rgb * vAO, albedo.a);
}