//
// Created by didi on 2023/5/17.
//

#ifndef OPENGLDEMO_OFFSCREENSURFACE_H
#define OPENGLDEMO_OFFSCREENSURFACE_H

#include "EglSurfaceBase.h"

class OffscreenSurface :EglSurfaceBase{
public:
    OffscreenSurface(EglCore *eglCore,int width,int height);
    void release();
};


#endif //OPENGLDEMO_OFFSCREENSURFACE_H

