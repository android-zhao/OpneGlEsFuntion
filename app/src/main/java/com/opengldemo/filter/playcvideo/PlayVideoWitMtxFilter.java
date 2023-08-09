package com.opengldemo.filter.playcvideo;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;
import com.opengldemo.filter.BaseFilter;
import com.opengldemo.view.MediaUtils;

import java.nio.FloatBuffer;
import java.util.Arrays;

public class PlayVideoWitMtxFilter extends BaseFilter {
    //采取2个texture 实现一个渲染和移动
    private static String TAG = "PlayVideoWitMtxFilter";

    public PlayVideoWitMtxFilter(Context context){
        super(GLesUtils.readTextFileFromResource(context, R.raw.base_filter_normal_mtx_vertex),
                GLesUtils.readTextFileFromResource(context, R.raw.base_filter_play_video_fragment));

    }

    public PlayVideoWitMtxFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    public void init() {
        Log.i(TAG,"init");
        super.init();
    }
    private int textureTransformLocation;//mvp矩阵在glsl中的 Uniform 句柄值
    private int uMatrixLocation = -1;//p矩阵 投影矩阵 正交投影矩阵
    private int moduleLocation = -1;//m矩阵 平移矩阵
    private int viewMatrixLocation = -1;//view 矩阵

    @Override
    protected void onInit() {
        Log.i(TAG,"onInit");
        super.onInit();
//        mVertexBuffer.clear();
//        mVertexBuffer.put(TextureRotateUtil.VERTEX).position(0);
//        mTextureBuffer.clear();
//        mTextureBuffer.put(TextureRotateUtil.TEXTURE_ROTATE_0).position(0);
        textureTransformLocation = GLES30.glGetUniformLocation(getProgramId(), "textureTransform");
        uMatrixLocation = GLES30.glGetUniformLocation(getProgramId(), "uMatrix");//p矩阵
        moduleLocation = GLES30.glGetUniformLocation(getProgramId(), "mMatrix");//m矩阵
        viewMatrixLocation = GLES30.glGetUniformLocation(getProgramId(), "vMatrix");//view矩阵

    }

    int windowWidth = -1;
    int windowHeight = -1;

    public void onSurfaceChanged(int width,int height) {
        windowWidth = width;
        windowHeight = height;
        initFilterMtx();
    }
    private void  initFilterMtx(){
        updateModuleMatrix();
        updateProjection();
        updateCameraMtx();
    }
    private void updateModuleMatrix(){
        updateTranslateMtx();
        updateScaleMtx();
        Matrix.multiplyMM(moduleMatrix,0,translateMatrix,0,scaleMatrix,0);

//        playVideoMtxFilter.setModuleMatrix(moduleMtx);

        Log.i(TAG,"updateModuleMatrix :" +Arrays.toString(moduleMatrix));
    }

    private void updateTranslateMtx(){
        float viewRatio = (float) windowWidth / windowHeight;
        Matrix.setIdentityM(translateMatrix,0);
        Matrix.translateM(translateMatrix,0,0.3f,0.5f,0.0f);
        Log.i(TAG,"updateTranslateMtx :" + Arrays.toString(translateMatrix));
    }

    private void updateScaleMtx(){
        float viewRatio = (float) windowWidth / windowHeight;
        Matrix.setIdentityM(scaleMatrix,0);
        Matrix.scaleM(scaleMatrix,0,0.5f,0.5f,1.0f);
        Log.i(TAG,"updateScaleMtx:" +Arrays.toString(scaleMatrix));
    }

    private void updateProjection() {
        float viewRatio = (float) windowWidth / windowHeight;

//        float videoRatio = (float) MediaUtils.getVideoWidth(mVideoPath) / MediaUtils.getVideoHeight(mVideoPath);


        //正交投影矩阵
        Matrix.orthoM(projectionMatrix, 0,
                - 1, 1, -1, 1,
                -1f, 1f);
        Log.i(TAG,"updateProjection :" +Arrays.toString(projectionMatrix));

    }

    private void updateCameraMtx(){
        Log.i(TAG,"updateCameraMtx beging:" +Arrays.toString(viewMatrix));
        Matrix.setLookAtM(
                viewMatrix, 0,
                0f, 0f, 5.0f,
                0f, 0f, 0f,
                0f, 1.0f, 0f
        );

        Log.i(TAG,"updateCameraMtx end:" +Arrays.toString(viewMatrix));
    }


    //纹理的矩阵，由相机写了图片到纹理上，由纹理api获取
    //此矩阵需要每次从纹理上更新，直接决定纹理显示的方向角度等
    protected float[] textureTransformMatrix = new float[16];
    protected float[] projectionMatrix = new float[16];//p矩阵 投影矩阵
    protected float[] viewMatrix = new float[16];//v矩阵 camera矩阵


    protected float[] moduleMatrix = new float[16];//m矩阵 module矩阵
    protected float[] translateMatrix = new float[16];//m矩阵 平移矩阵
    protected float[] scaleMatrix = new float[16];//m矩阵 缩放矩阵
    protected float[] rotaMatrix = new float[16];//m矩阵 旋转矩阵


    public void setTextureTransformMatrix(float[] matrix) {
        textureTransformMatrix = matrix;
    }

    public void setProjectionMatrix(float[] projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }
    public void setCameraMtx(float[] viewMtx) {
        this.viewMatrix = viewMtx;
    }

    public void setTranslateMatrix(float[] translateMatrix) {
        this.translateMatrix = translateMatrix;
    }


    public void setScaleMatrix(float[] scaleMatrix){
        this.scaleMatrix = scaleMatrix;
    }

    boolean isPrintfModuleMtx = false;
    public void setModuleMatrix(float[] moduleMatrix){
        this.moduleMatrix =moduleMatrix;
        isPrintfModuleMtx = true;
        Log.i(TAG,"setModuleMatrix params:-->" + Arrays.toString(moduleMatrix));
        Log.i(TAG,"setModuleMatrix moduleMatrix :-->" + Arrays.toString(moduleMatrix));
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
    }
    private int openglFrameCount = 0;

    public int onDrawFrame(final int textureId,
                           FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        openglFrameCount++;
        if(openglFrameCount % 60 == 0){
            Log.d(TAG,"onDrawFrame");
        }
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

        //设置MVP矩阵 p矩阵
        GLES30.glUniformMatrix4fv(uMatrixLocation,
                1, false, projectionMatrix, 0);
        if(isPrintfModuleMtx){
            Log.i(TAG,"onDraw moduleMatrix  >" + Arrays.toString(moduleMatrix));
            isPrintfModuleMtx = false;
        }

        //设置MVP矩阵 m矩阵
        GLES30.glUniformMatrix4fv(moduleLocation,
                1, false, moduleMatrix, 0);
        //设置MVP矩阵 v矩阵
        GLES30.glUniformMatrix4fv(viewMatrixLocation,
                1, false, viewMatrix, 0);

        if(openglFrameCount % 60 == 0){
            Log.i(TAG,"projectionMatrix:" + Arrays.toString(projectionMatrix));
            Log.i(TAG,"moduleMatrix:" + Arrays.toString(moduleMatrix));
            Log.i(TAG,"viewMatrix:" + Arrays.toString(viewMatrix));
            Log.i(TAG,"textureTransformMatrix:" + Arrays.toString(textureTransformMatrix));
        }
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
