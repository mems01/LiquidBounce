#version 330

in vec3 inPos;
in vec4 inColor;
in vec2 inUv;

out vec2 vertexUv;
out vec4 vertexColor;

uniform mat4 mvpMatrix;

void main() {
    vertexColor = inColor;
    vertexUv = inUv;

    gl_Position = mvpMatrix * vec4(inPos, 1.0);
}
