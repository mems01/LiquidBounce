#version 330

in vec3 inPos;
in vec4 inColor;

out vec4 vertexColor;

uniform mat4 mvpMatrix;

void main() {
    vertexColor = inColor;

    gl_Position = mvpMatrix * vec4(inPos, 1.0);
}
