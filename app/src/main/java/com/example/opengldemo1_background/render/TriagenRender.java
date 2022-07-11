package com.example.opengldemo1_background.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/*
* 本类实现一个在屏幕画个三角形
* */
public class TriagenRender implements GLSurfaceView.Renderer {

    FloatBuffer floatBuffer ;
    int progrem;
    private int vPosition;
    int colorHandle;
    //设置颜色，依次为红绿蓝和透明通道
    float color[] = { 1.0f, 0f, 0f, 1.0f };

    //三角形定点坐标
    float titagenrender  [] = {
        0.5f, 0.5f, 0f,
        -0.5f,-0.5f,0f,
        0.5f,-0.5f,0f
    };

    //定点着色器
    private final String vertexShaderCode =
            "attribute vec4 vPosition; " +
                    "void main() {" +
                    "gl_Position = vPosition;" +
                    "}";
    //片元着色器
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private String TAG  = "TriagenRender";
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG,"onSurfaceCreated 1");
        //1: 申请CPU内存空间
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(titagenrender.length  *4);
        byteBuffer.order(ByteOrder.nativeOrder());
        floatBuffer = byteBuffer.asFloatBuffer();

        Log.i(TAG,"onSurfaceCreated 2");
        // 2:将所要做的三角形的坐标输入floatbytebuffer
        floatBuffer.put(titagenrender);
        floatBuffer.position(0);

        Log.i(TAG,"onSurfaceCreated 3");
        //3:创建定点着色器和片元着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        Log.i(TAG,"onSurfaceCreated 4");
        // 4:创建承载着色器的opengl 方法,并将点点着色器和片元着色器加入
        progrem = GLES20.glCreateProgram();
        GLES20.glAttachShader(progrem,vertexShader);
        GLES20.glAttachShader(progrem,fragmentShader);

        Log.i(TAG,"onSurfaceCreated 5");
        //5：连接程序
        GLES20.glLinkProgram(progrem);

    }
    //创建着色器 （本方法主要为定点着色器和 片元着色器）
    private int loadShader(int type,String shaderCode){
        //根据类型创建着色器
        int shade = GLES20.glCreateShader(type);
        //加载资源
        GLES20.glShaderSource(shade,shaderCode);
        GLES20.glCompileShader(shade);
        return shade;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.i(TAG,"onDrawFrame 1");
        //1：获取程序中的句柄并启用
        GLES20.glUseProgram(progrem);
        vPosition = GLES20.glGetAttribLocation(progrem, "vPosition");
        GLES20.glEnableVertexAttribArray(vPosition);
//        GLES20.glEnable(vPosition);

        Log.i(TAG,"onDrawFrame 2");
        // 2:准备三角形的定点坐标数据
        GLES20.glVertexAttribPointer(vPosition,3,GLES20.GL_FLOAT, false,3*4,floatBuffer);

        Log.i(TAG,"onDrawFrame 3");
        //3:准备着色器片元
        colorHandle = GLES20.glGetUniformLocation(progrem, "vColor");
        GLES20.glUniform4fv(colorHandle,1,color,0);

        Log.i(TAG,"onDrawFrame 4");
        // 4:绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,3);
        GLES20.glDisableVertexAttribArray(vPosition);
    }
}
