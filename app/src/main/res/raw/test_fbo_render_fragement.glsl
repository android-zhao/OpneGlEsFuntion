precision mediump float;

uniform sampler2D uTextureUnit;
varying vec2 vTexCoord;


void main() {
    vec4 color = texture2D(uTextureUnit, vTexCoord);
    float rgb = color.g;
    vec4 c = vec4(rgb, rgb, rgb, color.a);
    gl_FragColor = c;
}

