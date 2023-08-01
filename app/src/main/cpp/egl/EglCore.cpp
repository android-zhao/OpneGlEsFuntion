
#include "EglCore.h"
#include "Logutils.h"
#include <EGL/egl.h>
#include <EGL/eglext.h>


EglCore::EglCore() {
    init(NULL,0);
}

//初始化完成，就已经完成了前四部
//1：获取 EGL Display 对象：eglGetDisplay()
//2：初始化与 EGLDisplay 之间的连接：eglInitialize()
//3：获取 EGLConfig 对象：eglChooseConfig()
//4：创建 EGLContext 实例：eglCreateContext()

EglCore::EglCore(EGLContext sharedContext, int flag) {
    init(sharedContext,flag);
}

EglCore::~EglCore(){
    release();
}

bool EglCore::release() {
    //先处理掉eglDisplay，eglContext，release线程，最后发送Terminate指令
    if(mEglDisplay != EGL_NO_DISPLAY){
        eglMakeCurrent(mEglDisplay,EGL_NO_SURFACE,EGL_NO_SURFACE,EGL_NO_CONTEXT);
        eglDestroyContext(mEglDisplay,mEglContext);
        eglReleaseThread();
        eglTerminate(mEglDisplay);
    }

    mEglDisplay = EGL_NO_DISPLAY;
    mEglContext = EGL_NO_CONTEXT;
    mEglConfig = nullptr;

}

bool EglCore::init(EGLContext sharedContext, int flag) {
    if(mEglDisplay == EGL_NO_DISPLAY){
        LogutilsE("init");
    }
    //正常的入参检查
    if(mEglDisplay != EGL_NO_DISPLAY ){
        LogutilsE("init ,already init end");
        return false;
    }

    if (sharedContext == NULL) {
        sharedContext = EGL_NO_CONTEXT;
        LogutilsE("init,sharedContext  is null");
        return false;
    }

    //第一步：创建  EglDisplay 通过api eglGetDisplay，创建完成需要检查 面向过程语言的繁琐之处
    mEglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);

    if (mEglDisplay == EGL_NO_DISPLAY) {
        LogutilsE("unable to get EGL14 display.\n");
        return false;
    }

    //第二步: opengl 初始化egl环境，且根据返回值检查结果，若返回失败直接创建失败
    if(!eglInitialize(mEglDisplay,0,0)){
        mEglDisplay = EGL_NO_DISPLAY;
        LogutilsE("unable to initialize EGL14");
        return false;
    }

    //策略优先处理gles 3.0版本
    if(flag  & FLAG_TRY_GLES3){
        EGLConfig config  = getEglConfig(flag,3);

        if(config != NULL){
            int attrib3_list[] = {
                    EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL_NONE
            };
            //第四步：创建eglcontext eglCreateContext
            EGLContext context = eglCreateContext(mEglDisplay,config,sharedContext,attrib3_list);
            checkEglError("eglCreateContext");
            if(eglGetError() == EGL_SUCCESS){
                mEglConfig = config;
                mEglContext = context;
                mGlVersion = 3;
            }
        }
    }

    //如果opengl 3.0未初始化完成，则使用opengl 2.0
    if(mEglContext == EGL_NO_CONTEXT){
        EGLConfig config = getEglConfig(flag, 2);

        int attrib2_list[] = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        };
        //第三部步：创建eglcontext eglCreateContext
        EGLContext context = eglCreateContext(mEglDisplay, config,
                                              sharedContext, attrib2_list);
        checkEglError("eglCreateContext");
        if (eglGetError() == EGL_SUCCESS) {
            mEglConfig = config;
            mEglContext = context;
            mGlVersion = 2;
        }
    }

    eglPresentationTimeANDROID = reinterpret_cast<EGL_PRESENTATION_TIME_ANDROIDPROC>(eglGetProcAddress(
            "eglPresentationTimeANDROID"));
    if( !eglPresentationTimeANDROID ){
        LogutilsE("init eglGetProcAddress failed");
    }

    int value[1] = {0};
    eglQueryContext(mEglDisplay,mEglContext,EGL_CONTEXT_CLIENT_VERSION,value);
    LogutilsE("init EGLContext created, client version %d", value[0]);
    return true;
}

void EglCore::checkEglError(const char *msg) {
    int error;
    if ((error = eglGetError()) != EGL_SUCCESS) {
        // TODO 抛出异常
        LogutilsE("%s: EGL error: %x", msg, error);
    }
}

EGLConfig EglCore::getEglConfig(int flags, int version) {
    //绘制位数
    int renderableType = EGL_OPENGL_ES2_BIT;

    if(version >= 3){
        renderableType |= EGL_OPENGL_ES3_BIT_KHR;
    }
    int attribList[] = {

            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_DEPTH_SIZE, 16,
            EGL_STENCIL_SIZE, 8,
            EGL_RENDERABLE_TYPE, renderableType,
            EGL_NONE, 0,      // placeholder for recordable [@-3]
            EGL_NONE
    };
   int length = sizeof (attribList)/sizeof (attribList[0]);

   if(  (flags & FLAG_RECORDABLE) != 0  ){
       attribList[length -3] = EGL_RECORDABLE_ANDROID;
       attribList[length -2] = 1;
   }
   EGLConfig  config;
   int numConfigs ;
    //第三步：获取配置的 EGLConfig对象  eglChooseConfig
   if( !eglChooseConfig(mEglDisplay,attribList,&config,1,&numConfigs) ){
       LogutilsE("unable to find RGB8888 / %d  EGLConfig", version);
       return NULL;
   }
   return config;
}

EGLContext EglCore::getEglContext() {
    return mEglContext;
}


void EglCore::releaseSurface(EGLSurface surface) {
    eglDestroySurface(mEglDisplay,surface);
}

//第五步 :创建EglSurface
EGLSurface EglCore::createWindowSurface(ANativeWindow *surface) {
    LogutilsI("createWindowSurface" );
    if (surface == NULL) {
        LogutilsE("ANativeWindow is NULL!");
        return NULL;
    }

    int surfaceAttribs[] = {
            EGL_NONE
    };

    EGLSurface  eglSurface =
            eglCreateWindowSurface(mEglDisplay,mEglConfig,surface,surfaceAttribs);
    LogutilsE("createWindowSurface eglCreateWindowSurface end");

    if (eglSurface == NULL) {
        LogutilsE("EGLSurface is NULL!");
        return NULL;
    }
    return eglSurface;
}

EGLSurface EglCore::createOffScreenSurface(int width, int height) {
    LogutilsI("createOffScreenSurface width:%d, height:%d" ,width,height);
    int surfaceAttribs[] = {
            EGL_WIDTH, width,
            EGL_HEIGHT, height,
            EGL_NONE
    };
    EGLSurface offScreenSurface  =
            eglCreatePbufferSurface(mEglDisplay,mEglConfig,surfaceAttribs);
    if(offScreenSurface == NULL){
        LogutilsE("createOffScreenSurface offScreenSurface is null");
        return nullptr;
    }
    return offScreenSurface;
}

void EglCore::makeCurrent(EGLSurface surface) {
    LogutilsI("makeCurrent");

    if (mEglDisplay == EGL_NO_DISPLAY) {
        LogutilsI("Note: makeCurrent w/o display.\n");
    }

    if(!eglMakeCurrent(mEglDisplay,surface,surface,mEglContext)){
        LogutilsE("makeCurrent failed");
    }

}

void EglCore::makeCurrent(EGLSurface drawSurface, EGLSurface readSurface) {
    if (mEglDisplay == EGL_NO_DISPLAY) {
        LogutilsI("Note: makeCurrent w/o display.\n");
    }
    if (!eglMakeCurrent(mEglDisplay, drawSurface, readSurface, mEglContext)) {
        // TODO 抛出异常
    }
}

/**
 *
 */
void EglCore::makeNothingCurrent() {
    if (!eglMakeCurrent(mEglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)) {
        // TODO 抛出异常
    }
}


bool EglCore::swapBuffer(EGLSurface eglSurface) {
    return eglSwapBuffers(mEglDisplay,eglSurface);
}


void EglCore::setPresentationTime(EGLSurface surface, long nasecs) {
    eglPresentationTimeANDROID(mEglDisplay,surface,nasecs);
}


bool EglCore::isCurrent(EGLSurface surface) {
    return mEglContext == eglGetCurrentContext() &&
       surface == eglGetCurrentSurface(EGL_DRAW);
}

bool EglCore::querySurface(EGLSurface surface, int what) {
    int value ;
    eglQuerySurface(mEglDisplay,surface, what, &value);
    return value;
}

/**
 * 查询字符串
 * @param what
 * @return
 */
 const char* EglCore::queryString(int what) {
    return eglQueryString(mEglDisplay, what);
}

/**
 * 获取GLES版本号
 * @return
 */
int EglCore::getGlVersion() {
    return mGlVersion;
}






