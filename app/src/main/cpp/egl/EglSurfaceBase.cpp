
#include "EglSurfaceBase.h"
#include "Logutils.h"
#include <GLES2/gl2.h>

EglSurfaceBase::EglSurfaceBase(EglCore *eglCore):mEglCore(eglCore){
    LogutilsI("EglSurfaceBase");
    mEglSurface = EGL_NO_SURFACE;
}

void EglSurfaceBase::createEglWindow(ANativeWindow *aNativeWindow) {
    LogutilsI("createEglWindow");
    if(mEglSurface != EGL_NO_SURFACE){
        LogutilsI("createEglWindow surface already created");
        return ;
    }
    mEglSurface = mEglCore->createWindowSurface(aNativeWindow);
}


void EglSurfaceBase::createOffScreenSurface(int width, int height) {
    LogutilsI("createOffScreenSurface");

    if(mEglSurface != EGL_NO_SURFACE){
        LogutilsI("createOffScreenSurface surface already created");
        return ;
    }
    mEglSurface = mEglCore->createOffScreenSurface(width,height);
    mWidth = width;
    mHeight = height;
}

int EglSurfaceBase::getWidth() {
    if(mWidth < 0){
        return mEglCore->querySurface(mEglSurface,EGL_WIDTH);
    } else{
        return mWidth;
    }
}

int EglSurfaceBase::getHeight() {
    if(mHeight < 0){
        return mEglCore->querySurface(mEglSurface,EGL_HEIGHT);
    }else{
        return mHeight;
    }
}

int EglSurfaceBase::releaseEglSurface() {
    mEglCore->releaseSurface(mEglSurface);
    mEglSurface == EGL_NO_SURFACE;
    mWidth = mHeight = -1;
}

void EglSurfaceBase::makeCurrent() {
    mEglCore->makeCurrent(mEglSurface);
}

bool EglSurfaceBase::swapBuffers() {
    bool result = mEglCore->swapBuffer(mEglSurface);
    if(!result){
        LogutilsI("swapBuffers failed:" );
    }
    return result;
}

void EglSurfaceBase::setPts(long nsec) {
    mEglCore->setPresentationTime(mEglSurface,nsec);

}


char *EglSurfaceBase::getCurrentFrame() {
    char *pixels = NULL;
    glReadPixels(0,0,mWidth,mHeight,GL_RGBA,GL_UNSIGNED_BYTE,pixels);
    return pixels;
}