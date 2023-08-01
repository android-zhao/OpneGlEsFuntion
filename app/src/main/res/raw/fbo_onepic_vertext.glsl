attribute vec4 a_Position;
attribute vec2 vCoord;
uniform mat4 vMatrix;

varying vec2 textureCoordinate;

void main() {
    gl_Position = vMatrix*a_Position;
    textureCoordinate = vCoord;
}
