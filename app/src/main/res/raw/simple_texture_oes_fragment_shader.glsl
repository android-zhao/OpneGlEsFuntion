#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 v_TextureCoordinates;
uniform samplerExternalOES u_TextureOESUnit;
void main()
{
    vec4 color = texture2D(u_TextureOESUnit, v_TextureCoordinates);

    gl_FragColor = vec4(color.r,color.g,color.b,0.5);
}
