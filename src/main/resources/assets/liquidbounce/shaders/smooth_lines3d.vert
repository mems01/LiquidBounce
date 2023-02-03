#version 330

in vec3 inPos;
in vec4 inColor;

out vec4 vertexColor;
out vec2 vertexLineCenter;

uniform mat4 mvpMatrix;
uniform vec2 viewPort;

void main(void) {
    vec4 pos = mvpMatrix * vec4(inPos, 1.0);

    gl_Position = pos;

    vertexLineCenter = 0.5 * (pos.xy + vec2(1, 1)) * viewPort;
    vertexColor = inColor;
}
