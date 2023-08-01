#extension GL_OES_EGL_image_external : require
precision mediump float;

varying mediump vec2 textureCoordinate;
uniform samplerExternalOES inputImageTexture;

void main() {
    vec2 xy = textureCoordinate.xy;
    vec3 textureColor = texture2D(inputImageTexture, xy).rgb;
    gl_FragColor = vec4(textureColor.rgb,1.0);
}
