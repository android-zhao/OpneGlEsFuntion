//
// Created by didi on 2023/6/14.
//

#include "ShaderTopTest.h"
#include "GLUtils.h"
#include <gtc/matrix_transform.hpp>
#include <time.h>


void ShaderTopTest::Init() {
    if(m_ProgramObj){
        LogutilsI("ShaderTopTest::Init m_ProgramObj exit ");
        return;
    }
    char fShaderStr[] = "#version 300 es\n"
                        "precision mediump float;\n"
                        "\n"
                        "layout(location = 0) out vec4 outColor;\n"
                        "\n"
                        "uniform vec2 iResolution;\n"
                        "uniform float iTime;\n"
                        "in vec2 v_texCoord;\n"
                        "\n"
                        "//得出一个颜色 通过 函数 a + b*cos( 6.28318*(c*t+d) ) 输出一个颜色值;\n"
                        "vec3 palette( float t ) {\n"
                        "    vec3 a = vec3(0.5, 0.5, 0.5);\n"
                        "    vec3 b = vec3(0.5, 0.5, 0.5);\n"
                        "    vec3 c = vec3(1.0, 1.0, 1.0);\n"
                        "    vec3 d = vec3(0.263,0.416,0.557);\n"
                        "\n"
                        "    return a + b*cos( 6.28318*(c*t+d) );\n"
                        "}\n"
                        "\n"
                        "vec4 mainImage( out vec4 fragColor, in vec2 fragCoord ) {\n"
                        "    fragColor = vec4(1.0,1.0,1.0,1.0);\n"
                        "    //创建uv坐标和将uv坐标转移到屏幕中间，通过将原来纹理坐标的中心在左下角的（0,0） ---数学变化----变化到屏幕中心，和顶点坐标的中心进行了重合\n"
                        "    vec2 uv = fragCoord;\n"
                        "    uv = (uv - 0.5 )*2.0;\n"
                        "    //todo  不是很理解此种行为为何能实现不把图像拉伸\n"
                        "    //解答：---> 因为将纹理坐标全映射成屏幕中心为坐标原点的话，即相当于把纹理贴到一个正方形上面，实际上屏幕的长宽比 是不一样的即\n"
                        "    //有可能是 u > v 有可能是  此时将原来的u 坐标范围从 （-1，1 ）乘以 长宽比 ，即将坐标转换到了 （- iResolution.x/iResolution.y ~ iResolution.x/iResolution.y） 之间\n"
                        "\n"
                        "    uv.x *= iResolution.x/iResolution.y;//为了防止屏幕的宽高比对绘制的有影响，对uv坐标进行变化，\n"
                        "\n"
                        "\n"
                        "    vec2 uv0 = fragCoord ;\n"
                        "    vec3 finnalCol = vec3(0.0);\n"
                        "\n"
                        "\n"
                        "\n"
                        "    for(float i = 0.0;i<2.0;i++){\n"
                        "        //下面3步主要作用是将 fract分形之后的，将每一个分型小区域内部的原点调整成它自身内部的中间值\n"
                        "        // 把uv坐标调整到(0 ~4)之间,那么小数部分就会出现 4次 因此屏幕上会出现 4个 图像\n"
                        "        uv *= 2.0;\n"
                        "        //新的内置函数，作用是将输入的值输出位小数部分，去掉整数部分\n"
                        "        //即 输入为  0 -1 之间的小数  输出位0 -1之间的小数，输入为 0-2之间的小数 输出位 0- 2之间的小数\n"
                        "        uv = fract(uv);\n"
                        "        //对目前的uv 坐标 做 -0.5 的动作，将每一个小数部分的中心转移到当前区域的中心\n"
                        "        uv = uv - 0.5;\n"
                        "\n"
                        "        //计算每一个像素点到（0，0）的长度，并将这个值赋给 fragColor --->得到了一个 从屏幕中心到周围的灰度渐变图\n"
                        "        //float d = length(uv) * exp( -length(uv0) );\n"
                        "        float d = length(uv) ;\n"
                        "        //先对最原始的d的值 进行一个生成 颜色的操作\n"
                        "        //    vec3 col = palette(d);\n"
                        "\n"
                        "        // 对输入的d 增加一个随着时间递增 的一个变化，会让col 产生一个周期性变化\n"
                        "        vec3 col = palette(length(uv0) + i *.4  + iTime *.4);\n"
                        "\n"
                        "        //对d进行自减操作，  负数也会变成黑色，因此经过次步操作得到的黑色圆圈变大\n"
                        "        d = sin(d *8. +iTime )/8.;\n"
                        "        //取完绝对值之后，黑色中心最小的负数也会变成正数，但是非常接近d-0.15= 0的周边越黑 稍微远离就会逐渐变白\n"
                        "        d = abs(d);\n"
                        "\n"
                        "        //staep函数参数2个，前一个参数是阈值，后一个参数是变量，当后面这个值大于前面的阈值时就取1 否则取0 ，因此就成了\n"
                        "        //当距离屏幕中心点的距离为0.15的一圈圆上在画一圈宽度为 0.01的圆圈,使用step会很尖锐，可替换成smoothstep\n"
                        "        //    d = step(0.01,d);\n"
                        "\n"
                        "        //将小于0.0的 全部赋值为0，大于0.1的全部赋值为1，,0 -0.1之间的平滑的赋值\n"
                        "        //    d  = smoothstep(0.0,0.1,d);\n"
                        "\n"
                        "        d  = 0.02/d ;\n"
                        "\n"
                        "        //d = pow(0.01/d,1.2);\n"
                        "\n"
                        "        //  对上面生成的颜色值 col 进行随着时间变化 做乘法运算 d做完倒数运算 之后的取值范围在（0 -1），对生成的 col 在做一个乘法运算\n"
                        "        //    col *= d;\n"
                        "        finnalCol += col *d;\n"
                        "    }\n"
                        "    fragColor = vec4(finnalCol,1.0);\n"
                        "    return fragColor;\n"
                        "}\n"
                        "\n"
                        "\n"
                        "void main() {\n"
                        "    vec4 fragColor =  vec4(1.0,0.0,0.0,0.0);\n"
                        "    mainImage(fragColor,v_texCoord);\n"
                        "    outColor = fragColor;\n"
                        "}";
    m_ProgramObj = GLUtils::CreateProgram(vertexArray,fShaderStr);
    if(!m_ProgramObj){
        LogutilsI("ShaderTopTest::CreateProgram failed ");
    }

    GLfloat verticesCoords[] = {
            -1.0f,  1.0f, 0.0f,  // Position 0
            -1.0f, -1.0f, 0.0f,  // Position 1
            1.0f,  -1.0f, 0.0f,  // Position 2
            1.0f,   1.0f, 0.0f,  // Position 3
    };

//    GLfloat textureCoords[] = {
//            0.0f,  0.0f,        // TexCoord 0
//            0.0f,  1.0f,        // TexCoord 1
//            1.0f,  1.0f,        // TexCoord 2
//            1.0f,  0.0f         // TexCoord 3
//    };

    GLfloat textureCoords[] = {
            0.0f,  1.0f,        // TexCoord 0
            0.0f,  0.0f,        // TexCoord 1
            1.0f,  0.0f,        // TexCoord 2
            1.0f,  1.0f         // TexCoord 3
    };
    GLushort indices[] = {
            0, 1, 2,
            0, 2, 3
    };

    //生成buffer
    glGenBuffers(3,mVboId);

    //将VBO和ebo 绑定到 VAO上

    //指定vboid【0】为顶点坐标
    glBindBuffer(GL_ARRAY_BUFFER,mVboId[0]);
    glBufferData(GL_ARRAY_BUFFER,sizeof (verticesCoords),verticesCoords,GL_STATIC_DRAW);

    //绑定纹理vbo
    glBindBuffer(GL_ARRAY_BUFFER,mVboId[1]);
    glBufferData(GL_ARRAY_BUFFER,sizeof (textureCoords),textureCoords,GL_STATIC_DRAW);

    //绑定索引到vbo上
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,mVboId[2]);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER,sizeof (indices),indices, GL_STATIC_DRAW);

    //创建vao
    glGenVertexArrays(1,&mVaoId);
    glBindVertexArray(mVaoId);

    //将vao和VBO绑定
    glBindBuffer(GL_ARRAY_BUFFER,mVboId[0]);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0,3,GL_FLOAT,GL_FALSE,3 * sizeof (GL_FLOAT),(const void *)0);
    glBindBuffer(GL_ARRAY_BUFFER,GL_NONE);

    glBindBuffer(GL_ARRAY_BUFFER,mVboId[1]);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1,2,GL_FLOAT,GL_FALSE,2 * sizeof (GL_FLOAT),(const void *)0);
    glBindBuffer(GL_ARRAY_BUFFER,GL_NONE);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,mVboId[2]);
    glBindVertexArray(GL_NONE);

    //api 查看说是改变像素的存取方式，不是特别理解
    glPixelStorei(GL_UNPACK_ALIGNMENT,1);

//    glPixelStorei(GL_UNPACK_ALIGNMENT,1);
    LogutilsI("ShaderTopTest::Init  end");
}

void ShaderTopTest::Draw(int screenWidth, int screenHeight) {
    LogutilsI("ShaderTopTest Draw  begin screenWidth:%d ,screenHeight:%d",screenWidth,screenHeight);
    if(m_ProgramObj == GL_NONE){
        LogutilsI("ShaderTopTest Draw  m_ProgramObj is null ");
        return;
    }
//    long  timeResult = getCurrentTime();
//    float iTimeResult = timeResult * 1.0f;
    iTimeOffet += 0.01f;
    UpdateMVPMatrix(mMvpMatrix,0,0,(float)(screenWidth/screenHeight));
    glUseProgram(m_ProgramObj);
    glBindVertexArray(mVaoId);


    LogutilsI("ShaderTopTest Draw  iTimeOffet:%f ",iTimeOffet);
    glUniformMatrix4fv(mMvpLocation,1,GL_FALSE,&mMvpMatrix[0][0]);
    GLUtils::setVec2(m_ProgramObj, "iResolution", screenWidth, screenHeight);
    GLUtils::setFloat(m_ProgramObj, "iTime", iTimeOffet);

    glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,(const void *)0);

}

void ShaderTopTest::Destroy() {

}

void ShaderTopTest::UpdateTransformMatrix(float rotateX,float rotateY,float scaleX,float scaleY) {
    LogutilsI("ShaderTopTest::UpdateMVPMatrix ");


//
//    //投影矩阵 P矩阵 (正交投影 和 透视投影  正交投影 api 只有 ortho 透视投影 api有 frustum/perspective )
//    glm::mat4 projectM =  glm::ortho(-1.0f,1.0f,-1.0f,1.0f,0.1f,100.0f);
//
//    //透视矩阵 效果出现近大远小的效果
//    //glm::mat4 Projection = glm::frustum(-ratio, ratio, -1.0f, 1.0f, 4.0f, 100.0f);
//
//    //参数1：表示Y轴上的夹角大小，第二个参数代码代码x轴上的夹角，在手机屏幕上 其实就是宽和高的比 ，即界面控件的屏幕宽和高比值
//    //glm::mat4 Projection = glm::perspective(45.0f,ratio, .0.1f,100f);
//
//    //相机矩阵，即View 矩阵
//    glm::mat4 viewM = glm::lookAt(
//            glm::vec3(0,0,4), // 相机位置 或者eye的位置，z轴方向上
//            glm::vec3(0,0,0),//中心位置，也就是眼睛看的中心点，通过相机位置和中心点位置可以确定 camera 的看的角度和方向
//            glm::vec3(0,1,0) // 向上的向量，指定此向量之后 opengl 就可以自己计算出来 真正的View矩阵
//    );
//
//    glm :: mat4  moduleM = glm::mat4(1.0f);
//    moduleM = glm::scale(moduleM,glm::vec3(mScaleX, mScaleY, 1.0f));
//    moduleM = glm::rotate(moduleM,radiansX,glm::vec3(1.0f,0.0f,0.0f));
//    moduleM = glm::rotate(moduleM,radiansY,glm::vec3(0.0f,1.0f,0.0f));
//
//    moduleM = glm::translate(moduleM,glm::vec3(0.0f,0.0f,0.0f));
//
//    mvpMatrix = projectM*viewM*moduleM;
}



ShaderTopTest::~ShaderTopTest() {
    LogutilsI("~ShaderTopTest constructor");
    vertexArray = nullptr;
}

ShaderTopTest::ShaderTopTest() {
    LogutilsI("ShaderTopTest constructor");
    mMvpLocation = GL_NONE;
    //指定opengl 的vs shader 脚本

    vertexArray =  "#version 300 es\n"
                   "layout(location = 0) in vec4 a_position;\n"
                   "layout(location = 1) in vec2 a_texCoord;\n"
                   "uniform mat4 u_MVPMatrix;\n"
                   "out vec2 v_texCoord;\n"
                   "void main()\n"
                   "{\n"
                   "    gl_Position = u_MVPMatrix * a_position;\n"
                   "    v_texCoord = a_texCoord;\n"
                   "}";


    mVaoId = GL_NONE;
    mMvpLocation = GL_NONE;
    mCurrentTime = 0;
    iTimeOffet = 1.0;
}

void ShaderTopTest::UpdateMVPMatrix(glm::mat4 &mvpMatrix, int angleX, int angleY, float ration) {
    LogutilsI("ShaderTopTest::UpdateMVPMatrix angleX = %d, angleY = %d, ratio = %f", angleX, angleY, ration);

    angleX = angleX%360;
    angleY = angleY%360;

//    角度转弧度
    float  radiansX = (MATH_PI /180.0f * angleX);
    float  radiansY = (MATH_PI /180.0f * angleY);

    //投影矩阵 P矩阵 (正交投影 和 透视投影  正交投影 api 只有 ortho 透视投影 api有 frustum/perspective )
    glm::mat4 projectM =  glm::ortho(-1.0f,1.0f,-1.0f,1.0f,0.1f,100.0f);

    //透视矩阵 效果出现近大远小的效果
    //glm::mat4 Projection = glm::frustum(-ratio, ratio, -1.0f, 1.0f, 4.0f, 100.0f);

    //参数1：表示Y轴上的夹角大小，第二个参数代码代码x轴上的夹角，在手机屏幕上 其实就是宽和高的比 ，即界面控件的屏幕宽和高比值
    //glm::mat4 Projection = glm::perspective(45.0f,ratio, .0.1f,100f);

    //相机矩阵，即View 矩阵
    glm::mat4 viewM = glm::lookAt(
            glm::vec3(0,0,4), // 相机位置 或者eye的位置，z轴方向上
            glm::vec3(0,0,0),//中心位置，也就是眼睛看的中心点，通过相机位置和中心点位置可以确定 camera 的看的角度和方向
            glm::vec3(0,1,0) // 向上的向量，指定此向量之后 opengl 就可以自己计算出来 真正的View矩阵
    );

    glm :: mat4  moduleM = glm::mat4(1.0f);
    moduleM = glm::scale(moduleM,glm::vec3(1.0f, 1.0f, 1.0f));
    moduleM = glm::rotate(moduleM,radiansX,glm::vec3(1.0f,0.0f,0.0f));
    moduleM = glm::rotate(moduleM,radiansY,glm::vec3(0.0f,1.0f,0.0f));

    moduleM = glm::translate(moduleM,glm::vec3(0.0f,0.0f,0.0f));

    mvpMatrix = projectM*viewM*moduleM;
}

