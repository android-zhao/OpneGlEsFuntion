attribute vec2 a_TextureCoordinates;
attribute vec4 a_Position;

varying vec2 v_TextureCoordinates;

uniform float texelWidthOffset;
uniform float texelHeightOffset;

void main()
{
      v_TextureCoordinates = a_TextureCoordinates;
      gl_Position = a_Position;
}
