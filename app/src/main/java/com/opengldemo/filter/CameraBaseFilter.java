package com.opengldemo.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.util.Log;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;

import java.nio.FloatBuffer;

//相机基本滤镜
public class CameraBaseFilter extends BaseFilter {
    private final static  String TAG = "CameraBaseFilter";
    public CameraBaseFilter(Context context) {
        super(GLesUtils.readTextFileFromResource(context, R.raw.base_fliter_normal_vertex),
                GLesUtils.readTextFileFromResource(context, R.raw.base_filter_nomal_fragement));
    }

    private int textureTransformLocation;//mvp矩阵在glsl中的 Uniform 句柄值
    protected void onInit() {
        super.onInit();
        textureTransformLocation = GLES30.glGetUniformLocation(getProgramId(), "textureTransform");
    }

    private float[] textureTransformMatrix;
    public void setTextureTransformMatrix(float[] matrix) {
        textureTransformMatrix = matrix;
    }
//    private FloatBuffer mVertexBuffer;
//    private FloatBuffer mTextureBuffer;

    @Override
    public int onDrawFrame(int textureId) {
        return onDrawFrame(textureId, mVertexBuffer, mTextureBuffer);
    }

    @Override
    public int onDrawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (!hasInitialized()) {
            return GLesUtils.NOT_INIT;
        }
        Log.d(TAG,"getProgramId() :" +getProgramId());
        GLES30.glUseProgram(getProgramId());
        runPendingOnDrawTask();

        //启用顶点坐标
        vertexBuffer.position(0);
        GLES30.glVertexAttribPointer(mAttributePosition,
                2, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(mAttributePosition);

        //启用纹理坐标
        textureBuffer.position(0);
        GLES30.glVertexAttribPointer(mAttributeTextureCoordinate,
                2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        GLES30.glEnableVertexAttribArray(mAttributeTextureCoordinate);

        //设置mvp矩阵
        GLES30.glUniformMatrix4fv(textureTransformLocation,
                1, false, textureTransformMatrix, 0);


        //启用纹理，此处纹理即为相机启动之后设置给相机预览创建的texture
        if (textureId != GLesUtils.NO_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES30.glUniform1i(mUniformTexture, 0);
        }

        //启动绘制，请绘制完成之后清除绘制参数，顶点着色器，片元着色器 和纹理
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glDisableVertexAttribArray(mAttributePosition);
        GLES30.glDisableVertexAttribArray(mAttributeTextureCoordinate);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        return GLesUtils.ON_DRAWN;
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyFrameBuffer();
    }


    public int onDrawToTexture(int textureId) {
        if (!hasInitialized()) {
            return GLesUtils.NOT_INIT;
        }

        if (frameBuffer == null) {
            return GLesUtils.NO_TEXTURE;
        }

        GLES30.glUseProgram(getProgramId());
        runPendingOnDrawTask();
        GLES30.glViewport(0, 0, frameWidth, frameHeight);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);

        mVertexBuffer.position(0);
        GLES30.glVertexAttribPointer(mAttributePosition, 2, GLES30.GL_FLOAT, false, 0, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(mAttributePosition);
        mTextureBuffer.position(0);
        GLES30.glVertexAttribPointer(mAttributeTextureCoordinate, 2, GLES30.GL_FLOAT, false, 0, mTextureBuffer);
        GLES30.glEnableVertexAttribArray(mAttributeTextureCoordinate);
        GLES30.glUniformMatrix4fv(textureTransformLocation, 1, false, textureTransformMatrix, 0);

        if (textureId != GLesUtils.NO_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES30.glUniform1i(mUniformTexture, 0);
        }

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        GLES30.glDisableVertexAttribArray(mAttributePosition);
        GLES30.glDisableVertexAttribArray(mAttributeTextureCoordinate);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        GLES30.glViewport(0, 0, mOutputWidth, mOutputHeight);
        return frameBufferTexture[0];
    }


    public void initFrameBuffer(int width, int height){
        //初始化的参数，先根据默认参数进行清除数据
        if (frameBuffer != null && (frameWidth != width || frameHeight != height))
            destroyFrameBuffer();

        //初始化FBO
        if (frameBuffer == null) {
            //传入参数是预览的宽和高
            frameWidth = width;
            frameHeight = height;

            frameBuffer = new int[1];
            frameBufferTexture = new int[1];
            GLES30.glGenFramebuffers(1, frameBuffer, 0);
            GLES30.glGenTextures(1, frameBufferTexture, 0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, frameBufferTexture[0]);

            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height,
                    0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                    GLES30.GL_TEXTURE_2D, frameBufferTexture[0], 0);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        }

    }

    private int[] frameBuffer = null;
    private int[] frameBufferTexture = null;
    private int frameWidth = -1;
    private int frameHeight = -1;
    public void destroyFrameBuffer() {
        if (frameBufferTexture != null) {
            GLES30.glDeleteTextures(1, frameBufferTexture, 0);
            frameBufferTexture = null;
        }
        if (frameBuffer != null) {
            GLES30.glDeleteFramebuffers(1, frameBuffer, 0);
            frameBuffer = null;
        }
        frameWidth = -1;
        frameHeight = -1;
    }
}
