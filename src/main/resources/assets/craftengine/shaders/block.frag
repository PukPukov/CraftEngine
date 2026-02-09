#version 460 core
#extension GL_ARB_bindless_texture : require

in vec2 fragUV;
in vec4 vertColor;
flat in int atlasIndex;

in vec3 fragNormal;
in vec3 fragPos;

out vec4 fragColor;

uniform vec3 lightDir;
uniform vec3 lightColor;

layout(bindless_sampler) uniform sampler2D atlases[2];

void main() {
    vec4 texColor = texture(atlases[atlasIndex], fragUV);
    vec3 light = (0.2 * lightColor + max(dot(normalize(fragNormal), normalize(-lightDir)), 0.0) * lightColor) * texColor.rgb * vertColor.rgb;
    fragColor = vec4(light, texColor.a);
}