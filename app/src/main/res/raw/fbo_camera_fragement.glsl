precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D vTexture;
void main() {
    vec4 color=texture2D( vTexture, textureCoordinate);
    vec4 c=vec4(rgb,rgb,rgb,1);
    gl_FragColor = c;
}
