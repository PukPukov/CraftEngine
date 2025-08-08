#version 460 core

layout(location = 0) in vec2 inPos;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec4 inColor;

uniform mat4 projection;

out vec2  fragUV;
out vec4  vertColor;

void main() {
    fragUV    = inUV;
    vertColor = inColor;
    gl_Position = projection * vec4(inPos, 0.0, 1.0);
}
