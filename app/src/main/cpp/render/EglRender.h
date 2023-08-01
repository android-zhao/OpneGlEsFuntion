//
// Created by didi on 2023/5/18.
//

#ifndef OPENGLDEMO_EGLRENDER_H
#define OPENGLDEMO_EGLRENDER_H

#include "stdint.h"
#include <GLES3/gl3.h>
#include <ImageDef.h>
#include "EGL/egl.h"
#include "EGL/eglext.h"

#define EGL_FEATURE_NUM 7
class EglRender {

public:
    EglRender();
    ~EglRender();

    void Init();

    int CreateGlesEnv();


    void setImageData(uint8_t *pData,int width,int height);

    void SetIntParams(int paramType, int param);

    void Draw();

    void DestroyGlesEnv();

    void UnInit();

    static EglRender* getInstance(){

        if(mInstance == nullptr){
            mInstance = new EglRender();
        }
        return mInstance;
    }

    static void destroyInstance(){
        if(mInstance != nullptr){
            delete mInstance;
            mInstance = nullptr;
        }
    }

private:
    static  EglRender *mInstance;
//    GLuint
    GLuint m_ImageTextureId;
    GLuint m_FboTextureId;
    GLuint m_FboId;
    GLuint m_VaoIds[1] = {GL_NONE};
    GLuint m_VboIds[3] = {GL_NONE};

    GLint m_SamplerLoc;
    GLint m_TexSizeLoc;

    NativeImage m_RenderImage;
    GLuint m_ProgramObj;
    GLuint m_VertexShader;
    GLuint m_FragmentShader;

    EGLConfig  m_eglConf;
    EGLSurface  m_eglSurface;
    EGLContext  m_eglCtx;
    EGLDisplay m_eglDisplay;

    bool       m_IsGLContextReady;
    const char*m_fShaderStrs [EGL_FEATURE_NUM];
    int        m_ShaderIndex;

};


#endif //OPENGLDEMO_EGLRENDER_H
