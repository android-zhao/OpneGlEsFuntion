package com.opengldemo.render;

public class NativeEglRender {
    //初始化
    public native void native_EglRenderInit();
    //将java层 的图像数据传递到native层
    public native void native_EglRenderSetImageData(byte[] data, int width, int height);

    //传递native层绘制 参数
    public native void native_EglRenderSetIntParams(int paramType, int param);

    //使用native层绘制
    public native void native_EglRenderDraw();

    //native层生命周期销毁
    public native void native_EglRenderUnInit();
}
