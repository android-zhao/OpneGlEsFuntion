//
// Created by didi on 2023/5/17.
//

#include "OffscreenSurface.h"

OffscreenSurface::OffscreenSurface(EglCore *eglCore, int width, int height) :EglSurfaceBase(eglCore){
    createOffScreenSurface(width,height);
}


void OffscreenSurface::release() {
    releaseEglSurface();
}

