precision mediump float;

varying vec4 v_Color;
uniform sampler2D u_Texture;

void main()
{
    vec4 texColor = texture(u_Texture,v_texCoord);
    gl_FragColor = texColor;
}