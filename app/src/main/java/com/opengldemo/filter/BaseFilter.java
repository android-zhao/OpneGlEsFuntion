package com.opengldemo.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.util.Log;

import com.opengldemo.GLesUtils;
import com.opengldemo.TextureRotateUtil;
import com.opengldemo.bean.Rotation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

public class    BaseFilter {

    private final static  String TAG = "BaseFilter";
    private Context mContext ;
    public final static String NORMAL_VERTEX_SHADER =
            "attribute vec4 position;\n" +
                    "attribute vec4 inputTextureCoordinate;\n" +
                    "varying vec2 textureCoordinate;\n" +
                    "void main() {\n" +
                    "   gl_Position = position;\n" +
                    "   textureCoordinate = inputTextureCoordinate.xy;\n" +
                    "}";

    public BaseFilter(String vertexShader, String fragmentShader) {
        initVertexAndTexture(vertexShader,fragmentShader);
    }

    private LinkedList<Runnable> mRunnableDraw = null;
    private String mVertexShader; //顶点着色器
    private String mFragmentShader; //片元着色器
    protected FloatBuffer mVertexBuffer; //顶点着色器的buffer
    protected FloatBuffer mTextureBuffer;//纹理的buffer

    private void initVertexAndTexture(String vertexShader, String fragmentShader) {
        mRunnableDraw = new LinkedList<>();
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;

        mVertexBuffer = ByteBuffer.allocateDirect(TextureRotateUtil.VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.put(TextureRotateUtil.VERTEX).position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(TextureRotateUtil.TEXTURE_ROTATE_0.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureBuffer.put(TextureRotateUtil.getRotateTexture(Rotation.NORMAL, false, true))
                .position(0);
        Log.i(TAG,"initVertexAndTexture end");
    }

    private boolean mHasInitialized = false;
    public void init() {
        onInit();
        mHasInitialized = true;
        onInitialized();
    }

    private int mProgramId = -1; //gpu 使用opengl 运行返回的句柄值
    protected int mAttributePosition = -1; // glsl 中顶点坐标内置函数
    protected int mUniformTexture = -1; //glsl 中texture 的oes纹理采样器
    protected int mAttributeTextureCoordinate = -1;//glsl中纹理坐标
    protected void onInit() {
        Log.i(TAG,"onInit begin");
        mProgramId = GLesUtils.buildProgram(mVertexShader, mFragmentShader);
        mAttributePosition = GLES30.glGetAttribLocation(mProgramId, "position");
        mUniformTexture = GLES30.glGetUniformLocation(mProgramId, "inputImageTexture");
        mAttributeTextureCoordinate = GLES30.glGetAttribLocation(mProgramId, "inputTextureCoordinate");
        Log.i(TAG,"onInit end");
    }

    protected void onInitialized() {

    }
    protected void onDestroy() {

    }

    public void destroy() {
        mHasInitialized = false;
        GLES30.glDeleteProgram(mProgramId);
        onDestroy();
    }

    //相机预览的宽和高
    protected int mInputWidth;
    protected int mInputHeight;

    public void onInputSizeChanged(final int width, final int height) {
        mInputWidth = width;
        mInputHeight = height;
    }

    protected void runPendingOnDrawTask() {
        while (!mRunnableDraw.isEmpty()) {
            mRunnableDraw.removeFirst().run();
        }
    }
    protected void onDrawArrayBefore() {

    }

    protected void onDrawArrayAfter() {

    }


    public int onDrawFrame(final int textureId) {
        return onDrawFrame(textureId, mVertexBuffer, mTextureBuffer);
    }

    public int onDrawFrame(final int textureId,
                           FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        Log.d(TAG,"onDrawFrame");
        if (!mHasInitialized){
            Log.d(TAG,"onDrawFrame,not mHasInitialized ");
            return GLesUtils.NOT_INIT;
        }
        GLES30.glUseProgram(mProgramId);
        runPendingOnDrawTask();
        //处理传入顶点坐标
        vertexBuffer.position(0);
        GLES30.glVertexAttribPointer(mAttributePosition, 2, GLES30.GL_FLOAT,
                false, 0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(mAttributePosition);

        textureBuffer.position(0);
        GLES30.glVertexAttribPointer(mAttributeTextureCoordinate, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        GLES30.glEnableVertexAttribArray(mAttributeTextureCoordinate);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

        if (textureId != GLesUtils.NO_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
//            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES30.glUniform1i(mUniformTexture, 0);
        }

        onDrawArrayBefore();

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glDisableVertexAttribArray(mAttributePosition);
        GLES30.glDisableVertexAttribArray(mAttributeTextureCoordinate);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        onDrawArrayAfter();

        return GLesUtils.ON_DRAWN;

    }

        public boolean hasInitialized() {
        return mHasInitialized;
    }

    public int getProgramId() {
        return mProgramId;
    }


    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunnableDraw) {
            mRunnableDraw.addLast(runnable);
        }
    }
    public void setFloat(final int location, final float floatVal) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES30.glUniform1f(location, floatVal);
            }
        });
    }

    public void setFloatVec2(final int location, final float[] floatArray) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES30.glUniform2fv(location, 1, FloatBuffer.wrap(floatArray));
            }
        });
    }

    //在界面上预览显示出来需要的宽和高
    protected int mOutputWidth;
    protected int mOutputHeight;

    public void onOutputSizeChanged(final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
    }

    public int getOutputWidth() {
        return mOutputWidth;
    }

    public int getOutputHeight() {
        return mOutputHeight;
    }


}

