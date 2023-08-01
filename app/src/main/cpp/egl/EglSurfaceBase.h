

#ifndef OPENGLDEMO_EGLSURFACEBASE_H
#define OPENGLDEMO_EGLSURFACEBASE_H

#include "EglCore.h"

class EglSurfaceBase {
public:
    EglSurfaceBase(EglCore *eglCore);

    //创建窗口surface
    void createEglWindow(ANativeWindow *aNativeWindow);

    //创建离屏surface
    void createOffScreenSurface(int width,int height);

    int getWidth();
    int getHeight();

    //释放eglSurface
    int releaseEglSurface();

    //切换上下文
    void makeCurrent();

    //交换前后台显示Buffer
    bool swapBuffers();

    //设置时间戳
    void setPts(long nsec);
    //获取当前帧
    char *getCurrentFrame();

protected:
    EglCore *mEglCore;
    EGLSurface mEglSurface;

    int mWidth;
    int mHeight;

};


#endif //OPENGLDEMO_EGLSURFACEBASE_H
