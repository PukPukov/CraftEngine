#version 460 core

in vec2  fragUV;
in vec4  vertColor;
out vec4 fragColor;

uniform sampler2D uiTexture;
uniform vec4     uniformColor;
uniform bool     useUniformColor;
uniform bool     useMask;

void main() {
    if (useMask) {
        float a = texture(uiTexture, fragUV).a;
        fragColor = vec4(uniformColor.rgb, a) * vertColor;
    }
    else if (useUniformColor) {
        fragColor = uniformColor * vertColor;
    }
    else {
        fragColor = texture(uiTexture, fragUV) * vertColor;
    }
}
