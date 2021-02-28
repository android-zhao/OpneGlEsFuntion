package com.example.opengldemo1_background;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    GLSurfaceView glSurfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }


}