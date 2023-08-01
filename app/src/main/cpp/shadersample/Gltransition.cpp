//
// Created by didi on 2023/5/24.
//

#include "Gltransition.h"
#include "GLUtils.h"
#include "ImageDef.h"
#include <gtc/matrix_transform.hpp>


Gltransition::Gltransition(){
    LogutilsI("Gltransition constructor");
    mMvpLocation = GL_NONE;
    for (int i = 0; i < BF_IMG_NUM; ++i) {
        m_TextureIds[i] = GL_NONE;
    }
    mVaoId = GL_NONE;

    mAngleX = 0;
    mAngleY = 0;

    mScaleX = 1.0f;
    mScaleY = 1.0f;

    mFrameIndex = 0;
    mLoopCount = 0;

}

Gltransition::~Gltransition()  {
    for(int i = 0;i<BF_IMG_NUM;i++){
        NativeImageUtil::FreeNativeImage(&nativeImage[i]);
    }
}


void Gltransition::Init() {
    if(m_ProgramObj){
        LogutilsI("Gltransition::Init m_ProgramObj exit ");
        return;
    }

    for(int i = 0;i<BF_IMG_NUM;i++){
        glGenTextures(1,&m_TextureIds[i]);
        glBindTexture(GL_TEXTURE_2D,m_TextureIds[i]);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, GL_NONE);
    }

    char vShaderStr[] =
            "#version 300 es\n"
            "layout(location = 0) in vec4 a_position;\n"
            "layout(location = 1) in vec2 a_texCoord;\n"
            "uniform mat4 u_MVPMatrix;\n"
            "out vec2 v_texCoord;\n"
            "void main()\n"
            "{\n"
            "    gl_Position = u_MVPMatrix * a_position;\n"
            "    v_texCoord = a_texCoord;\n"
            "}";

    char fShaderStr[] =
            "#version 300 es\n"
            "precision mediump float;\n"
            "in vec2 v_texCoord;\n"
            "layout(location = 0) out vec4 outColor;\n"
            "uniform sampler2D u_texture0;\n"
            "uniform sampler2D u_texture1;\n"
            "uniform float u_offset;\n"
            "uniform vec2 u_texSize;\n"
            "\n"
            "const float MIN_AMOUNT = -0.16;\n"
            "const float MAX_AMOUNT = 1.5;\n"
            "\n"
            "const float PI = 3.141592653589793;\n"
            "\n"
            "const float scale = 512.0;\n"
            "const float sharpness = 3.0;\n"
            "\n"
            "const float cylinderRadius = 1.0 / PI / 2.0;\n"
            "\n"
            "float amount = 0.0;\n"
            "float cylinderCenter = 0.0;\n"
            "float cylinderAngle = 0.0;\n"
            "\n"
            "vec3 hitPoint(float hitAngle, float yc, vec3 point, mat3 rrotation)\n"
            "{\n"
            "    float hitPoint = hitAngle / (2.0 * PI);\n"
            "    point.y = hitPoint;\n"
            "    return rrotation * point;\n"
            "}\n"
            "\n"
            "vec4 antiAlias(vec4 color1, vec4 color2, float distanc)\n"
            "{\n"
            "    distanc *= scale;\n"
            "    if (distanc < 0.0) return color2;\n"
            "    if (distanc > 2.0) return color1;\n"
            "    float dd = pow(1.0 - distanc / 2.0, sharpness);\n"
            "    return ((color2 - color1) * dd) + color1;\n"
            "}\n"
            "\n"
            "float distanceToEdge(vec3 point)\n"
            "{\n"
            "    float dx = abs(point.x > 0.5 ? 1.0 - point.x : point.x);\n"
            "    float dy = abs(point.y > 0.5 ? 1.0 - point.y : point.y);\n"
            "    if (point.x < 0.0) dx = -point.x;\n"
            "    if (point.x > 1.0) dx = point.x - 1.0;\n"
            "    if (point.y < 0.0) dy = -point.y;\n"
            "    if (point.y > 1.0) dy = point.y - 1.0;\n"
            "    if ((point.x < 0.0 || point.x > 1.0) && (point.y < 0.0 || point.y > 1.0)) return sqrt(dx * dx + dy * dy);\n"
            "    return min(dx, dy);\n"
            "}\n"
            "\n"
            "vec4 seeThrough(float yc, vec2 p, mat3 rotation, mat3 rrotation)\n"
            "{\n"
            "    float hitAngle = PI - (acos(yc / cylinderRadius) - cylinderAngle);\n"
            "    vec3 point = hitPoint(hitAngle, yc, rotation * vec3(p, 1.0), rrotation);\n"
            "    if (yc <= 0.0 && (point.x < 0.0 || point.y < 0.0 || point.x > 1.0 || point.y > 1.0))\n"
            "    {\n"
            "        return texture(u_texture1, p);\n"
            "    }\n"
            "\n"
            "    if (yc > 0.0) return texture(u_texture0, p);\n"
            "\n"
            "    vec4 color = texture(u_texture0, point.xy);\n"
            "    vec4 tcolor = vec4(0.0);\n"
            "\n"
            "    return antiAlias(color, tcolor, distanceToEdge(point));\n"
            "}\n"
            "\n"
            "vec4 seeThroughWithShadow(float yc, vec2 p, vec3 point, mat3 rotation, mat3 rrotation)\n"
            "{\n"
            "    float shadow = distanceToEdge(point) * 30.0;\n"
            "    shadow = (1.0 - shadow) / 3.0;\n"
            "\n"
            "    if (shadow < 0.0) shadow = 0.0; else shadow *= amount;\n"
            "\n"
            "    vec4 shadowColor = seeThrough(yc, p, rotation, rrotation);\n"
            "    shadowColor.r -= shadow;\n"
            "    shadowColor.g -= shadow;\n"
            "    shadowColor.b -= shadow;\n"
            "\n"
            "    return shadowColor;\n"
            "}\n"
            "\n"
            "vec4 backside(float yc, vec3 point)\n"
            "{\n"
            "    vec4 color = texture(u_texture0, point.xy);\n"
            "    float gray = (color.r + color.b + color.g) / 15.0;\n"
            "    gray += (8.0 / 10.0) * (pow(1.0 - abs(yc / cylinderRadius), 2.0 / 10.0) / 2.0 + (5.0 / 10.0));\n"
            "    color.rgb = vec3(gray);\n"
            "    return color;\n"
            "}\n"
            "\n"
            "vec4 behindSurface(vec2 p, float yc, vec3 point, mat3 rrotation)\n"
            "{\n"
            "    float shado = (1.0 - ((-cylinderRadius - yc) / amount * 7.0)) / 6.0;\n"
            "    shado *= 1.0 - abs(point.x - 0.5);\n"
            "\n"
            "    yc = (-cylinderRadius - cylinderRadius - yc);\n"
            "\n"
            "    float hitAngle = (acos(yc / cylinderRadius) + cylinderAngle) - PI;\n"
            "    point = hitPoint(hitAngle, yc, point, rrotation);\n"
            "\n"
            "    if (yc < 0.0 && point.x >= 0.0 && point.y >= 0.0 && point.x <= 1.0 && point.y <= 1.0 && (hitAngle < PI || amount > 0.5))\n"
            "    {\n"
            "        shado = 1.0 - (sqrt(pow(point.x - 0.5, 2.0) + pow(point.y - 0.5, 2.0)) / (71.0 / 100.0));\n"
            "        shado *= pow(-yc / cylinderRadius, 3.0);\n"
            "        shado *= 0.5;\n"
            "    }\n"
            "    else\n"
            "    {\n"
            "        shado = 0.0;\n"
            "    }\n"
            "    return vec4(texture(u_texture1, p).rgb - shado, 1.0);\n"
            "}\n"
            "\n"
            "vec4 transition(vec2 p) {\n"
            "\n"
            "    const float angle = 100.0 * PI / 180.0;\n"
            "    float c = cos(-angle);\n"
            "    float s = sin(-angle);\n"
            "\n"
            "    mat3 rotation = mat3( c, s, 0,\n"
            "    -s, c, 0,\n"
            "    -0.801, 0.8900, 1\n"
            "    );\n"
            "    c = cos(angle);\n"
            "    s = sin(angle);\n"
            "\n"
            "    mat3 rrotation = mat3(\tc, s, 0,\n"
            "    -s, c, 0,\n"
            "    0.98500, 0.985, 1\n"
            "    );\n"
            "\n"
            "    vec3 point = rotation * vec3(p, 1.0);\n"
            "\n"
            "    float yc = point.y - cylinderCenter;\n"
            "\n"
            "    if (yc < -cylinderRadius)\n"
            "    {\n"
            "        // Behind surface\n"
            "        return behindSurface(p,yc, point, rrotation);\n"
            "    }\n"
            "\n"
            "    if (yc > cylinderRadius)\n"
            "    {\n"
            "        // Flat surface\n"
            "        return texture(u_texture0, p);\n"
            "    }\n"
            "\n"
            "    float hitAngle = (acos(yc / cylinderRadius) + cylinderAngle) - PI;\n"
            "\n"
            "    float hitAngleMod = mod(hitAngle, 2.0 * PI);\n"
            "    if ((hitAngleMod > PI && amount < 0.5) || (hitAngleMod > PI/2.0 && amount < 0.0))\n"
            "    {\n"
            "        return seeThrough(yc, p, rotation, rrotation);\n"
            "    }\n"
            "\n"
            "    point = hitPoint(hitAngle, yc, point, rrotation);\n"
            "\n"
            "    if (point.x < 0.0 || point.y < 0.0 || point.x > 1.0 || point.y > 1.0)\n"
            "    {\n"
            "        return seeThroughWithShadow(yc, p, point, rotation, rrotation);\n"
            "    }\n"
            "\n"
            "    vec4 color = backside(yc, point);\n"
            "\n"
            "    vec4 otherColor;\n"
            "    if (yc < 0.0)\n"
            "    {\n"
            "        float shado = 1.0 - (sqrt(pow(point.x - 0.5, 2.0) + pow(point.y - 0.5, 2.0)) / 0.71);\n"
            "        shado *= pow(-yc / cylinderRadius, 3.0);\n"
            "        shado *= 0.5;\n"
            "        otherColor = vec4(0.0, 0.0, 0.0, shado);\n"
            "    }\n"
            "    else\n"
            "    {\n"
            "        otherColor = texture(u_texture0, p);\n"
            "    }\n"
            "\n"
            "    color = antiAlias(color, otherColor, cylinderRadius - abs(yc));\n"
            "\n"
            "    vec4 cl = seeThroughWithShadow(yc, p, point, rotation, rrotation);\n"
            "    float dist = distanceToEdge(point);\n"
            "\n"
            "    return antiAlias(color, cl, dist);\n"
            "}\n"
            "\n"
            "void main()\n"
            "{\n"
            "    amount = u_offset * (MAX_AMOUNT - MIN_AMOUNT) + MIN_AMOUNT;\n"
            "    cylinderCenter = amount;\n"
            "    cylinderAngle = 2.0 * PI * amount;\n"
            "\n"
            "    outColor = transition(v_texCoord);\n"
            "}";





    char fShaderStr2[] =
            "#version 300 es\n"
            "precision mediump float;\n"
            "in vec2 v_texCoord;\n"
            "layout(location = 0) out vec4 outColor;\n"
            "uniform sampler2D u_texture0;\n"
            "uniform sampler2D u_texture1;\n"
            "uniform float u_offset;\n"
            "uniform vec2 u_texSize;\n"
            "\n"
            "const float speed = 1.0;\n"
            "const float angle = 1.0;\n"
            "const float power = 1.5;\n"
            "\n"
            "vec4 transition(vec2 uv) {\n"
            "    vec2 p = uv.xy / vec2(1.0).xy;\n"
            "    vec2 q = p;\n"
            "    float t = pow(u_offset, power)*speed;\n"
            "    p = p -0.5;\n"
            "    for (int i = 0; i < 7; i++) {\n"
            "        p = vec2(sin(t)*p.x + cos(t)*p.y, sin(t)*p.y - cos(t)*p.x);\n"
            "        t += angle;\n"
            "        p = abs(mod(p, 2.0) - 1.0);\n"
            "    }\n"
            "    abs(mod(p, 1.0));\n"
            "    return mix(\n"
            "    mix(texture(u_texture0, q), texture(u_texture1, q), u_offset),\n"
            "    mix(texture(u_texture0, p), texture(u_texture1, p), u_offset), 1.0 - 2.0*abs(u_offset - 0.5));\n"
            "}\n"
            "\n"
            "void main()\n"
            "{\n"
            "    outColor = transition(v_texCoord);\n"
            "}";

    char fShaderStr3[] =
            "#version 300 es\n"
            "precision mediump float;\n"
            "in vec2 v_texCoord;\n"
            "layout(location = 0) out vec4 outColor;\n"
            "uniform sampler2D u_texture0;\n"
            "uniform sampler2D u_texture1;\n"
            "uniform float u_offset;\n"
            "uniform vec2 u_texSize;\n"
            "\n"
            "const float zoom_quickness = 0.8;\n"
            "\n"
            "vec2 zoom(vec2 uv, float amount) {\n"
            "    return 0.5 + ((uv - 0.5) * (1.0-amount));\n"
            "}\n"
            "\n"
            "vec4 transition (vec2 uv) {\n"
            "    float nQuick = clamp(zoom_quickness,0.2,1.0);\n"
            "    return mix(\n"
            "    texture(u_texture0, zoom(uv, smoothstep(0.0, nQuick, u_offset))),\n"
            "    texture(u_texture1, uv),\n"
            "    smoothstep(nQuick-0.2, 1.0, u_offset)\n"
            "    );\n"
            "}\n"
            "\n"
            "void main()\n"
            "{\n"
            "    outColor = transition(v_texCoord);\n"
            "}";


    char fShaderStr4[] =
            "#version 300 es\n"
            "precision mediump float;\n"
            "in vec2 v_texCoord;\n"
            "layout(location = 0) out vec4 outColor;\n"
            "uniform sampler2D u_texture0;\n"
            "uniform sampler2D u_texture1;\n"
            "uniform float u_offset;\n"
            "uniform vec2 u_texSize;\n"
            "\n"
            "// Number of total bars/columns\n"
            "const int bars = 20;\n"
            "\n"
            "// Multiplier for speed ratio. 0 = no variation when going down, higher = some elements go much faster\n"
            "const float amplitude = 2.0;\n"
            "\n"
            "// Further variations in speed. 0 = no noise, 1 = super noisy (ignore frequency)\n"
            "const float noise = 0.1;\n"
            "\n"
            "// Speed variation horizontally. the bigger the value, the shorter the waves\n"
            "const float frequency = 0.5;\n"
            "\n"
            "// How much the bars seem to \"run\" from the middle of the screen first (sticking to the sides). 0 = no drip, 1 = curved drip\n"
            "const float dripScale = 0.5;\n"
            "\n"
            "\n"
            "// The code proper --------\n"
            "\n"
            "float rand(int num) {\n"
            "    return fract(mod(float(num) * 67123.313, 12.0) * sin(float(num) * 10.3) * cos(float(num)));\n"
            "}\n"
            "\n"
            "float wave(int num) {\n"
            "    float fn = float(num) * frequency * 0.1 * float(bars);\n"
            "    return cos(fn * 0.5) * cos(fn * 0.13) * sin((fn+10.0) * 0.3) / 2.0 + 0.5;\n"
            "}\n"
            "\n"
            "float drip(int num) {\n"
            "    return sin(float(num) / float(bars - 1) * 3.141592) * dripScale;\n"
            "}\n"
            "\n"
            "float pos(int num) {\n"
            "    return (noise == 0.0 ? wave(num) : mix(wave(num), rand(num), noise)) + (dripScale == 0.0 ? 0.0 : drip(num));\n"
            "}\n"
            "\n"
            "vec4 getFromColor(vec2 uv) {\n"
            "    return texture(u_texture0, uv);\n"
            "}\n"
            "\n"
            "vec4 getToColor(vec2 uv) {\n"
            "    return texture(u_texture1, uv);\n"
            "}\n"
            "\n"
            "vec4 transition(vec2 uv) {\n"
            "    int bar = int(uv.x * (float(bars)));\n"
            "    float scale = 1.0 + pos(bar) * amplitude;\n"
            "    float phase = u_offset * scale;\n"
            "    float posY = uv.y / vec2(1.0).y;\n"
            "    vec2 p;\n"
            "    vec4 c;\n"
            "    if (phase + posY < 1.0) {\n"
            "        p = vec2(uv.x, uv.y + mix(0.0, vec2(1.0).y, phase)) / vec2(1.0).xy;\n"
            "        c = getFromColor(p);\n"
            "    } else {\n"
            "        p = uv.xy / vec2(1.0).xy;\n"
            "        c = getToColor(p);\n"
            "    }\n"
            "\n"
            "    // Finally, apply the color\n"
            "    return c;\n"
            "}\n"
            "\n"
            "void main()\n"
            "{\n"
            "    outColor = transition(v_texCoord);\n"
            "}";



        m_ProgramObj = GLUtils::CreateProgram(vShaderStr,fShaderStr2);
        if( m_ProgramObj){
                mMvpLocation =  glGetUniformLocation(m_ProgramObj,"u_MVPMatrix");
        } else{
                LogutilsE("Gltransition::Init createProgram failed");
        }

        GLfloat verticesCoords[] = {
                -1.0f,  1.0f, 0.0f,  // Position 0
                -1.0f, -1.0f, 0.0f,  // Position 1
                1.0f,  -1.0f, 0.0f,  // Position 2
                1.0f,   1.0f, 0.0f,  // Position 3
        };

        GLfloat textureCoords[] = {
                0.0f,  0.0f,        // TexCoord 0
                0.0f,  1.0f,        // TexCoord 1
                1.0f,  1.0f,        // TexCoord 2
                1.0f,  0.0f         // TexCoord 3
        };
        GLushort indices[] = {
                0, 1, 2,
                0, 2, 3
        };

        //创建生成3个vbo，用于显示顶点，纹理，和 ebo
        glGenBuffers(3,mVboIds);

        glBindBuffer(GL_ARRAY_BUFFER,mVboIds[0]);
        glBufferData(GL_ARRAY_BUFFER,sizeof (verticesCoords),verticesCoords,GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER,mVboIds[1]);
        glBufferData(GL_ARRAY_BUFFER,sizeof (textureCoords),textureCoords,GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER,mVboIds[2]);
        glBufferData(GL_ARRAY_BUFFER,sizeof (indices),indices,GL_STATIC_DRAW);

        glGenVertexArrays(1,&mVaoId);
        glBindVertexArray(mVaoId);

        glBindBuffer(GL_ARRAY_BUFFER,mVboIds[0]);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0,3,GL_FLOAT,GL_FALSE,3*sizeof (GLfloat),(const void *)0);
        glBindBuffer(GL_ARRAY_BUFFER,GL_NONE);

        glBindBuffer(GL_ARRAY_BUFFER,mVboIds[1]);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1,2,GL_FLOAT,GL_FALSE,2*sizeof (GLfloat),(const void *)0 );
        glBindBuffer(GL_ARRAY_BUFFER,GL_NONE);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,mVboIds[2]);

        glBindVertexArray(GL_NONE);
        glPixelStorei(GL_UNPACK_ALIGNMENT,1);

        for(int i = 0; i < BF_IMG_NUM; ++i){
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D,m_TextureIds[i]);
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,nativeImage[i].width,nativeImage[i].height,
                         0,GL_RGBA,GL_UNSIGNED_BYTE,nativeImage[i].ppPlane[0]);
        }
}





void Gltransition::LoadImage(NativeImage *pImage) {
    LogutilsE("Gltransition::LoadImage ： %p",pImage->ppPlane[0]);
}






void Gltransition::Draw(int screenW, int screenH) {

    LogutilsE("GLTransitionExample::Draw()");
    if(m_ProgramObj == GL_NONE || m_TextureIds[0] == GL_NONE){
        LogutilsE("GLTransitionExample::Draw m_ProgramObj or m_TextureIds[0] is null");
        return;
    }

    mFrameIndex ++;
    //更新mvp矩阵
    UpdateMVPMatrix(mMvpMatrix,mAngleX,mAngleY,(float)(screenW/screenH) );

    glUseProgram(m_ProgramObj);

    glBindVertexArray(mVaoId);
    glUniformMatrix4fv(mMvpLocation,1,GL_FALSE,&mMvpMatrix[0][0]);

    float  offset = (mFrameIndex % BF_LOOP_COUNT) * 1.0f / BF_LOOP_COUNT;

    if(mFrameIndex  % BF_LOOP_COUNT == 0){
        mLoopCount ++;
    }


    //绑定2个纹理 分别绑定到 纹理插槽0 和插槽1上
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D,m_TextureIds[mLoopCount % BF_IMG_NUM]);
    GLUtils::setInt(m_ProgramObj,"u_texture0",0);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D,m_TextureIds [ (mLoopCount +1)  % BF_IMG_NUM]);
    GLUtils::setInt(m_ProgramObj,"u_texture1",1);

    //fragment shader 中只传了2个值 一个是纹理也就是图片的宽和高 另外一个 是 u_offset
    GLUtils::setVec2(m_ProgramObj, "u_texSize", nativeImage[0].width, nativeImage[0].height);
    GLUtils::setFloat(m_ProgramObj,"u_offset",offset);

    glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,(const void *)0);

}

void Gltransition::Destroy() {

    if (m_ProgramObj)
    {
        glDeleteProgram(m_ProgramObj);
        glDeleteBuffers(3, mVboIds);
        glDeleteVertexArrays(1, &mVaoId);
        glDeleteTextures(BF_IMG_NUM, m_TextureIds);
    }
}



void Gltransition::UpdateMVPMatrix(glm::mat4 &mvpMatrix, int angleX, int angleY, float ration) {
    LogutilsI("GLTransitionExample::UpdateMVPMatrix angleX = %d, angleY = %d, ratio = %f", angleX, angleY, ration);

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
    moduleM = glm::scale(moduleM,glm::vec3(mScaleX, mScaleY, 1.0f));
    moduleM = glm::rotate(moduleM,radiansX,glm::vec3(1.0f,0.0f,0.0f));
    moduleM = glm::rotate(moduleM,radiansY,glm::vec3(0.0f,1.0f,0.0f));

    moduleM = glm::translate(moduleM,glm::vec3(0.0f,0.0f,0.0f));

    mvpMatrix = projectM*viewM*moduleM;
}

//从界面上进行窗口变化 或者 有手指移动和缩放等  通过此接口更新，此接口将 mAngleX mAngleY mScaleX，mScaleX 等更新
//  每次DrawCall 的时候就 会重新计算 mvp 矩阵 从而达到更新界面
void Gltransition::UpdateTransformMatrix(float rotateX, float rotateY, float scaleX, float scaleY) {
    GLSampleBase::UpdateTransformMatrix(rotateX,rotateY,scaleX,scaleY);

    mAngleX = static_cast<int >(rotateX);
    mAngleY = static_cast<int >(rotateY);

    mScaleX = scaleX;
    mScaleY = scaleY;

}

//native 测已经指定做多就存储6张图片
void Gltransition::LoadMultiImageWithIndex(int index, NativeImage *pImage) {
    LogutilsI("Gltransition::LoadMultiImageWithIndex pImage = %p,[w=%d,h=%d,f=%d]",
              pImage->ppPlane[0], pImage->width, pImage->height, pImage->format);

    if( pImage  && index >=0 && index <= BF_IMG_NUM){
        nativeImage[index].width = pImage->width;
        nativeImage[index].height = pImage->height;
        nativeImage[index].format = pImage->format;

//        nativeImage[index].ppPlane = pImage->ppPlane; //不能直接用 界面传下来的内存在java虚拟机中
    //     将原图片的内存拷贝到opengl 所在的 Gltransition类中所准备的数组中
        NativeImageUtil::CopyNativeImage(pImage,&nativeImage[index]);
    }
}


