package com.opengldemo.render;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;


import com.opengldemo.GLesUtils;
import com.opengldemo.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
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
import static android.opengl.GLES20.glDrawArrays;
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
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glValidateProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class SimpleRender implements GLSurfaceView.Renderer {
    private String TAG = "SimpleRender";

    private  int aPositionLocation;
    private  int aColorLocation;
    private Context mContext;
    // todo glsl 中待更新
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";

    private  FloatBuffer floatBuffer = null;
    public static final int BYTES_PER_FLOAT = 4;
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

    private int mRendeId = 0;
    private int mTextureId = 0 ;
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    //顶点缓存 vertxtArray Buffer + 颜色坐标（ARGB/RGBA）

//    public static final float CUBE[] = {
//            -1.0f, -1.0f, /* 0 */ 1f, 0f, 0f,  0.0f,0.0f,//纹理坐标0
//            1.0f, -1.0f,  /* 1 */  1f, 0f, 0f, 1.0f,0.0f,//纹理坐标1
//            -1.0f, 1.0f,  /* 2 */ 1f, 0f, 0f,  1.0f,1.0f,//纹理坐标2
//            1.0f, 1.0f,   /* 3 */ 1f, 0f, 0f,  0.0f,1.0f//纹理坐标3
//    };
    public SimpleRender(Context context) {
        mContext = context;
//        (CUBE);

        //1：创建顶点坐标和纹理坐标系
        float CUBE[] = {
                //翻转顶点信息中的纹理坐标,统一用1去减
                -1.0f, -1.0f,  /* 顶点0*/ 0f, 1f - 0f,/* 纹理0*/
                1.0f, -1.0f, /* 顶点1*/ 1f,   1f -0f, /* 纹理1*/
                -1.0f, 1.0f, /* 顶点2 */0f,  1f -1f, /* 纹理2*/
                1.0f, 1.0f, /* 顶点3 */ 1f,  1f -1f, /* 纹理3*/
        };

        floatBuffer = ByteBuffer
                .allocateDirect(CUBE.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(CUBE);
    }

    private void updateTexture() {
        mTextureId = GLesUtils.loadjpg(mContext);
        //默认0slot位置上纹理
//        glActiveTexture(GL_TEXTURE0);
//        glBindTexture(GL_TEXTURE_2D,textureId);
    }


    private void updateShader() {
        String vertxtStr  = null;
        String framentStr = null;
        vertxtStr = readTextFileFromResource(mContext, R.raw.simple_vertex_shader);
        framentStr = readTextFileFromResource(mContext,R.raw.simple_fragment_shader);
        mRendeId = buildProgram(vertxtStr, framentStr);
        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
        aColorLocation = glGetAttribLocation(mRendeId, A_COLOR);
    }
    private void updateTextureShader() {
        String vertxtStr  = null;
        String framentStr = null;
        vertxtStr = readTextFileFromResource(mContext, R.raw.simple_texture_vertex);
        framentStr = readTextFileFromResource(mContext,R.raw.simple_texture_frament);
        mRendeId = buildProgram(vertxtStr, framentStr);
        aPositionLocation = glGetAttribLocation(mRendeId, A_POSITION);
        aColorLocation = glGetAttribLocation(mRendeId, A_COLOR);
    }

    public static String readTextFileFromResource(Context context,
                                                  int resourceId) {
        StringBuilder body = new StringBuilder();

        try {
            InputStream inputStream =
                    context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine;

            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not open resource: " + resourceId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resource not found: " + resourceId, nfe);
        }

        return body.toString();
    }

    private void updateVertexArray(float[] vertexData){
        floatBuffer = ByteBuffer
                .allocateDirect(vertexData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 1.0f, 0.0f, 0.0f);

//        updateShader();
        Log.i(TAG,"onSurfaceCreated " );
        updateTexture();
        updateTextureAndVertxy();

    }
    private  int uTextureUnitLocation  = -1;
    private  int aTextureCoordinatesLocation = -1;
    private void updateTextureAndVertxy() {

        //2：创建shader
        updateTextureShader();

        //3：指定顶点坐标和shader中由cpu设置的相关参数
        // 纹理采样器 id uTextureUnitLocation
        uTextureUnitLocation =  glGetUniformLocation(mRendeId, U_TEXTURE_UNIT);
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

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    //绘制每一帧
    @Override
    public void onDrawFrame(GL10 gl) {
//        gl.glClear(GL_COLOR_BUFFER_BIT);
//        drawallColor();
        Log.i(TAG, "onDrawFrame" );
        drawTexture();
    }

    private void drawTexture(){
        glClear(GL_COLOR_BUFFER_BIT);
        //开始使用 gpu小程序
        glUseProgram(mRendeId);
        //激活纹理并通知采样函数去哪个纹理插槽去采样
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,mTextureId);
        glUniform1i(uTextureUnitLocation,0);

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
    private void drawallColor() {
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(mRendeId);
        //设置顶点坐标
        floatBuffer.position(0);
        glVertexAttribPointer(aPositionLocation,POSITION_COMPONENT_COUNT,
                GL_FLOAT,false, STRIDE_VERTEX_COLOR,floatBuffer);
        glEnableVertexAttribArray(aPositionLocation);
        floatBuffer.position(0);

        //设置颜色坐标

        floatBuffer.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(aColorLocation,COLOR_COMPONENT_COUNT,
                GL_FLOAT,false, STRIDE_VERTEX_COLOR,floatBuffer);
        glEnableVertexAttribArray(aColorLocation);
        floatBuffer.position(0);
        //
        //开始绘画
        glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
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

    /**
     * Validates an OpenGL program. Should only be called when developing the
     * application.
     */
    public  boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        Log.i(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
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


    private  int compileShader(int type, String shaderCode) {
        // Create a new shader object.
        final int shaderObjectId = glCreateShader(type);

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
}
