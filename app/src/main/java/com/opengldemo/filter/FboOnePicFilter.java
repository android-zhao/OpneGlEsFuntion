package com.opengldemo.filter;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_FRAMEBUFFER_COMPLETE;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindRenderbuffer;
import static android.opengl.GLES20.glCheckFramebufferStatus;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glFramebufferRenderbuffer;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glValidateProgram;
import static android.opengl.GLES20.glViewport;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;
import com.opengldemo.TextureUtils;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class FboOnePicFilter {
    private final static String TAG = "FboOnePicFilter";
    private Context mContext;

    private FloatBuffer verFloatBuffer = null;
    private FloatBuffer texFloatBuffer = null;
    //顶点坐标
    private float vertex[] = {
            -1.0f,  1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f,  -1.0f,
    };

    //纹理坐标
    private float texture[] = {
            0.0f, 0.0f,
            0.0f,  1.0f,
            1.0f,  0.0f,
            1.0f, 1.0f,
    };

    OnDataDrawWithFilter onDataDrawInterface;
    public FboOnePicFilter(Context mContext) {
        this.mContext = mContext;
        onFilterCreate();
    }

    private void onFilterCreate(){
        mBitmap = TextureUtils.readBitmapFromRes(mContext);
        createVertexArray();
        createShader();
        createEnv();
    }
    public void setViewParms(int width, int height){
        mWidth = width;
        mHeight = height;
    }
    public void setPreviewSize(int previewWidth,int previewHeight){
        mCameraPreviewWidth = previewWidth;
        mCameraPreviewHeight = previewHeight;
    }
    private int mRendeId = -1;

    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private  int uTextureUnitLocation  = -1;
    private  int uTextureTransformLocation = -1;
    private  int aTextureCoordinatesLocation = -1;
    protected static final String A_TEXTURE_COORDINATES = "vCoord";
    private  int aPositionLocation = -1;
    private  int aColorLocation = -1;
    protected static final String U_TEXTURE_OES_UNIT = "vTexture";
    protected static final String U_TEXTURE_TRANSFORM = "vMatrix";
    private int mWidth,mHeight;
    private int mCameraPreviewWidth,mCameraPreviewHeight;
    private void createShader() {

            String vertxtStr  = null;
            String framentStr = null;
            vertxtStr = GLesUtils.readTextFileFromResource(mContext, R.raw.fbo_onepic_vertext);
            framentStr = GLesUtils.readTextFileFromResource(mContext,R.raw.fbo_camera_fragement);
            mRendeId = buildProgram(vertxtStr, framentStr);

//            Log.i(TAG,"updateTextureShader end");

        // 纹理采样器 id uTextureUnitLocation
        uTextureUnitLocation =  glGetUniformLocation(mRendeId, U_TEXTURE_OES_UNIT);
        //视图的变化矩阵
        uTextureTransformLocation = glGetUniformLocation(mRendeId, U_TEXTURE_TRANSFORM);
        //顶点坐标 顶点0 1 2 3
        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
        //纹理坐标 在cube 数组中声明的
        aTextureCoordinatesLocation = glGetAttribLocation(mRendeId, A_TEXTURE_COORDINATES);

        Log.i(TAG,"updateTextureAndVertxy floatBuffer size :" + verFloatBuffer.capacity());
        Log.i(TAG,"updateTextureAndVertxy texFloatBuffer size :" + texFloatBuffer.capacity());

        Log.i(TAG,"updateTextureAndVertxy mRendeId  :" + mRendeId);
        Log.i(TAG,"updateTextureAndVertxy frament 采样器 句柄  :" + uTextureUnitLocation);
        Log.i(TAG,"updateTextureAndVertxy vertex 顶点句柄  :" + aPositionLocation);

        Log.i(TAG,"updateTextureAndVertxy vertex 变化矩阵  :" + uTextureTransformLocation);
        Log.i(TAG,"updateTextureAndVertxy vertex 纹理坐标 句柄  :" + aTextureCoordinatesLocation);


        Log.i(TAG,"updateTextureAndVertxy end");

    }
    public void setListener(OnDataDrawWithFilter listener){
        onDataDrawInterface = listener;
    }
    public void onDrawFrame(){

        glViewport(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        GLES20.glEnable(GL_DEPTH_TEST);
        if(mBitmap != null && !mBitmap.isRecycled()){
            Log.i(TAG,"fbo filter onDrawFrame ");
            glBindFramebuffer(GL_FRAMEBUFFER,fboBuffer[0]);

            //将纹理缓冲附加到帧缓冲上 但是帧纹理缓冲有2个 确认下分别哪一个附加那哪一个上
            glFramebufferTexture2D(GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D,fboTextureBuffer[1],0);

            glFramebufferRenderbuffer(GL_FRAMEBUFFER,GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER, fboRenderBuffer[0]);

            // 检查 FBO 的完整性状态
            if (glCheckFramebufferStatus(GL_FRAMEBUFFER)!= GL_FRAMEBUFFER_COMPLETE) {
                Log.i(TAG,"FBOSample::CreateFrameBufferObj glCheckFramebufferStatus status != GL_FRAMEBUFFER_COMPLETE");
            }
            onClear();
            onUseProgram();
            onSetExpandData();
            onBindTexture(fboTextureBuffer[0]);
            onDraw();
            long begin = System.currentTimeMillis();

            GLES20.glReadPixels(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, mBuffer);
            long end = System.currentTimeMillis();

            Log.i(TAG,"fbo glReadPixels cost -->" +(end -begin));
            if(onDataDrawInterface != null){
                onDataDrawInterface.onData(mBuffer);
            }


            /***********************************************************/
            deleteEnvi();
            mBitmap.recycle();
        }
    }


    public void onDrawFrameForCamera(int cameraTextureId){

        glViewport(0, 0, mWidth, mHeight);
        GLES20.glEnable(GL_DEPTH_TEST);
        Log.i(TAG,"fbo filter onDrawFrameForCamera ");
            glBindFramebuffer(GL_FRAMEBUFFER,fboBuffer[0]);

            //将纹理缓冲附加到帧缓冲上 但是帧纹理缓冲有2个 确认下分别哪一个附加那哪一个上
            glFramebufferTexture2D(GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D,fboTextureBuffer[1],0);

            glFramebufferRenderbuffer(GL_FRAMEBUFFER,GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER, fboRenderBuffer[0]);

            // 检查 FBO 的完整性状态
            if (glCheckFramebufferStatus(GL_FRAMEBUFFER)!= GL_FRAMEBUFFER_COMPLETE) {
                Log.i(TAG,"FBOSample::CreateFrameBufferObj glCheckFramebufferStatus status != GL_FRAMEBUFFER_COMPLETE");
            }
            onClear();
            onUseProgram();
            onSetExpandData();
            onBindTexture(cameraTextureId);
            onDraw();

//            long begin = System.currentTimeMillis();
//
//            GLES20.glReadPixels(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), GLES20.GL_RGBA,
//                    GLES20.GL_UNSIGNED_BYTE, mBuffer);
//            long end = System.currentTimeMillis();
//            Log.i(TAG,"fbo glReadPixels cost -->" +(end -begin));
//            if(onDataDrawInterface != null){
//                onDataDrawInterface.onData(mBuffer);
//            }

            /***********************************************************/
            deleteEnvi();
//            mBitmap.recycle();

    }

    private void onClear(){
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }
    protected void onUseProgram(){
        GLES20.glUseProgram(mRendeId);
    }

    /**
     * 设置MVP矩阵
     */
    protected void onSetExpandData(){
        Log.i(TAG,"onSetExpandData  变化矩阵值：" + Arrays.toString(getMatrix()));
        GLES20.glUniformMatrix4fv(uTextureTransformLocation,1,false,getMatrix(),0);
    }
    private int textureType=0;      //默认使用Texture2D0
    /**
     * 绑定默认纹理
     */
    protected void onBindTexture(int textureId){
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+textureType);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
        GLES20.glUniform1i(uTextureUnitLocation,textureType);
    }
    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected void onDraw(){
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation,2, GLES20.GL_FLOAT, false,
                0,verFloatBuffer);
        GLES20.glEnableVertexAttribArray(aTextureCoordinatesLocation);
        GLES20.glVertexAttribPointer(aTextureCoordinatesLocation, 2,
                GLES20.GL_FLOAT, false, 0, texFloatBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aTextureCoordinatesLocation);
    }

    private float[] getMatrix(){
        float origin[] = new float[]{
                1,0,0,0,
                0,1,0,0,
                0,0,1,0,
                0,0,0,1
        };
        return flip(origin,false,true);
    }

    private static float[] flip(float[] m,boolean x,boolean y){
        if(x||y){
            Matrix.scaleM(m,0,x?-1:1,y?-1:1,1);
        }
        return m;
    }

    private int fboBuffer[] = new int[1];
    private int fboRenderBuffer[] = new int[1];
    private  int fboTextureBuffer [] = new int[2];
    private ByteBuffer mBuffer;
    //需要提前初始化
    private Bitmap mBitmap;
    private void createEnv(){
        /***********************************************************/
        GLES20.glGenFramebuffers(1,fboBuffer,0);
        glBindFramebuffer(GL_FRAMEBUFFER,fboBuffer[0]);

        //FBO的renderbuffer 附件 针对renderbuffer进行处理的操作
        GLES20.glGenRenderbuffers(1, fboRenderBuffer,0);
        glBindRenderbuffer(GL_RENDERBUFFER, fboRenderBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                mBitmap.getWidth(), mBitmap.getHeight());
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, fboRenderBuffer[0]);
        //解绑renderbuffer
        glBindRenderbuffer(GL_RENDERBUFFER,0);


        //FBO的纹理附件
        glGenTextures(2,fboTextureBuffer,0);
        for (int i = 0;i<2;i++){

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureBuffer[i]);
           if(i == 0){
               GLUtils.texImage2D(GL_TEXTURE_2D,0,GLES20.GL_RGBA,mBitmap,0);
           } else {
               glTexImage2D(GL_TEXTURE_2D,0,GLES20.GL_RGBA,mBitmap.getWidth(),mBitmap.getHeight(),
                       0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,null);
           }
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        //从GPU 读取已经处理好的FBO缓冲 buffer
        mBuffer = ByteBuffer.allocate(mBitmap.getWidth() * mBitmap.getHeight() * 4);
    }

    private void deleteEnvi() {
        GLES20.glDeleteTextures(2, fboTextureBuffer, 0);
        GLES20.glDeleteRenderbuffers(1, fboRenderBuffer, 0);
        GLES20.glDeleteFramebuffers(1, fboBuffer, 0);
    }
    private int buildProgram(String vertexShaderStr,String fragementShader){
        int program;

        // Compile the shaders.
        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderStr);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragementShader);
        // Link them into a shader program.
        program = linkProgram(vertexShader, fragmentShader);
        validateProgram(program);

        return program;
    }

    private  int compileShader(int type, String shaderCode) {
        // Create a new shader object.
        Log.i(TAG, "shaderObjectId begin :" +type + " ," + shaderCode);
        final int shaderObjectId = glCreateShader(type);
        Log.i(TAG, "shaderObjectId :" +shaderObjectId);
        if (shaderObjectId == 0) {
            Log.i(TAG, "Could not create new shader.");
            return 0;
        }

        // Pass in the shader source.
        glShaderSource(shaderObjectId, shaderCode);

        // Compile the shader.
        glCompileShader(shaderObjectId);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

        Log.i(TAG, "Results of compiling source:" + glGetShaderInfoLog(shaderObjectId));

        // Verify the compile status.
        if (compileStatus[0] == 0) {
            // If it failed, delete the shader object.
            glDeleteShader(shaderObjectId);
            Log.i(TAG, "Compilation of shader failed.");
            return 0;
        }

        // Return the shader object ID.
        return shaderObjectId;
    }

    public  int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // Create a new program object.
        final int programObjectId = glCreateProgram();

        if (programObjectId == 0) {
            Log.i(TAG, "Could not create new program");
            return 0;
        }
        // Attach the vertex shader to the program.
        glAttachShader(programObjectId, vertexShaderId);
        // Attach the fragment shader to the program.
        glAttachShader(programObjectId, fragmentShaderId);

        // Link the two shaders together into a program.
        glLinkProgram(programObjectId);

        // Get the link status.
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

        // Print the program info log to the Android log output.
        Log.i(TAG,"Results of linking program:" + glGetProgramInfoLog(programObjectId));

        // Verify the link status.
        if (linkStatus[0] == 0) {
            // If it failed, delete the program object.
            glDeleteProgram(programObjectId);
            Log.w(TAG, "Linking of program failed.");
            return 0;
        }

        // Return the program object ID.
        return programObjectId;
    }


    public static final int BYTES_PER_FLOAT = 4;
    private void createVertexArray() {
        verFloatBuffer = ByteBuffer
                .allocateDirect(vertex.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertex);
        verFloatBuffer.position(0);
        texFloatBuffer= ByteBuffer
                .allocateDirect(texture.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(texture);
        texFloatBuffer.position(0);

    }
    public  boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        Log.i(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    public interface OnDataDrawWithFilter {
        void onData(ByteBuffer buffer);
    }
}
