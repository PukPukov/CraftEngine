#version 460 core
#extension GL_ARB_bindless_texture : require

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec4 inColor;
layout(location = 3) in int  inAtlas;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

out vec2 fragUV;
out vec4 vertColor;
flat out int atlasIndex;

void main() {
    fragUV = inUV;
    vertColor = inColor;
    atlasIndex = inAtlas;

    vec4 pos = vec4(inPos.xy, inPos.z, 1.0);
    gl_Position = projection * view * model * pos;
}
