#version 460 core

layout (location = 0) in vec3 attrib_Position;
layout (location = 1) in vec2 attrib_TexCoord;
layout (location = 2) in float attrib_AO;

out vec2 texCoord;
out float vertexAO;

uniform mat4 view;
uniform mat4 model;
uniform mat4 projection;

void main() {
    gl_Position = projection * view * model * vec4(attrib_Position, 1.0);
    texCoord = attrib_TexCoord;
    vertexAO = attrib_AO;
}