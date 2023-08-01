//
// Created by didi on 2023/5/18.
//

#include "EglRender.h"
#include "Logutils.h"
#include "GLUtils.h"

#define PARAM_TYPE_SHADER_INDEX    200
#define VERTEX_POS_LOC  0
#define TEXTURE_POS_LOC 1

EglRender *EglRender::mInstance = nullptr;

const char vShaderStr[] =
        "#version 300 es                            \n"
        "layout(location = 0) in vec4 a_position;   \n"
        "layout(location = 1) in vec2 a_texCoord;   \n"
        "out vec2 v_texCoord;                       \n"
        "void main()                                \n"
        "{                                          \n"
        "   gl_Position = a_position;               \n"
        "   v_texCoord = a_texCoord;                \n"
        "}                                          \n";

const char fShaderStr0[] =
        "#version 300 es\n"
        "precision mediump float;\n"
        "in vec2 v_texCoord;\n"
        "layout(location = 0) out vec4 outColor;\n"
        "uniform sampler2D s_TextureMap;\n"
        "void main()\n"
        "{\n"
        "    outColor = texture(s_TextureMap, v_texCoord);\n"
        "}";

// 马赛克
const char fShaderStr1[] =
        "#version 300 es\n"
        "precision highp float;\n"
        "layout(location = 0) out vec4 outColor;\n"
        "in vec2 v_texCoord;\n"
        "uniform sampler2D s_TextureMap;\n"
        "uniform vec2 u_texSize;\n"
        "\n"
        "vec4 CrossStitching(vec2 uv) {\n"
        "    float stitchSize = u_texSize.x / 35.0;\n"
        "    int invert = 0;\n"
        "    vec4 color = vec4(0.0);\n"
        "    float size = stitchSize;\n"
        "    vec2 cPos = uv * u_texSize.xy;\n"
        "    vec2 tlPos = floor(cPos / vec2(size, size));\n"
        "    tlPos *= size;\n"
        "    int remX = int(mod(cPos.x, size));\n"
        "    int remY = int(mod(cPos.y, size));\n"
        "    if (remX == 0 && remY == 0)\n"
        "    tlPos = cPos;\n"
        "    vec2 blPos = tlPos;\n"
        "    blPos.y += (size - 1.0);\n"
        "    if ((remX == remY) || (((int(cPos.x) - int(blPos.x)) == (int(blPos.y) - int(cPos.y))))) {\n"
        "        if (invert == 1)\n"
        "        color = vec4(0.2, 0.15, 0.05, 1.0);\n"
        "        else\n"
        "        color = texture(s_TextureMap, tlPos * vec2(1.0 / u_texSize.x, 1.0 / u_texSize.y)) * 1.4;\n"
        "    } else {\n"
        "        if (invert == 1)\n"
        "        color = texture(s_TextureMap, tlPos * vec2(1.0 / u_texSize.x, 1.0 / u_texSize.y)) * 1.4;\n"
        "        else\n"
        "        color = vec4(0.0, 0.0, 0.0, 1.0);\n"
        "    }\n"
        "    return color;\n"
        "}\n"
        "void main() {\n"
        "    outColor = CrossStitching(v_texCoord);\n"
        "}";

// 网格
const char fShaderStr2[] =
        "#version 300 es\n"
        "precision highp float;\n"
        "layout(location = 0) out vec4 outColor;\n"
        "in vec2 v_texCoord;\n"
        "uniform sampler2D s_TextureMap;\n"
        "uniform vec2 u_texSize;\n"
        "void main() {\n"
        "    float size = u_texSize.x / 75.0;\n"
        "    float radius = size * 0.5;\n"
        "    vec2 fragCoord = v_texCoord * u_texSize.xy;\n"
        "    vec2 quadPos = floor(fragCoord.xy / size) * size;\n"
        "    vec2 quad = quadPos/u_texSize.xy;\n"
        "    vec2 quadCenter = (quadPos + size/2.0);\n"
        "    float dist = length(quadCenter - fragCoord.xy);\n"
        "\n"
        "    if (dist > radius) {\n"
        "        outColor = vec4(0.25);\n"
        "    } else {\n"
        "        outColor = texture(s_TextureMap, v_texCoord);\n"
        "    }\n"
        "}";


//顶点坐标
const GLfloat vVertices[] = {
        -1.0f, -1.0f, 0.0f, // bottom left
        1.0f, -1.0f, 0.0f, // bottom right
        -1.0f,  1.0f, 0.0f, // top left
        1.0f,  1.0f, 0.0f, // top right
};

//正常纹理坐标
const GLfloat vTexCoors[] = {
        0.0f, 1.0f, // bottom left
        1.0f, 1.0f, // bottom right
        0.0f, 0.0f, // top left
        1.0f, 0.0f, // top right
};

//fbo 纹理坐标与正常纹理方向不同(上下镜像)
const GLfloat vFboTexCoors[] = {
        0.0f, 0.0f,  // bottom left
        1.0f, 0.0f,  // bottom right
        0.0f, 1.0f,  // top left
        1.0f, 1.0f,  // top right
};

const GLushort indices[] = {
        0, 1, 2,
        1, 3, 2
};

EglRender::EglRender() {
    m_ImageTextureId = GL_NONE;
    m_FboTextureId = GL_NONE;
    m_SamplerLoc = GL_NONE;
    m_TexSizeLoc = GL_NONE;
    m_FboId = GL_NONE;
    m_ProgramObj = GL_NONE;
    m_VertexShader = GL_NONE;
    m_FragmentShader = GL_NONE;

    m_IsGLContextReady = false;
    m_ShaderIndex = 0;
}

EglRender::~EglRender()  {
    if(m_RenderImage.ppPlane[0]){
        NativeImageUtil::FreeNativeImage(&m_RenderImage);
        m_RenderImage.ppPlane[0] = nullptr;
    }
}

int EglRender::CreateGlesEnv() {
    LogutilsI("EGLRender::CreateGlesEnv");
    // EGL config attributes
    const EGLint confAttr[] =
            {
                    EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT_KHR,
                    EGL_SURFACE_TYPE,EGL_PBUFFER_BIT,//EGL_WINDOW_BIT EGL_PBUFFER_BIT we will create a pixelbuffer surface
                    EGL_RED_SIZE,   8,
                    EGL_GREEN_SIZE, 8,
                    EGL_BLUE_SIZE,  8,
                    EGL_ALPHA_SIZE, 8,// if you need the alpha channel
                    EGL_DEPTH_SIZE, 16,// if you need the depth buffer
                    EGL_STENCIL_SIZE,8,
                    EGL_NONE
            };
    // EGL context attributes
    const EGLint ctxAttr[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL_NONE
    };

    // surface attributes
    // the surface size is set to the input frame size
    const EGLint surfaceAttr[] = {
            EGL_WIDTH, 1,
            EGL_HEIGHT,1,
            EGL_NONE
    };

    EGLint eglMajVers, eglMinVers;
    EGLint numConfig;

    int result = 0;
    do{
        //第一步：获取eglDisplay
        m_eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        if(m_eglDisplay == EGL_NO_DISPLAY){
            LogutilsE("EGLRender::CreateGlesEnv Unable to open connection to local windowing system");
            result = -1;
            break;
        }
        //第二步：egl 初始化，获取版本
        if(   !eglInitialize(m_eglDisplay,&eglMajVers,&eglMinVers) ){
            // Unable to initialize EGL. Handle and recover
            LogutilsE("EGLRender::CreateGlesEnv Unable to initialize EGL");
            result = -1;
            break;
        }

        LogutilsE("EGLRender::CreateGlesEnv EGL init with version %d.%d", eglMajVers, eglMinVers);

        //第三步：egl配置
        if(  !eglChooseConfig(m_eglDisplay,confAttr,&m_eglConf,1,&numConfig) ){
            LogutilsE("EGLRender::CreateGlesEnv some config is wrong");
            result = -1;
            break;
        }

        //第四步：创建eglSurface，此eglsurface是离屏渲染
        m_eglSurface = eglCreatePbufferSurface(m_eglDisplay,m_eglConf,surfaceAttr);
        if(m_eglSurface == EGL_NO_SURFACE){
            switch (eglGetError()) {
                case EGL_BAD_ALLOC:
                    // Not enough resources available. Handle and recover
                    LogutilsE("EGLRender::CreateGlesEnv Not enough resources available");
                    break;
                case EGL_BAD_CONFIG:
                    // Verify that provided EGLConfig is valid
                    LogutilsE("EGLRender::CreateGlesEnv provided EGLConfig is invalid");
                    break;
                case EGL_BAD_PARAMETER:
                    // Verify that the EGL_WIDTH and EGL_HEIGHT are
                    // non-negative values
                    LogutilsE("EGLRender::CreateGlesEnv provided EGL_WIDTH and EGL_HEIGHT is invalid");
                    break;
                case EGL_BAD_MATCH:
                    // Check window and EGLConfig attributes to determine
                    // compatibility and pbuffer-texture parameters
                    LogutilsE("EGLRender::CreateGlesEnv Check window and EGLConfig attributes");
                    break;

            }

        }

        //第五步：创建eglContext
        m_eglCtx = eglCreateContext(m_eglDisplay,m_eglConf,EGL_NO_CONTEXT,ctxAttr);
        if(m_eglCtx == EGL_NO_CONTEXT)
        {
            EGLint error = eglGetError();
            if(error == EGL_BAD_CONFIG)
            {
                // Handle error and recover
                LogutilsE("EGLRender::CreateGlesEnv EGL_BAD_CONFIG");
                result = -1;
                break;
            }
        }

        //第六步：绑定eglSurface
        if(!eglMakeCurrent(m_eglDisplay,m_eglSurface,m_eglSurface,m_eglCtx)){
            LogutilsE("EGLRender::CreateGlesEnv MakeCurrent failed");
            result = -1;
            break;
        }
        LogutilsI("EGLRender::CreateGlesEnv initialize success!");
    }
    while (false);


    if(result != 0){
        LogutilsI("EGLRender::CreateGlesEnv fail");
    }

    return result;
}

void EglRender::Init() {
    LogutilsI("EglRender init");
    if (CreateGlesEnv() == 0)
    {
        m_IsGLContextReady = true;
    }
    if(!m_IsGLContextReady) return;
    //todo 就是用一个shader类型
    m_fShaderStrs[0] = fShaderStr0;
    m_fShaderStrs[1] = fShaderStr1;
//    m_fShaderStrs[1] = fShaderStr2;
    //创建普通纹理 给前台使用
    glGenTextures(1, &m_ImageTextureId);
    glBindTexture(GL_TEXTURE_2D, m_ImageTextureId);

    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);

    //创建FBO纹理  给FBO buffer使用
    glGenTextures(1, &m_FboTextureId);
    glBindTexture(GL_TEXTURE_2D, m_FboTextureId);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);

    //todo 根据界面上层传下来的索引切换片元着色器脚本
    //马赛克特效
    m_ProgramObj = GLUtils::CreateProgram(vShaderStr,m_fShaderStrs[1],m_VertexShader,m_FragmentShader);
    if(!m_ProgramObj){
        GLUtils::GlCheckError("CreateProgram");
        LogutilsI("CreateProgram error");
        return;
    }

    m_SamplerLoc = glGetUniformLocation(m_ProgramObj, "s_TextureMap");
    m_TexSizeLoc = glGetUniformLocation(m_ProgramObj, "u_texSize");

    //创建三个VB0，分别代表顶点，纹理 和 索引坐标，并指定大小和绘制 规则
    glGenBuffers(3,m_VboIds);
    glBindBuffer(GL_ARRAY_BUFFER,m_VboIds[0]);
    glBufferData(GL_ARRAY_BUFFER,sizeof (vVertices),vVertices,GL_STATIC_DRAW);

    glBindBuffer(GL_ARRAY_BUFFER,m_VboIds[1]);
    glBufferData(GL_ARRAY_BUFFER,sizeof (vTexCoors),vTexCoors,GL_STATIC_DRAW);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,m_VboIds[2]);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER,sizeof (indices),indices,GL_STATIC_DRAW);


    GO_CHECK_GL_ERROR();

    //将Vb0都绑定到VAO上， 一般CPU 向GPU 传送数据 通过传送VA0 一次经数据都送到GPU
    glGenVertexArrays(1,m_VaoIds);

    glBindVertexArray(m_VaoIds[0]);
    glBindBuffer(GL_ARRAY_BUFFER, m_VboIds[0]);
    glEnableVertexAttribArray(VERTEX_POS_LOC);
    glVertexAttribPointer(VERTEX_POS_LOC,3,GL_FLOAT,GL_FALSE,3 * sizeof(GLfloat),(const void *)0 );
    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);

    glBindVertexArray(m_VaoIds[1]);
    glBindBuffer(GL_ARRAY_BUFFER, m_VboIds[1]);
    glEnableVertexAttribArray(TEXTURE_POS_LOC);
    glVertexAttribPointer(TEXTURE_POS_LOC,2,GL_FLOAT,GL_FALSE,2 * sizeof(GLfloat),(const void *)0 );
    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m_VboIds[2]);
    GO_CHECK_GL_ERROR();
    glBindVertexArray(GL_NONE);
}

void EglRender::setImageData(uint8_t *pData, int width, int height) {
    LogutilsI("EGLRender::SetImageData pData = %p, [w,h] = [%d, %d],m_IsGLContextReady: %d", pData, width, height,m_IsGLContextReady);

    //完成 egl 条件创建和 传下来的图片数据不为空
    if(pData || m_IsGLContextReady){
        if (m_RenderImage.ppPlane[0])
        {
            NativeImageUtil::FreeNativeImage(&m_RenderImage);
            m_RenderImage.ppPlane[0] = nullptr;
        }

        m_RenderImage.width  =width;
        m_RenderImage.height = height;
        m_RenderImage.format = IMAGE_FORMAT_RGBA;
        NativeImageUtil::AllocNativeImage(&m_RenderImage);
        LogutilsI("EGLRender::SetImageData ，AllocNativeImage end");
        //将java层传下来的数据 进行memcpy 到native 另一块内存中去
        memcpy(m_RenderImage.ppPlane[0], pData, static_cast<size_t>(width * height * 4));

        //init 时已经初始化创建了纹理
        glBindTexture(GL_TEXTURE_2D,m_ImageTextureId);
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,m_RenderImage.width,m_RenderImage.height,
                     0,GL_RGBA,GL_UNSIGNED_BYTE,m_RenderImage.ppPlane[0]);
        glBindTexture(GL_TEXTURE_2D,GL_NONE);
        LogutilsI("EGLRender::SetImageData ，bindTexture end");
        //高层传下来图片就创建FBO
        if(m_FboId == GL_NONE){
            LogutilsI("EGLRender::SetImageData ，m_FboId %d",m_FboId);
            glGenFramebuffers(1,&m_FboId);
            glBindFramebuffer(GL_FRAMEBUFFER,m_FboId);
            glBindTexture(GL_TEXTURE_2D,m_FboTextureId);
            //todo GL_COLOR_ATTACHMENT0 参数含义 待理解
            glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D,m_FboTextureId,0);
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,m_RenderImage.width,m_RenderImage.height,
                         0,GL_RGBA,GL_UNSIGNED_BYTE,nullptr);

            if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
                LogutilsI("EGLRender::SetImageData glCheckFramebufferStatus status != GL_FRAMEBUFFER_COMPLETE");
            }
            glBindTexture(GL_TEXTURE_2D,GL_NONE);
            glBindFramebuffer(GL_FRAMEBUFFER,GL_NONE);
        }
        LogutilsI("EGLRender::SetImageData  end");
    }

}

void EglRender::Draw() {
    LogutilsI("EGLRender::Draw");
    if(m_ProgramObj == GL_NONE){
        return;
    }

    //指定窗口大小
    glViewport(0,0,m_RenderImage.width,m_RenderImage.height);
    glUseProgram(m_ProgramObj);
    glBindFramebuffer(GL_FRAMEBUFFER,m_FboId);

    //指定VAO 和 纹理
    glBindVertexArray(m_VaoIds[0]);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D,m_ImageTextureId);
    glUniform1f(m_SamplerLoc,0);

    if (m_TexSizeLoc > -1) {
        GLfloat size[2];
        size[0] = m_RenderImage.width;
        size[1] = m_RenderImage.height;
        glUniform2f(m_TexSizeLoc, size[0], size[1]);
    }

    GO_CHECK_GL_ERROR();
    glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,(const void*)0);
    glBindVertexArray(GL_NONE);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);

}

void EglRender::UnInit()
{
    LogutilsI("EGLRender::UnInit");
    if (m_ProgramObj)
    {
        glDeleteProgram(m_ProgramObj);
        m_ProgramObj = GL_NONE;
    }

    if (m_ImageTextureId)
    {
        glDeleteTextures(1, &m_ImageTextureId);
        m_ImageTextureId = GL_NONE;
    }

    if (m_FboTextureId)
    {
        glDeleteTextures(1, &m_FboTextureId);
        m_FboTextureId = GL_NONE;
    }

    if (m_VboIds[0])
    {
        glDeleteBuffers(3, m_VboIds);
        m_VboIds[0] = GL_NONE;
        m_VboIds[1] = GL_NONE;
        m_VboIds[2] = GL_NONE;

    }

    if (m_VaoIds[0])
    {
        glDeleteVertexArrays(1, m_VaoIds);
        m_VaoIds[0] = GL_NONE;
    }

    if (m_FboId)
    {
        glDeleteFramebuffers(1, &m_FboId);
        m_FboId = GL_NONE;
    }


    if (m_IsGLContextReady)
    {
        DestroyGlesEnv();
        m_IsGLContextReady = false;
    }

}

void EglRender::DestroyGlesEnv()
{
    //8. 释放 EGL 环境
    if (m_eglDisplay != EGL_NO_DISPLAY) {
        eglMakeCurrent(m_eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(m_eglDisplay, m_eglCtx);
        eglDestroySurface(m_eglDisplay, m_eglSurface);
        eglReleaseThread();
        eglTerminate(m_eglDisplay);
    }

    m_eglDisplay = EGL_NO_DISPLAY;
    m_eglSurface = EGL_NO_SURFACE;
    m_eglCtx = EGL_NO_CONTEXT;

}

