package com.opengldemo.render;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glViewport;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;
import com.opengldemo.filter.FboOnePicFilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FboRender implements GLSurfaceView.Renderer {

    private static final  String TAG = "FboRender";
    private Context mContext;
    private FboOnePicFilter mOnePicFilter ;
    FboOnePicFilter.OnDataDrawWithFilter listener;
    OnDataDrawByBuffer lis;

    /*****************************************render中实现****************************************/
//    private float mVertex[] = {
//            -1.0f, 1.0f, 0.0f,
//            -1.0f, -1.0f, 0.0f,
//            1.0f, 1.0f, 0.0f,
//            1.0f, -1.0f, 0.0f
//    };
//    private float[] mFboTexture = {
//            0.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f
//    };
//
//    protected FloatBuffer mVertexBuffer = null;
//    protected FloatBuffer mFboTextureBuffer = null;
//    // 帧缓冲对象 - 颜色、深度、模板附着点，纹理对象可以连接到帧缓冲区对象的颜色附着点
//    private int[] mFrameBufferId = new int[1];
//    private int[] mTextureId = new int[2];
//    private int mRendeId;

    /*****************************************render中实现****************************************/
    public FboRender(Context mContext, FboOnePicFilter.OnDataDrawWithFilter listener) {
        this.mContext = mContext;
        this.listener = listener;
    }

    public FboRender(Context mContext,OnDataDrawByBuffer listener) {
        this.mContext = mContext;
        lis = listener;
//        createArrays();
    }

    /*****************************************render中实现****************************************/
//    void createArrays(){
//        mVertexBuffer = ByteBuffer.allocateDirect(mVertex.length * Float.BYTES)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        mVertexBuffer.put(mVertex);
//        mVertexBuffer.position(0);
//
//        mFboTextureBuffer = ByteBuffer.allocateDirect(mFboTexture.length * Float.BYTES)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        mFboTextureBuffer.put(mFboTexture);
//        mFboTextureBuffer.position(0);
//
//    }

    /*****************************************render中实现****************************************/

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i("zhf_opengl","onSurfaceCreated");
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        mOnePicFilter = new FboOnePicFilter(mContext);


        /*****************************************render中实现****************************************/
        //设置背景颜色
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        //启动深度测试
//        gl.glEnable(GLES20.GL_DEPTH_TEST);
//
//        createProgram();
//        createFboEnv();
        /*****************************************render中实现****************************************/
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i("zhf_opengl","onSurfaceChanged");
//        glViewport(0, 0, mBitmapWidth, mBitmapHeight);

        glViewport(0, 0, width, height);
        mOnePicFilter.setFilterParms(width,height);
        mOnePicFilter.setListener(listener);

    }
    int count = 0;
    @Override
    public void onDrawFrame(GL10 gl) {
        Log.i("zhf_opengl","onDrawFrame begin");
        //使用FBO绘制
        mOnePicFilter.onDrawFrame();

        /*****************************************render中实现****************************************/
//        if(count<1){
//            count++;
//            Log.i("zhf_opengl","onDrawFrame begin count" +count);
//            return;
//        }
//        createFboEnv();
//// 将颜色缓存区设置为预设的颜色
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//        // 启用顶点的数组句柄
//        GLES20.glEnableVertexAttribArray(aPositionLocation);
//        GLES20.glEnableVertexAttribArray(aTextureCoordinatesLocation);
//        // 绘制模型
//        GLES20.glUseProgram(mRendeId);
//        //准备顶点坐标和纹理坐标
//        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
//        GLES20.glVertexAttribPointer(aTextureCoordinatesLocation, 2, GLES20.GL_FLOAT, false, 0, mFboTextureBuffer);
//        //激活纹理
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE);
//        //绑定纹理
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
//        // 绑定缓存
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId[0]);
//        // 绘制贴图
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//
//
//
//
//        // 分配字节缓区大小， 一个像素4个字节
//        ByteBuffer byteBuffer = ByteBuffer.allocate(mBitmapWidth * mBitmapHeight * 4);
//        GLES20.glReadPixels(0, 0, mBitmapWidth, mBitmapHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
//
//        Log.i("zhf_opengl","copyPixelsFromBuffer end");
//        // 回调
//        lis.onDataFromRender(byteBuffer,mBitmapWidth,mBitmapHeight);
//
//        // 禁止顶点数组句柄
//        GLES20.glDisableVertexAttribArray(0);
//        GLES20.glDisableVertexAttribArray(1);
        /*****************************************render中实现****************************************/


    }
    protected static final String U_TEXTURE_OES_UNIT = "uTextureUnit";
    private int uTextureUnitLocation = -1;

    protected static final String A_POSITION = "vPosition";
    private int aPositionLocation = -1;

    protected static final String A_TEXTURE_COORDINATES = "aTextureCoord";
    private int mBitmapWidth = -1,mBitmapHeight = -1;
    private int aTextureCoordinatesLocation = -1;

    /*****************************************render中实现****************************************/

//    void createProgram(){
//
//        String vertxtStr  = null;
//        String framentStr = null;
//        vertxtStr = GLesUtils.readTextFileFromResource(mContext, R.raw.test_fbo_render_vertex);
//        framentStr = GLesUtils.readTextFileFromResource(mContext,R.raw.test_fbo_render_fragement);
//        mRendeId =GLesUtils.buildProgram(vertxtStr, framentStr);
//
//        // 纹理采样器 id uTextureUnitLocation
//        uTextureUnitLocation =  glGetUniformLocation(mRendeId, U_TEXTURE_OES_UNIT);
//        //视图的变化矩阵
////        uTextureTransformLocation = glGetUniformLocation(mRendeId, U_TEXTURE_TRANSFORM);
//        //顶点坐标 顶点0 1 2 3
//        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
//        //纹理坐标 在cube 数组中声明的
//        aTextureCoordinatesLocation = glGetAttribLocation(mRendeId, A_TEXTURE_COORDINATES);
//
//        Log.i(TAG,"updateTextureAndVertxy floatBuffer size :" + mVertexBuffer.capacity());
//        Log.i(TAG,"updateTextureAndVertxy texFloatBuffer size :" + mFboTextureBuffer.capacity());
//
//        Log.i(TAG,"updateTextureAndVertxy mRendeId  :" + mRendeId);
//        Log.i(TAG,"updateTextureAndVertxy frament 采样器 句柄  :" + uTextureUnitLocation);
//        Log.i(TAG,"updateTextureAndVertxy vertex 顶点句柄  :" + aPositionLocation);
//        Log.i(TAG,"updateTextureAndVertxy vertex 纹理坐标 句柄  :" + aTextureCoordinatesLocation);
//
//        Log.i(TAG,"updateTextureAndVertxy end");
//    }

//    void createFboEnv(){
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inScaled = false;
//
//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.raw.cat_test1, options);
//        mBitmapWidth = bitmap.getWidth();
//        mBitmapHeight = bitmap.getHeight();
//
//        // 生成纹理id
//        GLES20.glGenTextures(2, mTextureId, 0);
//        for (int i = 0; i < 2; i++) {
//            // 绑定纹理到OpenGL
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[i]);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//            if (i == 0) {
//                // 第一个纹理对象给渲染管线(加载bitmap到纹理中)
//                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
//            } else {
//                // 第二个纹理对象给帧缓冲区
//                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(),
//                        0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
//            }
//            // 取消绑定纹理
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE);
//        }
//        // 创建帧缓存id
//        GLES20.glGenFramebuffers(1, mFrameBufferId, 0);
//        // 绑定帧缓存
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId[0]);
//        // 将第二个纹理附着在帧缓存的颜色附着点上
//        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureId[1], 0);
//        // 取消绑定帧缓存
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);
//    }
    /*****************************************render中实现****************************************/

    public interface OnDataDraw{
        void onDataFromRender(final Bitmap bitmap);
    }

    public interface OnDataDrawByBuffer{
        void onDataFromRender(final ByteBuffer buffer,int width,int height);
    }

}
