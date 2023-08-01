#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision highp float;

in vec2 textureCoordinate;
out vec4 outColor;
uniform samplerExternalOES inputImageTexture;

void main() {
     outColor = texture(inputImageTexture, textureCoordinate);
}

