#version 460 core
#extension GL_ARB_bindless_texture : require

in vec2 fragUV;
in vec4 vertColor;
flat in int atlasIndex;

out vec4 fragColor;

layout(bindless_sampler) uniform sampler2D atlases[2];

uniform vec4 uniformColor;
uniform bool useMask;
uniform bool useUniformColor;

void main() {
    vec4 texColor = texture(atlases[atlasIndex], fragUV);
    if (useMask) fragColor = vec4(uniformColor.rgb, texColor.a) * vertColor;
    else if (useUniformColor) fragColor = uniformColor * vertColor;
    else fragColor = texColor * vertColor;
}