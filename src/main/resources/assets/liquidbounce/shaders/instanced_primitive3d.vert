#version 330

in vec3 inPos;
in vec4 inColor;

in vec3 instancePos;
in vec4 instanceColor;

out vec4 vertexColor;

uniform mat4 mvpMatrix;

void main() {
    vertexColor = inColor * instanceColor;

    gl_Position = mvpMatrix * vec4(inPos + instancePos, 1.0);
}
