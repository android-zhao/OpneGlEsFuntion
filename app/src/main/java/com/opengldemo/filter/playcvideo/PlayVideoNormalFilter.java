package com.opengldemo.filter.playcvideo;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.util.Log;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;
import com.opengldemo.TextureRotateUtil;
import com.opengldemo.filter.BaseFilter;

import java.nio.FloatBuffer;

public class PlayVideoNormalFilter extends BaseFilter {

    private static String TAG = "PlayVideoNormalFilter";

    public PlayVideoNormalFilter(Context context){
        super(GLesUtils.readTextFileFromResource(context, R.raw.base_fliter_normal_vertex),
                GLesUtils.readTextFileFromResource(context, R.raw.base_filter_play_video_fragment));

    }

    public PlayVideoNormalFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    public void init() {
        Log.i(TAG,"init");
        super.init();
    }
    private int textureTransformLocation;//mvp矩阵在glsl中的 Uniform 句柄值
    private int uMatrixLocation = -1;//mvp矩阵
    private int textureScaleLocation = -1;//纹理的缩放矩阵
    @Override
    protected void onInit() {
        Log.i(TAG,"onInit");
        super.onInit();
//        mVertexBuffer.clear();
//        mVertexBuffer.put(TextureRotateUtil.VERTEX).position(0);
//        mTextureBuffer.clear();
//        mTextureBuffer.put(TextureRotateUtil.TEXTURE_ROTATE_0).position(0);
        textureTransformLocation = GLES30.glGetUniformLocation(getProgramId(), "textureTransform");
        uMatrixLocation = GLES30.glGetUniformLocation(getProgramId(), "uMatrix");
        textureScaleLocation = GLES30.glGetUniformLocation(getProgramId(), "textureScale");
    }

    private float[] textureTransformMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] textureScaleMatrix = new float[16];

    public void setTextureTransformMatrix(float[] matrix) {
        textureTransformMatrix = matrix;
    }

    public void setProjectionMatrix(float[] projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public void setTextureScaleMatrix(float[] textureScaleMatrix) {
        this.textureScaleMatrix = textureScaleMatrix;
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
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

        //设置纹理旋转矩阵
        GLES30.glUniformMatrix4fv(textureTransformLocation,
                1, false, textureTransformMatrix, 0);
        //设置MVP矩阵
        GLES30.glUniformMatrix4fv(uMatrixLocation,
                1, false, projectionMatrix, 0);

        //设置纹理缩放矩阵
        GLES30.glUniformMatrix4fv(textureScaleLocation,
                1, false, textureScaleMatrix, 0);

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
