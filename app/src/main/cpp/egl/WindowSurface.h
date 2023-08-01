//
// Created by didi on 2023/5/13.
//

#ifndef OPENGLDEMO_WINDOWSURFACE_H
#define OPENGLDEMO_WINDOWSURFACE_H

#include "EglSurfaceBase.h"
#include <android/native_window.h>

class WindowSurface  : EglSurfaceBase{

public:
    WindowSurface(ANativeWindow *surface,EglCore *mEglCore,bool isReleaseSurface);
    WindowSurface(ANativeWindow *surface,EglCore *mEglCore);

    void release();
    void recreate(EglCore *eglCore);
private:

    ANativeWindow *mSurface;
    bool mIsReleaseSurface;

};


#endif //OPENGLDEMO_WINDOWSURFACE_H
