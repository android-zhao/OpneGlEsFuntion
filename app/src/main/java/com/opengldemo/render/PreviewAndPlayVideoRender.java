package com.opengldemo.render;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glValidateProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;
import com.opengldemo.filter.playcvideo.PlayVideoNormalFilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PreviewAndPlayVideoRender implements GLSurfaceView.Renderer {

    private String TAG = "PreviewAndPlayVideoRender";
    Context mContext;
    Handler mHandler;
    private int OPEN_CAMERA=0;
    int surfaceWidth = -1;//窗口宽
    int surfaceHeight  = -1;//窗口高
    private FloatBuffer floatBuffer = null;
    public static final int BYTES_PER_FLOAT = 4;
    private PlayVideoNormalFilter mPlayVideoNormalFilter = null;


    public PreviewAndPlayVideoRender(Context context, Handler handler){
        this.mContext = context;
        this.mHandler = handler;
        createVertexArray();
        Log.i(TAG,"PreviewAndPlayVideoRender END");
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG,"onSurfaceCreated ");
        glDisable(GLES20.GL_DITHER);
        glEnable(GLES20.GL_CULL_FACE);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        createCameraTexture();
        Log.i(TAG,"mCameraTextureId :" +mCameraTextureId);

        createCodecTexture();
        Log.i(TAG,"mCodecTextureId:" + mCodecTextureId);

        updateTextureAndVertxy();
        Log.i(TAG,"update shader end  ");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        Log.i(TAG,"onSurfaceChanged , width:" +width + ", height " +height);
        surfaceWidth = width;
        surfaceHeight = height;
//        mPreviewSize = new Size(width,height);
        createCameraTexture();
        Message message = Message.obtain();
        message.what =OPEN_CAMERA;
        mHandler.sendMessage(message);


    }
    private boolean isUpdateVertex = false;
    @Override
    public void onDrawFrame(GL10 gl) {
        Log.i(TAG,"onDrawFrame");
        if(isUpdateVertex){
            updateTextureAndVertxy();
            isUpdateVertex = false;
        }
        drawTexture();
    }

    private void createTexture(){
        cameraSurfaceTexture = new SurfaceTexture(mCameraTextureId);
    }

    public SurfaceTexture getTexture(){
        return cameraSurfaceTexture;
    }
    //顶点坐标数量
    private static final int POSITION_COMPONENT_COUNT = 2;
    //    private static final int POSITION_COMPONENT_COUNT = 3;
    //颜色值坐标
    private static final int COLOR_COMPONENT_COUNT = 3;

    //纹理值坐标
    private static final int TEXTURE_COMPONENT_COUNT = 2;

    // 顶点坐标 + 颜色坐标合成的stride
    private static final int STRIDE_VERTEX_COLOR =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT)
                    * BYTES_PER_FLOAT;

    // 顶点坐标 + 纹理坐标合成的stride
    private static final int STRIDE_VERTEX_TEXTURE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT)
                    * BYTES_PER_FLOAT;
    private SurfaceTexture cameraSurfaceTexture = null;
    float[] mtx = new float[16];
    private void drawTexture(){
        glClear(GL_COLOR_BUFFER_BIT);
        if(cameraSurfaceTexture == null){
            Log.i(TAG," CAMERA IS NOT READY ");
            return;
        }

        cameraSurfaceTexture.updateTexImage();
        cameraSurfaceTexture.getTransformMatrix(mtx);


        //开始使用 gpu小程序
        glUseProgram(mRendeId);
        //激活纹理并通知采样函数去哪个纹理插槽去采样
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,mCameraTextureId);
        glUniform1i(uTextureUnitLocation,0);

        glUniformMatrix4fv(uTextureTransformLocation, 1, false, mtx, 0);


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




    void createVertexArray(){
        //1：创建顶点坐标和纹理坐标系
//        float CUBE[] = {
//                //翻转顶点信息中的纹理坐标,统一用1去减
//                -1.0f, -1.0f,  /* 顶点0*/ 0f, 1f - 0f,/* 纹理0*/
//                1.0f, -1.0f, /* 顶点1*/ 1f,   1f -0f, /* 纹理1*/
//                -1.0f, 1.0f, /* 顶点2 */0f,  1f -1f, /* 纹理2*/
//                1.0f, 1.0f, /* 顶点3 */ 1f,  1f -1f, /* 纹理3*/
//        };
        float CUBE[] = {
                //翻转顶点信息中的纹理坐标,统一用1去减
                -1.0f, -1.0f,  /* 顶点0*/ 0f, 0f,/* 纹理0*/
                1.0f, -1.0f, /* 顶点1*/ 1f,   0f, /* 纹理1*/
                -1.0f, 1.0f, /* 顶点2 */0f,  1f , /* 纹理2*/
                1.0f, 1.0f, /* 顶点3 */ 1f,  1f, /* 纹理3*/
        };


        floatBuffer = ByteBuffer
                .allocateDirect(CUBE.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(CUBE);
    }
    private int mCameraTextureId;
    private int mCodecTextureId;

    private void createCameraTexture(){
        mCameraTextureId = GLesUtils.createCameraTexture();
        createTexture();
    }
    public int getCameraTextureId() {
        return mCameraTextureId;
    }

    //创建codec的纹理
    private void createCodecTexture(){
        mCodecTextureId = GLesUtils.createCameraTexture();

    }

    private int getCodecTextureId(){
        return mCodecTextureId;
    }

    private  int uTextureUnitLocation  = -1;
    private  int uTextureTransformLocation = -1;
    private  int aTextureCoordinatesLocation = -1;
    protected static final String U_TEXTURE_OES_UNIT = "u_TextureOESUnit";
    protected static final String U_TEXTURE_TRANSFORM = "u_textureTransform";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    private void updateTextureAndVertxy() {

        //2：创建shader
        updateTextureShader();

        //3：指定顶点坐标和shader中由cpu设置的相关参数

        // 纹理采样器 id uTextureUnitLocation
        uTextureUnitLocation =  glGetUniformLocation(mRendeId, U_TEXTURE_OES_UNIT);
        //视图的变化矩阵
        uTextureTransformLocation = glGetUniformLocation(mRendeId, U_TEXTURE_TRANSFORM);
        //顶点坐标 顶点0 1 2 3
        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
        //纹理坐标 在cube 数组中声明的
        aTextureCoordinatesLocation = glGetAttribLocation(mRendeId, A_TEXTURE_COORDINATES);

        Log.i(TAG,"updateTextureAndVertxy floatBuffer size :" + floatBuffer.capacity());
        Log.i(TAG,"updateTextureAndVertxy uTextureUnitLocation  :" + uTextureUnitLocation);
        Log.i(TAG,"updateTextureAndVertxy aPositionLocation  :" + aPositionLocation);
        Log.i(TAG,"updateTextureAndVertxy mRendeId  :" + mRendeId);
        Log.i(TAG,"updateTextureAndVertxy aTextureCoordinatesLocation  :" + aTextureCoordinatesLocation);


        Log.i(TAG,"updateTextureAndVertxy end");
    }

    private int mRendeId  =-1;
    private  int aPositionLocation = -1;
    private  int aColorLocation = -1;
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";

    private void updateTextureShader() {
        String vertxtStr  = null;
        String framentStr = null;
        vertxtStr = GLesUtils.readTextFileFromResource(mContext, R.raw.simple_texture_oes_vertex_shader);

        //todo 待处理双纹理 simple_texture_oes_fragment_shader
//        framentStr = GLesUtils.readTextFileFromResource(mContext,R.raw.camera2_dobule_texture);
        framentStr = GLesUtils.readTextFileFromResource(mContext,R.raw.simple_texture_oes_fragment_shader);
        mRendeId = buildProgram(vertxtStr, framentStr);
        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
//        aColorLocation = glGetAttribLocation(mRendeId, A_COLOR);
        Log.i(TAG,"updateTextureShader end");
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

    public  boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        Log.i(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }



}
