#version 460 core

in vec2 fragUV;
out vec4 fragColor;

uniform sampler2D uiTexture;
uniform vec4 color;
uniform bool useColor;

void main() {
    fragColor = useColor ? color : texture(uiTexture, fragUV);
}
