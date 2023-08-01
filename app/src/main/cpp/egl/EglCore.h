
#ifndef OPENGLDEMO_EGLCORE_H
#define OPENGLDEMO_EGLCORE_H
#include <EGL/egl.h>

#define FLAG_RECORDABLE 0X1
#define EGL_RECORDABLE_ANDROID 0x3142
#define FLAG_TRY_GLES3 002

typedef EGLBoolean (EGLAPIENTRYP EGL_PRESENTATION_TIME_ANDROIDPROC)(EGLDisplay display,EGLSurface surface,
        khronos_stime_nanoseconds_t time);
class EglCore {
private:
    EGLDisplay  mEglDisplay = EGL_NO_DISPLAY;
    EGLConfig mEglConfig = nullptr;
    EGLContext mEglContext = EGL_NO_CONTEXT;

    // 设置时间戳方法
    EGL_PRESENTATION_TIME_ANDROIDPROC eglPresentationTimeANDROID = nullptr;

    int mGlVersion = -1;

    EGLConfig getEglConfig(int flag,int version);
public:

    EglCore();
    ~EglCore();

    EglCore(EGLContext sharedContext,int flag);
    bool init(EGLContext sharedContext,int flag);

    //资源释放
    bool release();

    // 获取EglContext
    EGLContext getEglContext() ;
    //释放surface
    void releaseSurface(EGLSurface surface);

    //创建屏幕显示surface
    EGLSurface createWindowSurface(ANativeWindow *surface);

    //创建离屏的surface
    EGLSurface createOffScreenSurface(int width,int height);

    //切换当前上下文
    void makeCurrent(EGLSurface surface);

    //切换到某个上下文
    void makeCurrent(EGLSurface drawSurface,EGLSurface readSurface);

    //没有上下文
    void makeNothingCurrent();

    //交换显示
    bool swapBuffer(EGLSurface eglSurface);

    //设置PTS显示
    void  setPresentationTime(EGLSurface surface,long nasecs);

    //判断是否属于上下文
    bool isCurrent(EGLSurface surface);

    //查询surface
    bool querySurface(EGLSurface surface,int what);

    //查询字符串
    const char * queryString(int what);

    //查询当前egl的版本号
    int getGlVersion();

    //检查opengl是否出差
    void checkEglError(const char * error);
};


#endif //OPENGLDEMO_EGLCORE_H
