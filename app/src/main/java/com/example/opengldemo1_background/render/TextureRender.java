package com.example.opengldemo1_background.render;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.Printer;

import com.example.opengldemo1_background.R;
import com.example.opengldemo1_background.utils.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextureRender implements GLSurfaceView.Renderer {

    private final  static  String TAG = "TextureRender";
    Context mContext;
    int textureId = -1 ;
    public TextureRender(Context context) {
        mContext = context;
        createBuffer();

    }

    private void loadPic() {
        textureId = OpenGlUtils.loadPic(R.raw.tescat2,mContext);
        if(textureId == -1){
            Log.e(TAG,"loadPic failed" );
            return;
        }
        Log.i(TAG,"loadPic ,textureId is  " +textureId);
    }

    private FloatBuffer mIndexBuffer = null;
    private float cube[] = {
        //顶点大小位置        纹理坐标
         -1.0f,-1.0f,       0.0f,0.0f,
          1.0f,-1.0f,       1.0f,0.0f,
          -1.0f,1.0f,       0.0f,1.0f,
          1.0f,1.0f,        1.0f,1.0f
    };
    //定点数量 一般是2个 或者3个
    private static final int POSITION_COMPONENT_COUNT = 2;

    //纹理坐标顶点数量
    private static final int TEXTURE_COMPONENT_COUNT = 2;

    //顶点中的Stride
    private static final int STRIDE_VERTEX_TEXTURE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT) * 4;



    //使用者顶点着色器和片元着色器的gpu程序在CPU对应的ID 值
    private int mRendeId = -1;
    //顶点着色器中的 顶点坐标
    private int aPositionLocation = -1;
    //顶点着色器中的  纹理坐标
    private int aTextureCoordinatesLocation = -1;
    //点段着色器中的 采样器坐标
    private int uTextureUnitLocation = -1;
    private static String A_POSITION  = "a_Position";
    private static String F_sample2D = "u_TextureUnit";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";


    private void updateShader(){
        String vertxtStr  = null;
        String framentStr = null;
        vertxtStr = OpenGlUtils.readTextFileFromResource(mContext, R.raw.test_load_texture_vertex);
        framentStr = OpenGlUtils.readTextFileFromResource(mContext,R.raw.test_load_texture_fragement);

        mRendeId =OpenGlUtils.buildProgram(vertxtStr, framentStr);
        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(mRendeId, A_TEXTURE_COORDINATES);
        uTextureUnitLocation = glGetAttribLocation(mRendeId,F_sample2D);

        Log.i(TAG,"updateShader floatBuffer size :" + mIndexBuffer.capacity());
        Log.i(TAG,"updateShader mRendeId  :" + mRendeId);
        Log.i(TAG,"updateShader aPositionLocation  :" + aPositionLocation);
        Log.i(TAG,"updateShader aTextureCoordinatesLocation  :" + aTextureCoordinatesLocation);
        Log.i(TAG,"updateShader uTextureUnitLocation  :" + uTextureUnitLocation);

    }
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.i(TAG,"onSurfaceCreated ");
        loadPic();
        updateShader();
    }

    private void createBuffer(){
        mIndexBuffer = ByteBuffer.allocateDirect(cube.length * 4).
                order(ByteOrder.nativeOrder()).asFloatBuffer().put(cube);
        mIndexBuffer.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        Log.i(TAG,"onSurfaceCreated ");
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        Log.i(TAG,"onDrawFrame ");
        drawTexture();
    }

    private void drawTexture(){
        glClear(GL_COLOR_BUFFER_BIT);
        //开始使用 gpu小程序
        glUseProgram(mRendeId);

        //激活纹理并通知采样函数去哪个纹理插槽去采样
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,textureId);
        glUniform1i(uTextureUnitLocation,0);

        //设置顶点坐标
        mIndexBuffer.position(0);
        glVertexAttribPointer(aPositionLocation,POSITION_COMPONENT_COUNT,
                GL_FLOAT,false, STRIDE_VERTEX_TEXTURE,mIndexBuffer);
        glEnableVertexAttribArray(aPositionLocation);
        mIndexBuffer.position(0);

        //纹理坐标使能
        mIndexBuffer.position(POSITION_COMPONENT_COUNT);

        glVertexAttribPointer(aTextureCoordinatesLocation, TEXTURE_COMPONENT_COUNT
                , GL_FLOAT,
                false, STRIDE_VERTEX_TEXTURE, mIndexBuffer);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);
        mIndexBuffer.position(0);

        // 开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
