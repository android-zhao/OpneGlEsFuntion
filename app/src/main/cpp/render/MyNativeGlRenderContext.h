//
// Created by didi on 2023/6/1.
//

#ifndef OPENGLDEMO_MYNATIVEGLRENDERCONTEXT_H
#define OPENGLDEMO_MYNATIVEGLRENDERCONTEXT_H


#include "GLSampleBase.h"
#include "GLES3/gl3.h"


class MyNativeGlRenderContext {

    MyNativeGlRenderContext();
    ~MyNativeGlRenderContext();
public:
    void SetImageData(int format, int width, int height, uint8_t *pData);

    void SetImageDataWithIndex(int index, int format, int width, int height, uint8_t *pData);

    void SetParamsInt(int paramType, int value0, int value1);

    void SetParamsFloat(int paramType, float value0, float value1);

    void SetParamsShortArr(short *const pShortArr, int arrSize);

    void UpdateTransformMatrix(float rotateX, float rotateY, float scaleX, float scaleY);

    void OnSurfaceCreated();

    void OnSurfaceChanged(int width, int height);

    void OnDrawFrame();

    static MyNativeGlRenderContext* getInstance();
    static void OnDestroyInstance();

private:
    static MyNativeGlRenderContext * mPContext;
    int screenWidth;
    int screenHeight;

    GLSampleBase *mPreShader;
    GLSampleBase *mPCurShader;

};


#endif //OPENGLDEMO_MYNATIVEGLRENDERCONTEXT_H
