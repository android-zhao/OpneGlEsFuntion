#version 300 es
layout(location = 0) in vec4 position;
// layout(location = 1) in vec2 a_texCoord;
//为什么使用vec4
layout(location = 1) in vec4 inputTextureCoordinate;
uniform mat4 textureTransform;
out vec2 textureCoordinate;
uniform mat4 mMatrix;//m矩阵
uniform mat4 vMatrix;//camera矩阵
uniform mat4 uMatrix;//p矩阵
void main()
{
    //处理纹理坐标进行mvp矩阵处理  一般矩阵处理的是position坐标 不处理纹理坐标
    textureCoordinate = (textureTransform * inputTextureCoordinate).xy;
//    gl_Position = uMatrix*vMatrix*mMatrix*position;
//    gl_Position = uMatrix*vMatrix*position;
     vec4 posiTem = mMatrix*position;
     gl_Position = uMatrix*posiTem;
}
