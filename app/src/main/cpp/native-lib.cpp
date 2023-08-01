#include <jni.h>
#include <string>
#include "Logutils.h"
#include "EglRender.h"
#include "MyNativeGlRenderContext.h"


static int registerNativeMethod(JNIEnv *env,const char *classname,JNINativeMethod *methods,int count);

using namespace std;
#define CLASS_NAME1  "com/opengldemo/render/NativeEglRender"
#define CLASS_NAME2  "com/opengldemo/render/NativeNormalRender"
#define CLASS_NAME3  "com/opengldemo/render/ShaderToyRender"

JNIEXPORT  void JNICALL  native_EglRenderInit(JNIEnv *jniEnv, jobject instance){
    EglRender::getInstance()->Init();
}

JNIEXPORT  void JNICALL native_UpdateTransformMatrix(JNIEnv *jniEnv, jobject instance,
                                                     jfloat rotateX,jfloat rotateY,
                                                     jfloat scaleX, jfloat scaleY){

}

JNIEXPORT  void JNICALL native_Init(JNIEnv *jniEnv, jobject instance){
    MyNativeGlRenderContext::getInstance();
}

JNIEXPORT void JNICALL native_UnInit(JNIEnv *jniEnv, jobject instance){

}

JNIEXPORT void JNICALL native_SetImageData(JNIEnv *env, jobject instance,jint format, jint width, jint height, jbyteArray imageData){

}

JNIEXPORT void JNICALL native_SetImageDataWithIndex
        (JNIEnv *env, jobject instance, jint index, jint format, jint width, jint height, jbyteArray imageData)
{
        int imageDatLen = env->GetArrayLength(imageData);
        uint8_t *imageDataJni = new uint8_t [imageDatLen];
        env->GetByteArrayRegion(imageData,0,imageDatLen, reinterpret_cast<jbyte*>(imageDataJni) );
        MyNativeGlRenderContext::getInstance()->SetImageDataWithIndex(index,format,width,height,imageDataJni);
        //new 出来的指针是指向 数组的 delete 时也需要 带上[]
        delete[] imageDataJni;
        //大对象 在java 创建，使用完毕，采用DeleteLocalRef 告诉GC回收期可以回收，如果不使用 DeleteLocalRef 则会阻止GC 回收，变相相当于出现了内存泄漏
        env->DeleteLocalRef(imageData);
}

JNIEXPORT void JNICALL native_SetParamsInt
        (JNIEnv *env, jobject instance, jint paramType, jint value0, jint value1)
{
        MyNativeGlRenderContext::getInstance()->SetParamsInt(paramType,value0,value1);
}

JNIEXPORT void JNICALL native_SetParamsFloat
        (JNIEnv *env, jobject instance, jint paramType, jfloat value0, jfloat value1)
{

}


JNIEXPORT void JNICALL native_SetAudioData
        (JNIEnv *env, jobject instance, jshortArray data)
{

}

JNIEXPORT void JNICALL native_OnSurfaceCreated(JNIEnv *env, jobject instance)
{
    MyNativeGlRenderContext::getInstance()->OnSurfaceCreated();
}

JNIEXPORT void JNICALL native_OnSurfaceChanged
        (JNIEnv *env, jobject instance, jint width, jint height)
{
    MyNativeGlRenderContext::getInstance()->OnSurfaceChanged(width,height);
}

JNIEXPORT void JNICALL native_OnDrawFrame(JNIEnv *env, jobject instance)
{
    MyNativeGlRenderContext::getInstance()->OnDrawFrame();
}


JNIEXPORT  void JNICALL native_EglRenderSetImageData(JNIEnv *jniEnv,jobject instance,
                                                     jbyteArray data,jint width, jint height){
    int length = jniEnv->GetArrayLength(data);
    //2种创建方式，一种C语言方式 一种C++
//    uint8_t * buff1= (uint8_t   *)malloc( (size_t) length ); //C语言
    uint8_t * buff= new uint8_t [length];//C++ 创建
//    reinterpret_cast  c++中理解 简单来说就是 针对指针进行转换的，int* 强转成 char*  指针 ，
//    强转完成之后 指向的还是同一块内存，但是装换完成之后 就会按照转换成之后的指针类型 进行内存读取
    jniEnv->GetByteArrayRegion(data, 0, length, reinterpret_cast<jbyte *>(buff));

    EglRender::getInstance()->setImageData(buff,width,height);

    delete []buff;
    jniEnv->DeleteLocalRef(data);
    LogutilsI("native_EglRenderSetImageData END");

}

//todo 暂时不实现 默认只有一种shander实现 为了实现多种效果切换接口
JNIEXPORT void JNICALL native_EglRenderSetIntParams(JNIEnv *jniEnv,jobject instance,jint type, jint param){

}

JNIEXPORT void JNICALL native_EglRenderDraw(JNIEnv *jniEnv,jobject instance){
    LogutilsI("native_EglRenderDraw begin");
    EglRender::getInstance()->Draw();
}

JNIEXPORT void JNICALL natuve_BgRenderUnInit(JNIEnv *jniEnv,jobject instance){

}

extern "C"
JNIEXPORT void JNICALL
shaderToy_onSurfaceCreate(JNIEnv *env, jobject thiz) {
    LogutilsI("ZHF shaderToy_onSurfaceCreate");
}


extern "C"
JNIEXPORT void JNICALL
shaderToy_onInit(JNIEnv *env, jobject thiz) {
    LogutilsI("shaderToy_onInit");
    MyNativeGlRenderContext::getInstance()->SetParamsInt(200,SAMPLE_TYPE_KEY_SHADER_TOY,0);
}

extern "C"
JNIEXPORT void JNICALL
shaderToy_onSurfaceChange(JNIEnv *env, jobject thiz,int width,int height) {
    LogutilsI("ZHF shaderToy_onSurfaceChange width %d,height:%d",width,height);
    MyNativeGlRenderContext::getInstance()->OnSurfaceChanged(width,height);
}

extern "C"
JNIEXPORT void JNICALL
shaderToy_onRenderDraw(JNIEnv *env, jobject thiz) {
    MyNativeGlRenderContext::getInstance()->OnDrawFrame();
}


static JNINativeMethod NativeEglRender_methods[] = {
        {"native_EglRenderInit",          "()V",       (void *)(native_EglRenderInit)},
        {"native_EglRenderSetImageData",  "([BII)V",   (void *)(native_EglRenderSetImageData)},
        {"native_EglRenderSetIntParams",  "(II)V",     (void *)(native_EglRenderSetIntParams)},
        {"native_EglRenderDraw",          "()V",       (void *)(native_EglRenderDraw)},
        {"native_EglRenderUnInit",        "()V",       (void *)(natuve_BgRenderUnInit)},
};

static JNINativeMethod NativeNormalRender_methods[] = {
        {"native_UpdateTransformMatrix",     "(FFFF)V",   (void *)native_UpdateTransformMatrix},
        {"nativeInit",                       "()V",       (void *)native_Init},
        {"nativeUninit",                    "()V",       (void *)(native_UnInit)},
        {"native_SetImageData",              "(III[B)V",  (void *)(native_SetImageData)},
        {"native_SetImageDataWithIndex",     "(IIII[B)V", (void *)(native_SetImageDataWithIndex)},
        {"native_SetParamsInt",              "(III)V",    (void *)(native_SetParamsInt)},
        {"native_SetParamsFloat",            "(IFF)V",    (void *)(native_SetParamsFloat)},
        {"native_SetAudioData",              "([S)V",     (void *)(native_SetAudioData)},
        {"native_OnSurfaceCreated",          "()V",       (void *)(native_OnSurfaceCreated)},
        {"native_OnSurfaceChanged",          "(II)V",     (void *)(native_OnSurfaceChanged)},
        {"native_OnDrawFrame",               "()V",       (void *)(native_OnDrawFrame)},

};

static JNINativeMethod NativeShaderToyRender_methods[] = {
        {"onInit",          "()V",       (void *)(shaderToy_onInit)},
        {"onSurfaceCreated",  "()V",   (void *)(shaderToy_onSurfaceCreate)},
        {"onSurfaceChanged",  "(II)V",     (void *)(shaderToy_onSurfaceChange)},
        {"onDrawFrame",          "()V",       (void *)(shaderToy_onRenderDraw)},
};


extern "C" jint  JNI_OnLoad(JavaVM *jvm, void *unused) {
    LogutilsI("===JNI_OnLoad====");

    jint jniRet = JNI_ERR;
    JNIEnv *env = NULL;

    if (jvm->GetEnv((void **) (&env), JNI_VERSION_1_6) != JNI_OK)
    {
        return jniRet;
    }


    jint regRet = registerNativeMethod(env,CLASS_NAME1,NativeEglRender_methods,
                         (  sizeof (NativeEglRender_methods) / sizeof (NativeEglRender_methods[0])  )
                         );
    if(regRet != JNI_TRUE){
        return JNI_ERR;
    }

    jint regRet2 = registerNativeMethod(env,CLASS_NAME2,NativeNormalRender_methods,
                                        (  sizeof (NativeNormalRender_methods) / sizeof (NativeNormalRender_methods[0])  )
    );
    if(regRet2 != JNI_TRUE){
        return JNI_ERR;
    }

    jint regRet3 = registerNativeMethod(env,CLASS_NAME3,NativeShaderToyRender_methods,
                                        (  sizeof (NativeShaderToyRender_methods) / sizeof (NativeShaderToyRender_methods[0])  )
                                        );
    if(regRet3 != JNI_TRUE){
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

static  int registerNativeMethod(JNIEnv *env,const char *classname,JNINativeMethod *methods,int count){
    LogutilsI("====registerNativeMethod====");
    jclass  jclass1 = env->FindClass(classname);

    if (jclass1 == NULL)
    {
        LogutilsI("registerNativeMethod fail. clazz == NULL");
        return JNI_FALSE;
    }

    if(env->RegisterNatives(jclass1,methods,count) < 0 ){
        LogutilsI("registerNativeMethod fail");
        return JNI_FALSE;
    }

    return JNI_TRUE;

}