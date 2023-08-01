#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision highp float;

in vec2 textureCoordinate;
out vec4 outColor;
uniform samplerExternalOES inputImageTexture;

void main() {
    vec2 uv = (textureCoordinate - 2 ) * 1.3;
//      outColor = texture(inputImageTexture, textureCoordinate);
    outColor = texture(inputImageTexture, uv);

}

