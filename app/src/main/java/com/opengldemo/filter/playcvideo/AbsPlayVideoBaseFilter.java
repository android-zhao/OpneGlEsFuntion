package com.opengldemo.filter.playcvideo;

import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.util.Log;

import com.opengldemo.GLesUtils;
import com.opengldemo.filter.BaseFilter;

import java.nio.FloatBuffer;

public abstract class AbsPlayVideoBaseFilter extends BaseFilter {

    private String TAG = "AbsPlayVideoBaseFilter";
    private int textureTransformLocation;//mvp矩阵在glsl中的 Uniform 句柄值

    public AbsPlayVideoBaseFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    protected void onInit() {
        super.onInit();
        textureTransformLocation = GLES30.glGetUniformLocation(getProgramId(), "textureTransform");
        Log.i(TAG,"onInit end");
    }

    private float[] textureTransformMatrix = new float[16];
    public void setTextureTransformMatrix(float[] matrix) {
        textureTransformMatrix = matrix;
    }

    public int onDrawFrame(final int textureId,
                           FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        Log.d(TAG,"onDrawFrame");

        if (!hasInitialized()) {
            Log.d(TAG,"onDrawFrame,not mHasInitialized ");
            return GLesUtils.NOT_INIT;
        }
        GLES30.glUseProgram(getProgramId());
        runPendingOnDrawTask();

        //处理传入顶点坐标
        vertexBuffer.position(0);
        GLES30.glVertexAttribPointer(mAttributePosition, 2, GLES30.GL_FLOAT,
                false, 0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(mAttributePosition);

        textureBuffer.position(0);
        GLES30.glVertexAttribPointer(mAttributeTextureCoordinate, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        GLES30.glEnableVertexAttribArray(mAttributeTextureCoordinate);

        //设置mvp矩阵
        GLES30.glUniformMatrix4fv(textureTransformLocation,
                1, false, textureTransformMatrix, 0);
        if (textureId != GLesUtils.NO_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES30.glUniform1i(mUniformTexture, 0);
        }


        onDrawArrayBefore();

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glDisableVertexAttribArray(mAttributePosition);
        GLES30.glDisableVertexAttribArray(mAttributeTextureCoordinate);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        onDrawArrayAfter();

        return GLesUtils.ON_DRAWN;
    }


}
