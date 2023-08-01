//
// Created by didi on 2023/5/13.
//

#include "WindowSurface.h"
#include "Logutils.h"

WindowSurface::WindowSurface(ANativeWindow *surface, EglCore *eglcore): EglSurfaceBase(eglcore) {
    LogutilsI("WindowSurface " );
    mSurface = surface;
    createEglWindow(mSurface);
}

WindowSurface::WindowSurface(ANativeWindow *surface, EglCore *eglcore,bool isReleaseSurface)
    : EglSurfaceBase(eglcore){
    LogutilsI("WindowSurface isReleaseSurface %d" ,isReleaseSurface);
    mSurface = surface;
    createEglWindow(mSurface);
    mIsReleaseSurface = isReleaseSurface;
}

void WindowSurface::release() {
    LogutilsI("WindowSurface release " );
    releaseEglSurface();
    if( mSurface != nullptr){
        ANativeWindow_release(mSurface);
        mSurface = nullptr;
    }
}

void WindowSurface::recreate(EglCore *eglCore) {
    if (mSurface == NULL) {
        LogutilsI("WindowSurface recreate,not yet implemented ANativeWindow");
        return;
    }
    mEglCore = eglCore;
    createEglWindow(mSurface);
}
