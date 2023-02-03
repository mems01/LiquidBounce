#version 330

in vec2 vertexUv;
in vec4 vertexColor;
out vec4 fragColor;

uniform sampler2D currentTexture;

void main() {
    fragColor = texture(currentTexture, vertexUv) * vertexColor;
}
