package com.opengldemo.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;

import com.opengldemo.CameraManager;
import com.opengldemo.CodecController;
import com.opengldemo.GLesUtils;
import com.opengldemo.TextureRotateUtil;
import com.opengldemo.bean.Rotation;
import com.opengldemo.factory.BeautyFilterType;
import com.opengldemo.filter.BaseFilter;
import com.opengldemo.filter.CameraBaseFilter;
import com.opengldemo.filter.CameraBlurFilter;
import com.opengldemo.filter.CameraColorInvertFilter;
import com.opengldemo.filter.CameraWeakPixInclusion;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

//todo 调试录制视频功能

public class CodecRender implements GLSurfaceView.Renderer , SurfaceTexture.OnFrameAvailableListener {

    private String TAG ="CodecRender";

    private Context mContext ;
    private Handler mHandler;
    protected int mSurfaceWidth, mSurfaceHeight;
    private final float[] mMatrix = new float[16];
    private GLSurfaceView mPreviewSurface;
    private CameraBaseFilter mCameraBaseFilter = null;
    private BaseFilter mAddCameraFilter =null;
    private CodecController mCodecManager;

    public CodecRender(Context context , Handler handler,GLSurfaceView surfaceView) {
        mContext = context;
        mHandler = handler;
        mPreviewSurface = surfaceView;
        mPreviewSurface.setEGLContextClientVersion(3);
        mPreviewSurface.setRenderer(this);
        mPreviewSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mCodecManager = new CodecController(context);
        mRecordStatus = RECORDER_OFF;
        createVertexArray();
    }

    private int cameraTextureId = -1;
    SurfaceTexture surfaceTexture = null;
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if(mHandler == null){
            return;
        }
        GLES30.glDisable(GL10.GL_DITHER);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glEnable(GL10.GL_CULL_FACE);
        GLES30.glEnable(GL10.GL_DEPTH_TEST);

        //创建基本的相机处理滤镜
        mCameraBaseFilter = new CameraBaseFilter(mContext);
        mCameraBaseFilter.init();

        //创建相机纹理
        cameraTextureId = GLesUtils.createCameraTexture();
        if (cameraTextureId != -1) {
            surfaceTexture = new SurfaceTexture(cameraTextureId);
            surfaceTexture.setOnFrameAvailableListener(this);
        }
        //初始化相机
        initCamera();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        onSurfaceChanged ,width -->1080,height-->1977
        Log.i(TAG,"onSurfaceChanged ,width -->"+width +
                ",height-->"+height );
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        //启动预览
        cameraManager.startPreview(surfaceTexture);
        onFilterChanged();
        Log.i(TAG,"onSurfaceChanged ,startPreview end" );
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //启动编码
//        Log.d(TAG,"onDrawFrame");
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mMatrix);
//        mMatrix--->[1.0, 0.0, 0.0, 0.0,
//                    0.0, 1.0, 0.0, 0.0,
//                    0.0, 0.0, 1.0, 0.0,
//                    0.0, 0.0, 0.0, 1.0
//                    ]
        mCameraBaseFilter.setTextureTransformMatrix(mMatrix);
        int id = cameraTextureId;

        //使用滤镜对预览进行处理，输入是：创建的基础纹理
        // 处理是：预览是基础相机绘制纹理 + 滤镜纹理
        // 输出是：进行相关滤镜处理后的 纹理id
        // 输出的纹理id 会被用在codec编码


        //如果没有额外额的滤镜 直接使用基础滤镜进行绘制onDrawFrame();
        if (mAddCameraFilter == null) {
             int result = mCameraBaseFilter.onDrawFrame(cameraTextureId, mVertexBuffer, mTextureBuffer);
            Log.d(TAG,"cameraTextureId :" +cameraTextureId );

        } else {
            //否则就先使用相机基础滤镜绘制到 cameraTextureId 这个纹理上，
            // 然后再这个纹理上进行 其他滤镜(mAddCameraFilter)绘制，依然是往这个纹理上绘制
            id = mCameraBaseFilter.onDrawToTexture(cameraTextureId);
            int result =  mAddCameraFilter.onDrawFrame(id, mVertexBuffer, mTextureBuffer);
            Log.d(TAG,"mAddCameraFilter != null  :" +result );
        }

        onRecorder(id);
//        Log.d(TAG,"mMatrix--->"+ Arrays.toString(mMatrix));

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.d(TAG,"onFrameAvailable ");
//        requestRender();
        mPreviewSurface.requestRender();
    }


    private volatile boolean recordEnable = false;
    private int mRecordStatus = -1;

    private final int RECORDER_OPEN = 0;
    private final int RECORDER_RESUME= 1;
    private final int RECORDER_OFF = 2;

    private int mPreviewWidth = -1;
    private int mPreviewHeight = -1;
    private static final int videoBitrate = 6 * 1024 * 1024;

    //录制视频
    private void onRecorder(int textureId){
            if(recordEnable){
            switch (mRecordStatus){
                case RECORDER_OFF:
                    mCodecManager.setPreviewSize(mImageWidth, mImageHeight);
                    mCodecManager.setTextureBuffer(mTextureBuffer);
                    mCodecManager.setVertexBuffer(mVertexBuffer);
                    String videoPath = mContext.getCacheDir().getPath();
                    String videoName = "codec_recorder.mp4";
                    File outPath = new File(videoPath, videoName);
                    CodecController.RecorderConfig recorderConfig =
                            new CodecController.RecorderConfig(mImageWidth, mImageHeight,
                                    videoBitrate, outPath, EGL14.eglGetCurrentContext() );
                    mCodecManager.startRecode( recorderConfig );
                    mRecordStatus = RECORDER_OPEN;
                    break;
                case RECORDER_OPEN:
                    break;
                case RECORDER_RESUME:
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordStatus);
            }
        } else {
                switch (mRecordStatus) {
                    case RECORDER_OPEN:
                    case RECORDER_RESUME:
                        mCodecManager.stopRecording();
                        mRecordStatus = RECORDER_OFF;
                        break;
                    case RECORDER_OFF:
                        break;
                    default:
                        throw new RuntimeException("unknown status " + mRecordStatus);
                }
        }
        mCodecManager.setTextureId(textureId);
        mCodecManager.frameAvailable(surfaceTexture);
    }

    public void setRecord(boolean isStartRecord){
        recordEnable = isStartRecord;
    }

    public boolean isRecordEnable(){
        return recordEnable;
    }

    //相机开启成功之后 拿到的预览数据大小，由相机给出
    private int mImageWidth = -1;
    private int mImageHeight = -1;
    CameraManager cameraManager;
    private void initCamera() {
        cameraManager = new CameraManager();

        if (cameraManager.getCamera() == null)
            cameraManager.openCamera();
        Camera.Size size = cameraManager.getPreviewSize();
        // rotation=90 or rotation=270, we need to exchange width and height
        if (cameraManager.getOrientation() == 90 || cameraManager.getOrientation() == 270) {
            mImageWidth = size.height;
            mImageHeight = size.width;
        } else {
            mImageWidth = size.width;
            mImageHeight = size.height;
        }
        //调整相机出来的视频 +纹理坐标位置
        mCameraBaseFilter.onInputSizeChanged(mImageWidth, mImageHeight);
        //        initCamera mImageWidth -->720,mImageHeight-->1280
        Log.d(TAG,"initCamera mImageWidth -->" +mImageWidth
                + ",mImageHeight-->" +mImageHeight);
        Log.i(TAG,"initCamera END");

        adjustSize(cameraManager.getOrientation(), cameraManager.isFront(), true);
    }

    public void adjustSize(int rotation, boolean horizontalFlip, boolean verticalFlip) {
        float[] vertexData = TextureRotateUtil.VERTEX;
        float[] textureData = TextureRotateUtil.getRotateTexture(Rotation.fromInt(rotation),
                horizontalFlip, verticalFlip);

        mVertexBuffer.clear();
        mVertexBuffer.put(vertexData).position(0);
        mTextureBuffer.clear();
        mTextureBuffer.put(textureData).position(0);

    }

    private FloatBuffer mVertexBuffer = null;
    private FloatBuffer mTextureBuffer = null;
    public static final int BYTES_PER_FLOAT = 4;
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
            Log.d(TAG,"createVertexArray :" +Arrays.toString(mVertexBuffer.array()) );
        }
        if(mTextureBuffer.hasArray()){
            Log.d(TAG,"createVertexArray :" +Arrays.toString(mTextureBuffer.array()) );
        }

    }

    private  void onFilterChanged(){
        Log.i(TAG,"onFilterChanged begin");
        if(mAddCameraFilter!=null){
            mAddCameraFilter.onInputSizeChanged(mImageWidth, mImageHeight);
            mAddCameraFilter.onOutputSizeChanged(mSurfaceWidth, mSurfaceHeight);
        }

        mCameraBaseFilter.onOutputSizeChanged(mSurfaceWidth, mSurfaceHeight);

        if (mAddCameraFilter != null)
            mCameraBaseFilter.initFrameBuffer(mImageWidth, mImageHeight);
        else
            mCameraBaseFilter.destroyFrameBuffer();

        Log.i(TAG,"onFilterChanged END");
    }


    public void setFilter(BeautyFilterType type){
        BaseFilter baseFilter = getBaseFilter(type);
        if(baseFilter == null){
            Log.i(TAG,"setFilter no filter");
            return;
        }
        setFilter(baseFilter);
    }

    private void setFilter(BaseFilter filter){
        Log.i(TAG,"setFilter begin ");
        if(mPreviewSurface != null){
            mPreviewSurface.queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (mAddCameraFilter != null)
                        mAddCameraFilter.destroy();
                    mAddCameraFilter = filter;
                    if (mAddCameraFilter != null)
                        mAddCameraFilter.init();
                    onFilterChanged();
                }
            });
        }
        Log.i(TAG,"setFilter  end");
    }

    private BaseFilter getBaseFilter(BeautyFilterType type){
        BaseFilter result = null;
            if(type == BeautyFilterType.BLUR){
                result = new CameraBlurFilter(mContext);
            }else if(type == BeautyFilterType.COLOR_INVERT){
                result = new CameraColorInvertFilter(mContext);
            }else if(type == BeautyFilterType.WEAK_PIXEL_INCLUSION){
                result = new CameraWeakPixInclusion(mContext);
            }
        return result;
    }

}
