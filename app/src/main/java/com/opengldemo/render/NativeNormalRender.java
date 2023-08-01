package com.opengldemo.render;

public class NativeNormalRender {

    static {
        System.loadLibrary("native-lib");
    }

    public native void native_UpdateTransformMatrix(float rotateX, float rotateY,
                                                    float scaleX, float scaleY);
    public native void native_SetParamsFloat( int  paramType,float x, float y);

    public native void nativeInit();

    public native void nativeUninit();

    public native void native_SetParamsInt(int paramType, int value0, int value1);

    public native void native_SetImageData(int format, int width, int height, byte[] bytes);

    public native void native_SetImageDataWithIndex(int index, int format, int width, int height, byte[] bytes);

    public native void native_SetAudioData(short[] audioData);

    public native void native_OnSurfaceCreated();

    public native void native_OnSurfaceChanged(int width, int height);

    public native void native_OnDrawFrame();

}
