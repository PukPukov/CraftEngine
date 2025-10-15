#version 330 core
const vec2 V[3] = vec2[3]( vec2(-1,-1), vec2(3,-1), vec2(-1,3) );
out vec2 v_ndc; // [-1..1]
void main() {
    vec2 p = V[gl_VertexID];
    v_ndc = p;
    gl_Position = vec4(p, 0.0, 1.0);
}