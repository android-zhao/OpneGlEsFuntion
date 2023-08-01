package com.opengldemo.render;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.Printer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyNativeGLRenderProxy implements GLSurfaceView.Renderer {

    private String TAG = "MyNativeGLRenderProxy";


    public static final int SAMPLE_TYPE  =  200;

    public static final int SAMPLE_TYPE_TRIANGLE                = SAMPLE_TYPE;
    public static final int SAMPLE_TYPE_TEXTURE_MAP             = SAMPLE_TYPE + 1;
    public static final int SAMPLE_TYPE_YUV_TEXTURE_MAP         = SAMPLE_TYPE + 2;
    public static final int SAMPLE_TYPE_VAO                     = SAMPLE_TYPE + 3;
    public static final int SAMPLE_TYPE_FBO                     = SAMPLE_TYPE + 4;
    public static final int SAMPLE_TYPE_EGL                     = SAMPLE_TYPE + 5;
    public static final int SAMPLE_TYPE_FBO_LEG                 = SAMPLE_TYPE + 6;
    public static final int SAMPLE_TYPE_COORD_SYSTEM            = SAMPLE_TYPE + 7;
    public static final int SAMPLE_TYPE_BASIC_LIGHTING          = SAMPLE_TYPE + 8;
    public static final int SAMPLE_TYPE_TRANS_FEEDBACK          = SAMPLE_TYPE + 9;
    public static final int SAMPLE_TYPE_MULTI_LIGHTS            = SAMPLE_TYPE + 10;
    public static final int SAMPLE_TYPE_DEPTH_TESTING           = SAMPLE_TYPE + 11;
    public static final int SAMPLE_TYPE_INSTANCING              = SAMPLE_TYPE + 12;
    public static final int SAMPLE_TYPE_STENCIL_TESTING         = SAMPLE_TYPE + 13;
    public static final int SAMPLE_TYPE_BLENDING                = SAMPLE_TYPE + 14;
    public static final int SAMPLE_TYPE_PARTICLES               = SAMPLE_TYPE + 15;
    public static final int SAMPLE_TYPE_SKYBOX                  = SAMPLE_TYPE + 16;
    public static final int SAMPLE_TYPE_3D_MODEL                = SAMPLE_TYPE + 17;
    public static final int SAMPLE_TYPE_PBO                     = SAMPLE_TYPE + 18;
    public static final int SAMPLE_TYPE_KEY_BEATING_HEART       = SAMPLE_TYPE + 19;
    public static final int SAMPLE_TYPE_KEY_CLOUD               = SAMPLE_TYPE + 20;
    public static final int SAMPLE_TYPE_KEY_TIME_TUNNEL         = SAMPLE_TYPE + 21;
    public static final int SAMPLE_TYPE_KEY_BEZIER_CURVE        = SAMPLE_TYPE + 22;
    public static final int SAMPLE_TYPE_KEY_BIG_EYES            = SAMPLE_TYPE + 23;
    public static final int SAMPLE_TYPE_KEY_FACE_SLENDER        = SAMPLE_TYPE + 24;
    public static final int SAMPLE_TYPE_KEY_BIG_HEAD            = SAMPLE_TYPE + 25;
    public static final int SAMPLE_TYPE_KEY_ROTARY_HEAD         = SAMPLE_TYPE + 26;
    public static final int SAMPLE_TYPE_KEY_VISUALIZE_AUDIO     = SAMPLE_TYPE + 27;
    public static final int SAMPLE_TYPE_KEY_SCRATCH_CARD        = SAMPLE_TYPE + 28;
    public static final int SAMPLE_TYPE_KEY_AVATAR              = SAMPLE_TYPE + 29;
    public static final int SAMPLE_TYPE_KEY_SHOCK_WAVE          = SAMPLE_TYPE + 30;
    public static final int SAMPLE_TYPE_KEY_MRT                 = SAMPLE_TYPE + 31;
    public static final int SAMPLE_TYPE_KEY_FBO_BLIT            = SAMPLE_TYPE + 32;
    public static final int SAMPLE_TYPE_KEY_TBO                 = SAMPLE_TYPE + 33;
    public static final int SAMPLE_TYPE_KEY_UBO                 = SAMPLE_TYPE + 34;
    public static final int SAMPLE_TYPE_KEY_RGB2YUYV            = SAMPLE_TYPE + 35;
    public static final int SAMPLE_TYPE_KEY_MULTI_THREAD_RENDER = SAMPLE_TYPE + 36;
    public static final int SAMPLE_TYPE_KEY_TEXT_RENDER         = SAMPLE_TYPE + 37;
    public static final int SAMPLE_TYPE_KEY_STAY_COLOR          = SAMPLE_TYPE + 38;
    public static final int SAMPLE_TYPE_KEY_TRANSITIONS_1       = SAMPLE_TYPE + 39;
    public static final int SAMPLE_TYPE_KEY_TRANSITIONS_2       = SAMPLE_TYPE + 40;
    public static final int SAMPLE_TYPE_KEY_TRANSITIONS_3       = SAMPLE_TYPE + 41;
    public static final int SAMPLE_TYPE_KEY_TRANSITIONS_4       = SAMPLE_TYPE + 42;
    public static final int SAMPLE_TYPE_KEY_RGB2NV21            = SAMPLE_TYPE + 43;
    public static final int SAMPLE_TYPE_KEY_RGB2I420            = SAMPLE_TYPE + 44;
    public static final int SAMPLE_TYPE_KEY_RGB2I444            = SAMPLE_TYPE + 45;
    public static final int SAMPLE_TYPE_KEY_COPY_TEXTURE        = SAMPLE_TYPE + 46;
    public static final int SAMPLE_TYPE_KEY_BLIT_FRAME_BUFFER   = SAMPLE_TYPE + 47;
    public static final int SAMPLE_TYPE_KEY_BINARY_PROGRAM      = SAMPLE_TYPE + 48;

    public static final int SAMPLE_TYPE_KEY_HWBuffer            = SAMPLE_TYPE + 50;

    public static final int SAMPLE_TYPE_SET_TOUCH_LOC           = SAMPLE_TYPE + 999;
    public static final int SAMPLE_TYPE_SET_GRAVITY_XY          = SAMPLE_TYPE + 1000;

    private NativeNormalRender mNativeRender;
    private int mSampleType;

    public MyNativeGLRenderProxy(){
        Log.i(TAG,"MyNativeGLRenderProxy");
        mNativeRender = new NativeNormalRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG,"onSurfaceCreated");
        mNativeRender.native_OnSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG,"onSurfaceChanged width:" + width +",height:" +height);
        mNativeRender.native_OnSurfaceChanged(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        Log.i(TAG,"onDrawFrame");
        mNativeRender.native_OnDrawFrame();
    }

    public void updateTransformMatrix(float rotateX, float rotateY, float scaleX, float scaleY)
    {
        mNativeRender.native_UpdateTransformMatrix(rotateX, rotateY, scaleX, scaleY);
    }

    public void setTouchLoc(float x, float y)
    {
        mNativeRender.native_SetParamsFloat(SAMPLE_TYPE_SET_TOUCH_LOC, x, y);
    }

    public int getSampleType() {
        return mSampleType;
    }

    public void init(){
        Log.i(TAG,"init");
        mNativeRender.nativeInit();
    }

    public void unInit(){
        mNativeRender.nativeUninit();
    }

    public void setImageDataWithIndex(int index,int format,int width,int height,byte[]imageData){
        mNativeRender.native_SetImageDataWithIndex(index,format,width,height,imageData);
    }

    public void setParamsInt(int paramType, int value0, int value1) {
        mNativeRender.native_SetParamsInt(paramType,value0,value1);
    }
}
