#version 460 core

layout(location=0) in vec3 aPos;
layout(location=1) in vec2 aUV;
layout(location=2) in float aAO;

uniform mat4 projection;
uniform mat4 view;

out vec2 vUV;
out float vAO;

void main() {
    gl_Position = projection * view * vec4(aPos, 1.0);
    vUV = aUV;
    vAO = clamp(aAO, 0.4, 1.0);
}