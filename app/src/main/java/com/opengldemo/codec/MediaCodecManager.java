package com.opengldemo.codec;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

//外部模块使用MediaCodec进行解码视频使用
public class MediaCodecManager {

    private static String TAG = "mediaCodecManager";

    private Context mContext;
    private DecodeVideoThread mVideoDecode;//视频解码线程
    private DecodeVideoThread.IDecodeStatusListener decodeStatusListener;//解码状态显示线程
    private MediaCodecHandler mCodecHandler;//处理消息回调以及线程切换
    private CodecParams codecParams;//解码的参数
    private Surface videoPlaySurface;//在哪展示

    private static  MediaCodecManager mCodecManager;


    public static MediaCodecManager getInstance(Context context){
        if(mCodecManager == null){
            mCodecManager = new MediaCodecManager(context);
        }
        return mCodecManager;
    }

    private MediaCodecManager(Context context){
        this.mContext = context;
        decodeStatusListener = new DecodeVideoThread.IDecodeStatusListener() {
            @Override
            public void onStatus(int status) {
                Log.i(TAG,"onStatus --->" +status);
            }
        };

        HandlerThread handlerThread = new HandlerThread("codec_manager");
        handlerThread.start();
        mCodecHandler = new MediaCodecHandler(handlerThread.getLooper());
        Log.i(TAG,"MediaCodecManager constructor end");
    }



    public void setMediaParams(CodecParams codecParams){
        Log.i(TAG,"setMediaParams -->" + codecParams.toString());
        String videoPath = codecParams.getVideoPath();
        mVideoDecode = new DecodeVideoThread("decode",videoPath,decodeStatusListener);
    }

    public void setPreviewSurface(Surface surface){
        mVideoDecode.setDecodeOutPut(surface);
    }

    public void startPlay(){
        mVideoDecode.start();
    }

    private static class MediaCodecHandler extends Handler{

        public MediaCodecHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }

    }



}
