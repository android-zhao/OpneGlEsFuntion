package com.example.opengldemo1_background.activity;


import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.opengldemo1_background.R;
import com.example.opengldemo1_background.render.TriagenRender;

public class SimpleRenderActivity extends Activity {


    GLSurfaceView glSurfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_render);

        initView();
    }

    void initView(){

        glSurfaceView = findViewById(R.id.surfaceview_opengles);
//        glSurfaceView.setElevation(2.0f);
        //设置onengl的版本
        glSurfaceView.setEGLContextClientVersion(2);

        //设置渲染器 自实现  背景颜色改变
//        glSurfaceView.setRenderer(new BackGroundRender());

        //设置渲染器 自实现  画三角形
        glSurfaceView.setRenderer(new TriagenRender());

        //设置渲染模式
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

}