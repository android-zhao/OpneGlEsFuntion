package com.opengldemo.filter;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;
import com.opengldemo.TextureUtils;
import com.opengldemo.render.WaterMarkRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_DST_ALPHA;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE2;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

import androidx.annotation.NonNull;

public class WatermarkFilter {

    private String TAG = "WatermarkFilter";
    private Context mContext;
    String mWaterMarkInfo;
    int mWidth,mHeight,mSize;
    int textureId = -1;
    public static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 2;
    //颜色值坐标
    private static final int COLOR_COMPONENT_COUNT = 3;
    //纹理值坐标
    private static final int TEXTURE_COMPONENT_COUNT = 2;
    // 顶点坐标 + 纹理坐标合成的stride
    private static final int STRIDE_VERTEX_TEXTURE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT)
                    * BYTES_PER_FLOAT;
    // 顶点坐标 + 颜色坐标合成的stride
    private static final int STRIDE_VERTEX_COLOR =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT)
                    * BYTES_PER_FLOAT;
    private static Handler mHandler = null;

    @SuppressLint("HandlerLeak")
    public WatermarkFilter(Context context, String watermarkInfo, int width, int height, int texSize) {
        mContext = context;
        mWaterMarkInfo = watermarkInfo;
        mWidth = width;
        mHeight = height;
        mSize = texSize;
        //创建顶点数组 和程序
        createVexArray();
        //创建GPU小程序 即携带2个着色器的程序
        createProgram();

        //创建携带信息的texture
        createTexture();

       Thread  myThread = new Thread(mRunable);
       myThread.start();
    }


    public void setWaterMarkInfo(int width,int height,String waterInfo,int size){

    }

    public void onDraw(){
        glViewport(0, 0, mWidth, mHeight);

//        glEnable(GL_BLEND);
//        glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);
        if(isNeedUpdate){
            updateTextureId();
            isNeedUpdate = false;
        }
        glUseProgram(mRendeId);

        //指定纹理插槽
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D,textureId);
        glUniform1i(uTextureUnitLocation,2);

        //指定顶点设置
        floatBuffer.position(0);

        // stride
        // 参数传递小了 不正确 会导致画出来的界面是三角形 或者不显示
        // 传递大了 直接不显示
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, STRIDE_VERTEX_TEXTURE, floatBuffer);
        glEnableVertexAttribArray(aPositionLocation);
        floatBuffer.position(0);

        //指定纹理顶点设置
        floatBuffer.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(aTextureCoordinatesLocation,TEXTURE_COMPONENT_COUNT,
                GL_FLOAT,false, STRIDE_VERTEX_TEXTURE,floatBuffer);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);
        floatBuffer.position(0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisable(GLES20.GL_BLEND);
    }


    public static final float CUBE[] = {
            //翻转顶点信息中的纹理坐标,统一用1去减  //顶点坐标 + 纹理坐标
            -1.0f, -1.0f, 0f, 1f - 0f,
            1.0f, -1.0f, 1f, 1f -0f,
            -1.0f, 1.0f, 0f, 1f -1f,
            1.0f, 1.0f, 1f, 1f -1f,
    };

//    float CUBE[] = {
//            //翻转顶点信息中的纹理坐标,统一用1去减   //一张图相当于上下对调 左上顶点和左下顶掉对调，右上顶点和右下顶点对调
//            -1.0f, -1.0f,  /* 顶点0*/ 0f, 1f - 0f,/* 纹理0*/
//            1.0f, -1.0f, /* 顶点1*/ 1f,   1f -0f, /* 纹理1*/
//            -1.0f, 1.0f, /* 顶点2 */0f,  1f -1f, /* 纹理2*/
//            1.0f, 1.0f, /* 顶点3 */ 1f,  1f -1f, /* 纹理3*/
//    };

    public  FloatBuffer floatBuffer;
    private int mRendeId = -1;
    private int aPositionLocation = -1;
    private int aColorLocation = -1;
    private int uTextureUnitLocation =-1;
    private int aTextureCoordinatesLocation =-1;
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    private void createVexArray(){
        floatBuffer = ByteBuffer
                .allocateDirect(CUBE.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(CUBE);
        Log.i(TAG,"createVexArray END");
    }

    private void createProgram(){
        updateTextureShader();
    }

    private void updateTextureShader() {
        String vertxtStr  = null;
        String framentStr = null;
        vertxtStr = GLesUtils.readTextFileFromResource(mContext, R.raw.simple_watermark_texture_vertex);
        framentStr = GLesUtils.readTextFileFromResource(mContext,R.raw.simple_watermark_texture_framge);
        mRendeId =GLesUtils.buildProgram(vertxtStr, framentStr);
        //顶点坐标 顶点0 1 2 3
        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
        aColorLocation = glGetAttribLocation(mRendeId, A_COLOR);

        //3：指定顶点坐标和shader中由cpu设置的相关参数
        // 纹理采样器 id uTextureUnitLocation
        uTextureUnitLocation =  glGetUniformLocation(mRendeId, U_TEXTURE_UNIT);

//        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
        //纹理坐标 在cube 数组中声明的
        aTextureCoordinatesLocation = glGetAttribLocation(mRendeId, A_TEXTURE_COORDINATES);
        Log.i(TAG,"updateTextureShader mRendeId :"  +mRendeId);
        Log.i(TAG,"updateTextureShader aPositionLocation :"  +aPositionLocation);
        Log.i(TAG,"updateTextureShader aColorLocation :"  +aColorLocation);
        Log.i(TAG,"updateTextureShader uTextureUnitLocation :"  +uTextureUnitLocation);
        Log.i(TAG,"updateTextureShader aTextureCoordinatesLocation :"  +aTextureCoordinatesLocation);

    }

    private void createTexture(){
         textureId = TextureUtils.createTexture("open gl waterwark ", 800, 200, 100);
        if(textureId == -1){
            Log.i(TAG,"create watermark texture failed");
            return;
        }
        Log.i(TAG,"createTexture , textureId"  +textureId);
    }

    private void updateTextureId(){
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        mWaterMarkInfo = sdf.format(new Date(System.currentTimeMillis()));

        textureId = TextureUtils.createTexture(mWaterMarkInfo, 800, 200, 50);
        if(textureId == -1){
            Log.i(TAG,"updateTextureId create watermark texture failed");
            return;
        }
        Log.i(TAG,"updateTextureId createTexture , textureId"  +textureId);
    }

    private volatile boolean  isNeedUpdate = false;

    final long timeInterval = 1000;
    private Runnable mRunable = new Runnable() {
        @Override
        public void run() {
            while (true){
                isNeedUpdate = true;
                try {
                    Thread.sleep(timeInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
