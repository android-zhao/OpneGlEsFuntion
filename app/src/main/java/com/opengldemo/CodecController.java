package com.opengldemo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.opengldemo.codec.AVRecorderCore;
import com.opengldemo.egl.EglCore;
import com.opengldemo.egl.WindowEglSurface;
import com.opengldemo.factory.BeautyFilterType;
import com.opengldemo.filter.BaseFilter;
import com.opengldemo.filter.CameraBaseFilter;
import com.opengldemo.filter.CameraBlurFilter;
import com.opengldemo.filter.CameraColorInvertFilter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;


public class CodecController implements  Runnable{
    private static final String TAG = "CodecController";
    Context mContext ;
    RecorderHandler mRecordHandler = null;
    AVRecorderCore mEncodeCore ;

    public CodecController(Context context) {
        mContext = context;

    }


    private final Object mReadyFence = new Object();
    private boolean mReady;
    private boolean mRunning;

    public void startRecode(RecorderConfig config){
        Log.i(TAG,"startRecode with config");
        synchronized (mReadyFence) {
            if (mRunning) {
                return;
            }
            mRunning = true;
            new Thread(this, TAG).start();
            while (!mReady) {
                try {
                    mReadyFence.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Message message = Message.obtain();
        message.what = MSG_START_RECORDING;
        message.obj = config;
        mRecordHandler.sendMessage(message);
    }

    public void stopRecording(){
        mRecordHandler.sendMessage(mRecordHandler.obtainMessage(MSG_STOP_RECORDING));
        mRecordHandler.sendMessage(mRecordHandler.obtainMessage(MSG_QUIT_RECORDING));
    }

    public void setTextureId(int textureId ){
        synchronized (mReadyFence) {
            if (!mReady)
                return;
        }
        mRecordHandler.sendMessage(mRecordHandler.obtainMessage(MSG_SET_TEXTURE_ID, textureId, 0, null));
    }


    private float[] mTransformMatrix  ;
    public void frameAvailable(SurfaceTexture surfaceTexture){
        synchronized (mReadyFence) {
            if (!mReady)
                return;
        }
        if (mTransformMatrix == null) {
            mTransformMatrix = new float[16];
        }
        surfaceTexture.getTransformMatrix(mTransformMatrix);
        long timestamp = surfaceTexture.getTimestamp();
        if (timestamp == 0) {
            return;
        }
        //todo 时间戳传递问题
        mRecordHandler.sendMessage(mRecordHandler.obtainMessage(MSG_FRAME_AVAILABLE,
                (int) (timestamp >> 32), (int) timestamp, mTransformMatrix));

        //todo 如何向另外一个线程传递
//        mEncodeCore.frameAvailable(surfaceTexture);
    }

    private int mVideoWidth = -1;
    private int mVideoHeight = -1;
    private CameraBaseFilter mCameraFilter = null;
    private BeautyFilterType type = BeautyFilterType.NONE;
    private BaseFilter mAddFilter = null;
    private WindowEglSurface mWindowSurface;
    private EglCore mEglCore;
    //todo 並未使用egl自定义
    private void handlerStartRecording(RecorderConfig config){

        Log.i(TAG,"handlerStartRecording: width-->" +config.mWidth +
                "height:"  +config.mHeight + ",bitrate -->" +config.mBitrate + ",file: " +config.mOutputFile);

        mEncodeCore = new AVRecorderCore(mContext,config.mWidth,config.mHeight,
                config.mBitrate,config.mOutputFile);
        mVideoWidth = config.mWidth;
        mVideoHeight = config.mHeight;

        mEglCore = new EglCore(config.mEglContext,EglCore.FLAG_RECORDABLE);
        mWindowSurface = new WindowEglSurface(mEglCore,mEncodeCore.getInputSurface(),false);
        mWindowSurface.makeCurrent();

        mCameraFilter = new CameraBaseFilter(mContext);
        mCameraFilter.init();

        mAddFilter = getBaseFilter(type);
        if (mAddFilter != null) {
            mAddFilter.init();
            mAddFilter.onOutputSizeChanged(mVideoWidth, mVideoHeight);
            mAddFilter.onInputSizeChanged(mPreviewWidth, mPreviewHeight);
        }
    }

    private void handleFrameAvailable(float[] transform, long timestamp){
        mEncodeCore.drainEncoder(false);
        mCameraFilter.setTextureTransformMatrix(transform);
        if (mAddFilter == null) {
            mCameraFilter.onDrawFrame(mTextureId, glVertexBuffer, glTextureBuffer);
        } else {
            mAddFilter.onDrawFrame(mTextureId, glVertexBuffer, glTextureBuffer);
        }

        mWindowSurface.setPresentationTime(timestamp);
        mWindowSurface.swapBuffers();
    }
    private void handlerStopRecording(){
        mEncodeCore.drainEncoder(true);
        releaseRecorder();
    }
    private void releaseRecorder() {
        mEncodeCore.release();
        if (mWindowSurface != null) {
            mWindowSurface.release();
            mWindowSurface = null;
        }
        if (mCameraFilter != null) {
            mCameraFilter.destroy();
            mCameraFilter = null;
        }
        if (mAddFilter != null) {
            mAddFilter.destroy();
            mAddFilter = null;
            type = BeautyFilterType.NONE;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    private int mTextureId = -1;
    private void handleSetTexture(int id) {
        mTextureId = id;
    }

    //前台进行预览的宽和高
    private int mPreviewWidth = -1;
    private int mPreviewHeight = -1;
    public void setPreviewSize(int width, int height){
        mPreviewWidth = width;
        mPreviewHeight = height;
    }
    private FloatBuffer glVertexBuffer;
    private FloatBuffer glTextureBuffer;

    public void setTextureBuffer(FloatBuffer glTextureBuffer) {
        this.glTextureBuffer = glTextureBuffer;
    }

    public void setVertexBuffer(FloatBuffer gLVertexBuffer) {
        this.glVertexBuffer = gLVertexBuffer;
    }


    @Override
    public void run() {
        Looper.prepare();
        synchronized (mReadyFence) {
            mRecordHandler = new RecorderHandler(this);
            mReady = true;
            mReadyFence.notify();
        }
        Looper.loop();
        synchronized (mReadyFence) {
            mReady = false;
            mRunning = false;
            mRecordHandler = null;
        }
    }

    public static class RecorderConfig{
        final int mWidth;
        final int mHeight;
        final int mBitrate;
        final File mOutputFile;
        final EGLContext mEglContext;

        public RecorderConfig(int width, int height, int bitrate, File outputFile, EGLContext eglContext) {
            this.mWidth = width;
            this.mHeight = height;
            this.mBitrate = bitrate;
            this.mOutputFile = outputFile;
            this.mEglContext = eglContext;
        }

//        public RecorderConfig(int width, int height, int bitrate, File outputFile) {
//            this.mWidth = width;
//            this.mHeight = height;
//            this.mBitrate = bitrate;
//            this.mOutputFile = outputFile;
//        }

    }


    private final static int MSG_START_RECORDING       = 0;
    private final static int MSG_STOP_RECORDING        = 1;
    private final static int MSG_FRAME_AVAILABLE       = 2;
    private final static int MSG_SET_TEXTURE_ID        = 3;
    private final static int MSG_UPDATE_SHARED_CONTEXT = 4;
    private final static int MSG_QUIT_RECORDING        = 5;

    private static class RecorderHandler  extends Handler {
        private final WeakReference<CodecController> mWeakRecorder;

        public RecorderHandler(CodecController manager) {
            this.mWeakRecorder = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Object obj = msg.obj;
            CodecController recorder = mWeakRecorder.get();
            if (recorder == null) {
                return;
            }

            switch (msg.what) {
                case MSG_START_RECORDING:
                    recorder.handlerStartRecording((RecorderConfig)obj);
                    break;
                case MSG_SET_TEXTURE_ID:
                    recorder.handleSetTexture(msg.arg1);
                    break;
                case MSG_FRAME_AVAILABLE:
                    long timestamp = (((long) msg.arg1) << 32) |
                            (((long) msg.arg2) & 0xffffffffL);
                    recorder.handleFrameAvailable((float[]) obj, timestamp);
                    break;
                case MSG_STOP_RECORDING:
                    recorder.handlerStopRecording();
                    break;
                case MSG_QUIT_RECORDING:
                    Looper.myLooper().quit();
                    break;
            }
        }
    }

    private BaseFilter getBaseFilter(BeautyFilterType type){
        BaseFilter result = null;
        if(type == BeautyFilterType.BLUR){
            result = new CameraBlurFilter(mContext);
        }else if(type == BeautyFilterType.COLOR_INVERT){
            result = new CameraColorInvertFilter(mContext);
        }
        return result;
    }
}
