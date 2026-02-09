#version 330 core
in vec2 v_ndc;          // [-1..1]
out vec4 FragColor;

uniform samplerCube skybox;
uniform mat4 uInvProj;  // inverse(projection)
uniform mat4 uInvView;  // inverse(view with zeroed translation)

void main() {
    // луч из NDC в view space
    vec4 clip = vec4(v_ndc, 1.0, 1.0);
    vec4 view = uInvProj * clip;
    view /= view.w; // перспективное деление

    // в world space (w=0 для направления)
    vec3 dir = normalize( (uInvView * vec4(view.xyz, 0.0)).xyz );

    FragColor = texture(skybox, dir);
}
