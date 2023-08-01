attribute vec4 a_Position;
attribute vec4 a_Color;

varying vec4 v_Color;
varying vec2 v_texCoord;

void main()
{

    v_Color = a_Color;
    v_texCoord = a_Position;
    gl_Position = a_Position;
}