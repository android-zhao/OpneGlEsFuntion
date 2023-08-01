//
// Created by didi on 2023/5/23.
//

#ifndef OPENGLDEMO_GLSAMPLEBASE_H
#define OPENGLDEMO_GLSAMPLEBASE_H

#include "stdint.h"
#include <GLES3/gl3.h>
#include "OpenGlLock.h"
#include "ImageDef.h"

#define MATH_PI 3.1415926535897932384626433832802

#define SAMPLE_TYPE                             200
#define SAMPLE_TYPE_KEY_TRIANGLE                SAMPLE_TYPE + 0
#define SAMPLE_TYPE_KEY_TEXTURE_MAP             SAMPLE_TYPE + 1
#define SAMPLE_TYPE_KEY_YUV_TEXTURE_MAP         SAMPLE_TYPE + 2
#define SAMPLE_TYPE_KEY_VAO                     SAMPLE_TYPE + 3
#define SAMPLE_TYPE_KEY_FBO                     SAMPLE_TYPE + 4
#define SAMPLE_TYPE_KEY_FBO_LEG_LENGTHEN        SAMPLE_TYPE + 6
#define SAMPLE_TYPE_KEY_COORD_SYSTEM            SAMPLE_TYPE + 7
#define SAMPLE_TYPE_KEY_BASIC_LIGHTING          SAMPLE_TYPE + 8
#define SAMPLE_TYPE_KEY_TRANSFORM_FEEDBACK      SAMPLE_TYPE + 9
#define SAMPLE_TYPE_KEY_MULTI_LIGHTS            SAMPLE_TYPE + 10
#define SAMPLE_TYPE_KEY_DEPTH_TESTING           SAMPLE_TYPE + 11
#define SAMPLE_TYPE_KEY_INSTANCING              SAMPLE_TYPE + 12
#define SAMPLE_TYPE_KEY_STENCIL_TESTING         SAMPLE_TYPE + 13
#define SAMPLE_TYPE_KEY_BLENDING                SAMPLE_TYPE + 14
#define SAMPLE_TYPE_KEY_PARTICLES               SAMPLE_TYPE + 15
#define SAMPLE_TYPE_KEY_SKYBOX                  SAMPLE_TYPE + 16
#define SAMPLE_TYPE_KEY_3D_MODEL                SAMPLE_TYPE + 17
#define SAMPLE_TYPE_KEY_PBO                     SAMPLE_TYPE + 18
#define SAMPLE_TYPE_KEY_BEATING_HEART           SAMPLE_TYPE + 19
#define SAMPLE_TYPE_KEY_CLOUD                   SAMPLE_TYPE + 20
#define SAMPLE_TYPE_KEY_TIME_TUNNEL             SAMPLE_TYPE + 21
#define SAMPLE_TYPE_KEY_BEZIER_CURVE            SAMPLE_TYPE + 22
#define SAMPLE_TYPE_KEY_BIG_EYES                SAMPLE_TYPE + 23
#define SAMPLE_TYPE_KEY_FACE_SLENDER            SAMPLE_TYPE + 24
#define SAMPLE_TYPE_KEY_BIG_HEAD                SAMPLE_TYPE + 25
#define SAMPLE_TYPE_KEY_RATARY_HEAD             SAMPLE_TYPE + 26
#define SAMPLE_TYPE_KEY_VISUALIZE_AUDIO         SAMPLE_TYPE + 27
#define SAMPLE_TYPE_KEY_SCRATCH_CARD            SAMPLE_TYPE + 28
#define SAMPLE_TYPE_KEY_AVATAR                  SAMPLE_TYPE + 29
#define SAMPLE_TYPE_KEY_SHOCK_WAVE              SAMPLE_TYPE + 30
#define SAMPLE_TYPE_KEY_MRT                     SAMPLE_TYPE + 31
#define SAMPLE_TYPE_KEY_FBO_BLIT                SAMPLE_TYPE + 32
#define SAMPLE_TYPE_KEY_TBO                     SAMPLE_TYPE + 33
#define SAMPLE_TYPE_KEY_UBO                     SAMPLE_TYPE + 34
#define SAMPLE_TYPE_KEY_RGB2YUYV                SAMPLE_TYPE + 35
#define SAMPLE_TYPE_KEY_MULTI_THREAD_RENDER     SAMPLE_TYPE + 36
#define SAMPLE_TYPE_KEY_TEXT_RENDER     		SAMPLE_TYPE + 37
#define SAMPLE_TYPE_KEY_STAY_COLOR       		SAMPLE_TYPE + 38
#define SAMPLE_TYPE_KEY_TRANSITIONS_1      		SAMPLE_TYPE + 39
#define SAMPLE_TYPE_KEY_TRANSITIONS_2      		SAMPLE_TYPE + 40
#define SAMPLE_TYPE_KEY_TRANSITIONS_3      		SAMPLE_TYPE + 41
#define SAMPLE_TYPE_KEY_TRANSITIONS_4      		SAMPLE_TYPE + 42
#define SAMPLE_TYPE_KEY_RGB2NV21                SAMPLE_TYPE + 43
#define SAMPLE_TYPE_KEY_RGB2I420                SAMPLE_TYPE + 44
#define SAMPLE_TYPE_KEY_RGB2I444                SAMPLE_TYPE + 45
#define SAMPLE_TYPE_KEY_COPY_TEXTURE            SAMPLE_TYPE + 46
#define SAMPLE_TYPE_KEY_BLIT_FRAME_BUFFER       SAMPLE_TYPE + 47
#define SAMPLE_TYPE_KEY_BINARY_PROGRAM          SAMPLE_TYPE + 48
#define SAMPLE_TYPE_KEY_HWBuffer                SAMPLE_TYPE + 50

#define SAMPLE_TYPE_KEY_SHADER_TOY              SAMPLE_TYPE + 51


class GLSampleBase {
public:
    GLSampleBase(){
        m_ProgramObj = 0;
        m_VertexShader = 0;
        m_FragmentShader = 0;

        m_SurfaceWidth = 0;
        m_SurfaceHeight = 0;
    }
    virtual ~GLSampleBase(){};

    virtual void LoadImage(NativeImage *nativeImage){

    }

    virtual void LoadMultiImageWithIndex(int index,NativeImage *nativeImage){

    }

    //指针常量 --> 指针指向不可以更改，指针的值可以修改
    //主要看 const 离谁近，离pShortArr 近，修饰 pShortArr，那么pShortArr 不能变，、
    // pShortArr是指针，所以指针不能变，也就是指针指向的那块内存不能变，但是内存上面的值可以变
    virtual void LoadShortArrData(short *const pShortArr,int arr){

    }

    //常量指针  形如 const  short * pShortArr --->指针可以变，但是指针指向的内存的地址不能变
    //主要看 const 离谁近 主要离(short * ) 近，short *  表示指向的值 ，所以 pShortArr能变 但是（short *  ）不能变
    virtual  void UpdateTransformMatrix(float rotateX,float rotateY,float scaleX,float scaleY){

    }

    virtual void setTouchPosition(float x,float y){

    }

    virtual void SetGravityXY(float x ,float y){

    }

    //纯虚函数 特性，无法实例化对象，子类必须实现
    virtual void Init() = 0;
    virtual void Draw(int screenWidth,int screenHeight) = 0;
    virtual void Destroy() = 0;

protected:
    GLuint m_VertexShader;
    GLuint m_FragmentShader;
    GLuint m_ProgramObj;
    MySynclock mLock;

    float iTimeOffet;
    int m_SurfaceWidth;
    int m_SurfaceHeight;

};
#endif //OPENGLDEMO_GLSAMPLEBASE_H
