package com.opengldemo.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;
import com.opengldemo.filter.WatermarkFilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class WaterMarkRender implements GLSurfaceView.Renderer {

    private Context mContext;
    private FloatBuffer floatBuffer = null;
    public static final int BYTES_PER_FLOAT = 4;
    private WatermarkFilter mWatermarkFilter;
    public WaterMarkRender(Context context) {
        mContext = context;
        createVertexArray();

    }
    void createVertexArray(){
        //1：创建顶点坐标和纹理坐标系
        float CUBE[] = {
                //翻转顶点信息中的纹理坐标,统一用1去减   //一张图相当于上下对调 左上顶点和左下顶掉对调，右上顶点和右下顶点对调
                -1.0f, -1.0f,  /* 顶点0*/ 0f, 1f - 0f,/* 纹理0*/
                1.0f, -1.0f, /* 顶点1*/ 1f,   1f -0f, /* 纹理1*/
                -1.0f, 1.0f, /* 顶点2 */0f,  1f -1f, /* 纹理2*/
                1.0f, 1.0f, /* 顶点3 */ 1f,  1f -1f, /* 纹理3*/
        };
//        float CUBE[] = {
//                  //纹理贴图是上下颠倒
//                -1.0f, -1.0f,  /* 顶点0*/ 0f, 0f,/* 纹理0*/
//                1.0f, -1.0f, /* 顶点1*/ 1f,   0f, /* 纹理1*/
//                -1.0f, 1.0f, /* 顶点2 */0f,  1f , /* 纹理2*/
//                1.0f, 1.0f, /* 顶点3 */ 1f,  1f, /* 纹理3*/
//        };


        floatBuffer = ByteBuffer
                .allocateDirect(CUBE.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(CUBE);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 0.0f, 0.0f);
        updateTexture();
        updateTextureAndVertxy();
//        mWatermarkFilter.setWaterMarkInfo(100,200,"open gl Demo",12);
        mWatermarkFilter = new WatermarkFilter(mContext,"open gl Demo",800,200,20);
    }

    int glsurface_width,glsurface_height;
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        glViewport(0, 0, width, height);
        glsurface_width = width;
        glsurface_height = height;
        Log.i(TAG,"onSurfaceChanged " + "width： "+width + "，height：" +height );

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        drawTexture();
        mWatermarkFilter.onDraw();
    }
    //顶点坐标数量
    private static final int POSITION_COMPONENT_COUNT = 2;
    //纹理值坐标
    private static final int TEXTURE_COMPONENT_COUNT = 2;
    // 顶点坐标 + 纹理坐标合成的stride
    private static final int STRIDE_VERTEX_TEXTURE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT)
                    * BYTES_PER_FLOAT;
    private float[] scaleMatrix = new float[16];
    private void drawTexture(){
        glViewport(0, 0, glsurface_width, glsurface_height);

        glClear(GL_COLOR_BUFFER_BIT);
        //开始使用 gpu小程序
        glUseProgram(mRendeId);

        //设置mvp矩阵
        Matrix.setIdentityM(scaleMatrix,0);
        //指定不扩大
        Matrix.scaleM(scaleMatrix,0,1,1,0);

        glUniformMatrix4fv(uMvpMatrixLocation,1,false,scaleMatrix,0);

        //激活纹理并通知采样函数去哪个纹理插槽去采样
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,mTextureId);
        glUniform1i(uTextureUnitLocation,0);


        //设置顶点坐标
        floatBuffer.position(0);
        glVertexAttribPointer(aPositionLocation,POSITION_COMPONENT_COUNT,
                GL_FLOAT,false, STRIDE_VERTEX_TEXTURE,floatBuffer);
        glEnableVertexAttribArray(aPositionLocation);
        floatBuffer.position(0);

        //纹理坐标使能
        floatBuffer.position(POSITION_COMPONENT_COUNT);

        glVertexAttribPointer(aTextureCoordinatesLocation, TEXTURE_COMPONENT_COUNT
                , GL_FLOAT,
                false, STRIDE_VERTEX_TEXTURE, floatBuffer);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);
        floatBuffer.position(0);

        // 开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
    private int mTextureId =-1;
    private String TAG = "WaterMarkRender";
    private int mRendeId = -1;

    private  int aPositionLocation = -1;
    private  int aColorLocation = -1;
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";

    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private static final String U_MVP_POSITION = "uMvpMatrix";

    private int aTextureCoordinatesLocation = -1;
    private int uMvpMatrixLocation = -1;

    private int uTextureUnitLocation = -1;
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    private void updateTexture() {
        mTextureId = GLesUtils.loadjpg(mContext);
        Log.i(TAG,"updateTexture: mTextureId" +mTextureId);
        //默认0slot位置上纹理
//        glActiveTexture(GL_TEXTURE0);
//        glBindTexture(GL_TEXTURE_2D,textureId);
    }
    private void updateTextureAndVertxy() {

        //2：创建shader
        updateTextureShader();

        //3：指定顶点坐标和shader中由cpu设置的相关参数
        // 纹理采样器 id uTextureUnitLocation
        uTextureUnitLocation =  glGetUniformLocation(mRendeId, U_TEXTURE_UNIT);
        //顶点坐标 顶点0 1 2 3
        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
        //纹理坐标 在cube 数组中声明的
        aTextureCoordinatesLocation = glGetAttribLocation(mRendeId, A_TEXTURE_COORDINATES);
        uMvpMatrixLocation = glGetUniformLocation(mRendeId,U_MVP_POSITION);
        Log.i(TAG,"updateTextureAndVertxy floatBuffer size :" + floatBuffer.capacity());
        Log.i(TAG,"updateTextureAndVertxy uTextureUnitLocation  :" + uTextureUnitLocation);
        Log.i(TAG,"updateTextureAndVertxy aPositionLocation  :" + aPositionLocation);
        Log.i(TAG,"updateTextureAndVertxy mRendeId  :" + mRendeId);
        Log.i(TAG,"updateTextureAndVertxy aTextureCoordinatesLocation  :" + aTextureCoordinatesLocation);
        Log.i(TAG,"updateTextureAndVertxy uMvpMatrixLocation  :" + uMvpMatrixLocation);

        Log.i(TAG,"updateTextureAndVertxy end");
    }
    //指定glsl脚本
    private void updateTextureShader() {
        String vertxtStr  = null;
        String framentStr = null;
        vertxtStr = GLesUtils.readTextFileFromResource(mContext, R.raw.simple_watermark_texture_vertex);
        framentStr = GLesUtils.readTextFileFromResource(mContext,R.raw.simple_watermark_texture_framge);
        mRendeId =GLesUtils.buildProgram(vertxtStr, framentStr);
        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
        aColorLocation = glGetAttribLocation(mRendeId, A_COLOR);
    }
}
