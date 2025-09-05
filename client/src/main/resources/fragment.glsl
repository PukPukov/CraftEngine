#version 460

in vec2 texCoord;
in float vertexAO;

out vec4 color;

uniform sampler2D tex;

void main() {
    color = texture(tex, texCoord);
    float ao = (vertexAO == 0.0) ? 1.0 : vertexAO;
    color.rgb *= ao;
}