package com.example.opengldemo1_background.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class BackGroundRender implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //surface创建之后调用
        //设置背景颜色
        GLES20.glClearColor(0.5f,0.5f,0.5f,0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        //渲染窗口发生改变
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //执行渲染工作
        GLES20.glClearColor(0,0,0,0);
    }
}
