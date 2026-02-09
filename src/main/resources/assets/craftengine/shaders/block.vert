#version 460 core
#extension GL_ARB_bindless_texture : require

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec4 inColor;
layout(location = 3) in int inAtlas;
layout(location = 4) in vec3 inNormal;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

out vec2 fragUV;
out vec4 vertColor;
flat out int atlasIndex;
out vec3 fragNormal;

void main() {
    fragUV = inUV;
    vertColor = inColor;
    atlasIndex = inAtlas;

    fragNormal = mat3(transpose(inverse(model))) * inNormal;
    gl_Position = projection * view * model * vec4(inPos, 1.0);
}
