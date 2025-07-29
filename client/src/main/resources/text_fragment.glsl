#version 460 core
in vec2 fragUV;
out vec4 fragColor;
uniform sampler2D uiTexture;
void main() {
    float alpha = texture(uiTexture, fragUV).r;
    fragColor = vec4(1.0, 1.0, 1.0, alpha);
}