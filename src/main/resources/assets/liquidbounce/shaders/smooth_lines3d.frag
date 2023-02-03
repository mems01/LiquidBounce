#version 330

in vec4 vertexColor;
in vec2 vertexLineCenter;

out vec4 fragColor;

uniform float lineWidth;

const float blendFactor = 1.5;

void main() {
    vec4 col = vertexColor;

    float d = length(vertexLineCenter - gl_FragCoord.xy);
    float w = lineWidth;

    // FIXME: This does not work
    /*if (d > w)
        col.w = 0;
    else
        col.w *= pow(float((w - d) / w), blend_factor);*/

    fragColor = col;
}
