package com.opengldemo.render;

import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.opengldemo.TextureRotateUtil;
import com.opengldemo.codec.CodecParams;
import com.opengldemo.codec.MediaCodecManager;
import com.opengldemo.filter.playcvideo.PlayVideoNormalFilter;
import com.opengldemo.view.MediaUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlayVideoByOpenglRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

   private static final String TAG = "PlayVideoByOpenglRender";

   private Context mContext ;
   private Surface mPreviewSurface = null;
   private String mVideoPath = "";

   private FloatBuffer mVertexBuffer = null;
   private FloatBuffer mTextureBuffer = null;
   private int previewTextureId = -1;
   private  SurfaceTexture mSurfaceTexture = null;
   private PlayVideoNormalFilter mPlayVideoNormalFilter = null;

   public PlayVideoByOpenglRender(Context context) {
      mContext = context;
      loadVideo();
   }

   private boolean loadVideo() {
//      String videoPath = "/data/data/com.opengldemo/cache/59733_720p.mp4";
      String videoPath = "/data/data/com.opengldemo/cache/test.mp4";
      // todo 检查视频的可用性等
      mVideoPath = videoPath;
      return true;
   }

   @Override
   public void onSurfaceCreated(GL10 gl, EGLConfig config) {
      Log.i(TAG,"onSurfaceCreated");
      //1：准备 opengl环境 包括顶点坐标 和创建纹理
      prepareOpenglEnv();


      GLES30.glDisable(GL10.GL_DITHER);
      GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
      GLES30.glEnable(GL10.GL_CULL_FACE);
      GLES30.glEnable(GL10.GL_DEPTH_TEST);

      //2：创建展示视频的基本filter
      mPlayVideoNormalFilter = new PlayVideoNormalFilter(mContext);
      mPlayVideoNormalFilter.init();

      //3：准备MediaPlayer环境播放视频
      initMediaPlayer();

   }

   private int viewWidth = -1; //窗口的宽
   private int viewHeight = -1;//窗口的高
   @Override
   public void onSurfaceChanged(GL10 gl, int width, int height) {
      Log.e(TAG,
              "线程名: " + Thread.currentThread().getName() +
                      "-----onVideoSizeChanged: (" + width + " , " + height + ")"
      );
      viewWidth = width;
      viewHeight = height;

      updateProjection();
      GLES30.glViewport(0, 0, viewWidth, viewHeight);
   }

   private float[] sTMatrix = new float[16];
   private final float[] projectionMatrix = new float[16];
   private void updateProjection() {
      float viewRatio = (float) viewWidth / viewHeight;

      float videoRatio = (float) MediaUtils.getVideoWidth(mVideoPath) / MediaUtils.getVideoHeight(mVideoPath);
      //正交投影矩阵
      Matrix.orthoM(projectionMatrix, 0,
              - 1, 1, -1, 1,
              -1f, 1f);
   }

   private volatile boolean isUpdateTexture = false;
   @Override
   public void onDrawFrame(GL10 gl) {

      synchronized (this){

         if(isUpdateTexture){
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(sTMatrix);
            mPlayVideoNormalFilter.setTextureTransformMatrix(sTMatrix);
            isUpdateTexture = false;
         }

         mPlayVideoNormalFilter.onDrawFrame(previewTextureId,mVertexBuffer,mTextureBuffer);
      }
   }

   @Override
   public void onFrameAvailable(SurfaceTexture surfaceTexture) {
      Log.i(TAG,"onFrameAvailable "+ Thread.currentThread().getName() );
      isUpdateTexture = true;
   }

   private void initMediaPlayer(){
      mSurfaceTexture = new SurfaceTexture(previewTextureId);
      mSurfaceTexture.setOnFrameAvailableListener(this);
      mPreviewSurface = new Surface(mSurfaceTexture);

      MediaCodecManager manager = MediaCodecManager.getInstance(mContext);

      CodecParams codecParams = new CodecParams();
      codecParams.setVideoPath(mVideoPath);

      //1：设置解码参数
      manager.setMediaParams(codecParams);
      //2：设置预览的surface
      manager.setPreviewSurface(mPreviewSurface);
      //3：启动播放
      manager.startPlay();

      Log.i(TAG,"initMediaPlayer end");

   }

   private void prepareOpenglEnv(){
      createVertexArray();
      previewTextureId = createTexture();
   }



   //创建顶点和纹理坐标
   void createVertexArray(){
      //1：创建顶点坐标和纹理坐标系
      mVertexBuffer = ByteBuffer.allocateDirect(TextureRotateUtil.VERTEX.length * 4)
              .order(ByteOrder.nativeOrder())
              .asFloatBuffer();
      mVertexBuffer.put(TextureRotateUtil.VERTEX).position(0);

      mTextureBuffer = ByteBuffer.allocateDirect(TextureRotateUtil.TEXTURE_ROTATE_0.length * 4)
              .order(ByteOrder.nativeOrder())
              .asFloatBuffer();
      mTextureBuffer.put(TextureRotateUtil.TEXTURE_ROTATE_0).position(0);
      if(mVertexBuffer.hasArray() ){
         Log.d(TAG,"createVertexArray :" + Arrays.toString(mVertexBuffer.array()) );
      }
      if(mTextureBuffer.hasArray()){
         Log.d(TAG,"createVertexArray :" +Arrays.toString(mTextureBuffer.array()) );
      }

   }

   private int createTexture(){
      int[] texture = new int[1];
      glGenTextures(1, texture, 0);
      glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
      GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
      GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
      GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
      GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
//        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
      return texture[0];
   }

}
