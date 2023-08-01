//
// Created by didi on 2023/6/1.
//

#include "MyNativeGlRenderContext.h"
#include "Logutils.h"
#include "ImageDef.h"
#include "../shadersample/Gltransition.h"
#include "../shadertoy/ShaderTopTest.h"


MyNativeGlRenderContext * MyNativeGlRenderContext::mPContext = nullptr;

MyNativeGlRenderContext::MyNativeGlRenderContext() {
    mPreShader = nullptr;
    mPCurShader = new Gltransition();

}

MyNativeGlRenderContext::~MyNativeGlRenderContext() {
    if(mPreShader){
        delete mPreShader;
        mPreShader = nullptr;
    }

    if(mPCurShader){
        delete mPCurShader;
        mPCurShader = nullptr;
    }
}



void MyNativeGlRenderContext::SetImageData(int format, int width, int height, uint8_t *pData) {

}


MyNativeGlRenderContext *MyNativeGlRenderContext::getInstance() {
//    LogutilsI("MyNativeGlRenderContext getInstance begin" );
    if( mPContext == nullptr ){
        mPContext =  new MyNativeGlRenderContext();
    }
    return mPContext;
}

void MyNativeGlRenderContext::OnDestroyInstance() {
    LogutilsI("MyNativeGlRenderContext getInstance begin" );
    if(mPContext != nullptr){
        delete mPContext;
        mPContext = nullptr;
    }
}

void MyNativeGlRenderContext::SetImageDataWithIndex(int index, int format, int width, int height,
                                                    uint8_t *pData) {
    LogutilsI("MyNativeGlRenderContext SetImageDataWithIndex begin" );
    LogutilsI("MyNativeGlRenderContext SetImageDataWithIndex index:%d,format:%d,width:%d,height:%d,pData:%p",
              index,format,width,height,pData);
    NativeImage nativeImage;
    nativeImage.width = width;
    nativeImage.height = height;
    nativeImage.format = format;
    nativeImage.ppPlane[0] = pData;

    switch (format) {
        case IMAGE_FORMAT_NV21:
        case IMAGE_FORMAT_NV12:
            nativeImage.ppPlane[1] = nativeImage.ppPlane[0] + width * height ;
            break;
        case IMAGE_FORMAT_I420:
            nativeImage.ppPlane[1] = nativeImage.ppPlane[0] + width * height;
            nativeImage.ppPlane[2] = nativeImage.ppPlane[1] + width * height / 4;
            break;
        default:
            break;
    }

    if(mPCurShader){
        mPCurShader->LoadMultiImageWithIndex(index,&nativeImage);
    }
}

void MyNativeGlRenderContext::OnSurfaceCreated() {
    LogutilsI(" MyNativeGlRenderContext::OnSurfaceCreated");
    glClearColor(1.0f,1.0f,1.0f,1.0f);
}


void MyNativeGlRenderContext::OnSurfaceChanged(int width, int height) {
    LogutilsI(" MyNativeGlRenderContext::OnSurfaceChanged");
    glViewport(0,0,width,height);
    screenWidth = width;
    screenHeight = height;
    if(mPCurShader){
        mPCurShader->Init();
    }
}

void MyNativeGlRenderContext::SetParamsInt(int paramType, int value0, int value1) {
    LogutilsI(" MyNativeGlRenderContext::SetParamsInt paramType %d,value0 :%d",paramType,value0);
    // todo  mPreShader存在和mPCurShader的赋值转换行为 此处暂时先不处理
    if (paramType == SAMPLE_TYPE){
        switch (value0) {
            case SAMPLE_TYPE_KEY_TRANSITIONS_1:
                mPCurShader = new Gltransition();
                break;
            case SAMPLE_TYPE_KEY_SHADER_TOY:
                mPCurShader = new ShaderTopTest();
                break;
            default:
                mPCurShader = nullptr;
        }
    }
}

void MyNativeGlRenderContext::OnDrawFrame() {
//    LogutilsI("MyNativeGlRenderContext::OnDrawFrame ");
    //清楚掉颜色和 深度的信息 每一次重新开始绘制
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    if(mPreShader){
        mPreShader->Destroy();
        delete mPreShader;
        mPreShader = nullptr;
    }

    if(mPCurShader){
//        mPCurShader->Init();
        mPCurShader->Draw(screenWidth,screenHeight);
    }

}



