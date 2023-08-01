package com.opengldemo.codec;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AVRecorderCore {

    private static final String TAG = "AVRecorderCore";
    MediaCodec mCodec ;
    MediaCodec.BufferInfo mBufferInfo ;
    private final static String MIME_TYPE = "video/avc";
    private final static int FRAME_RATE = 30;
    private final static int IFRAME_INTERVAL = 30;
    private Surface mInputSurface = null;
    private Object objLock = new Object();
    private MediaMuxer mMuxer;
    private int trackerID = 0;
    private  boolean isStartMuxer = false;
    public AVRecorderCore(Context context) {
//            initCodec();
    }
    private Context mContext;
    public AVRecorderCore(Context context,int width, int height, int bitrate, File outputFile) {
        mContext = context;
        initCodec(width,height,bitrate,outputFile.getAbsolutePath(),null);
    }
    public void initCodec(int width, int height, int bitrate, String filePath, Surface surface){
        Log.i(TAG,"VideoRecorderCore begin");
        mBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        try {
            mCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        } catch (IOException e) {

            e.printStackTrace();
        }

        mCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mCodec.createInputSurface();
        mCodec.start();

        try {
            mMuxer = new MediaMuxer(filePath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        trackerID = -1;
        isStartMuxer = false;
        Log.i(TAG,"VideoRecorderCore end");
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void setTextureId(int textureId ){

    }
    public void frameAvailable(SurfaceTexture surfaceTexture){

    }

    private final static int TIMEOUT_USEC = 20000;
    public void drainEncoder(boolean endOfStream) {
        if (endOfStream) {
            mCodec.signalEndOfInputStream();
        }

        ByteBuffer[] outputBuffers = mCodec.getOutputBuffers();
        while (true) {


            int encodeStatus = mCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            Log.d(TAG,"drainEncoder encodeStatus: " +encodeStatus);
            if (encodeStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;
                }
            }else if (encodeStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mCodec.getOutputBuffers();
            }else if (encodeStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (isStartMuxer) {
                    throw new RuntimeException("format has changed!");
                }
                MediaFormat newFormat = mCodec.getOutputFormat();
                trackerID = mMuxer.addTrack(newFormat);
                mMuxer.start();
                isStartMuxer = true;
            }else if (encodeStatus < 0) {
                Log.e(TAG, "error encodeStatus=" + encodeStatus);
            }else {
                ByteBuffer data = outputBuffers[encodeStatus];
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mBufferInfo.size = 0;
                }
                if (mBufferInfo.size != 0) {
                    if (!isStartMuxer) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    data.position(mBufferInfo.offset);
                    data.limit(mBufferInfo.offset + mBufferInfo.size);
                    mMuxer.writeSampleData(trackerID, data, mBufferInfo);
                }
                mCodec.releaseOutputBuffer(encodeStatus, false);
                Log.i(TAG,"drainEncoder mBufferInfo.flags :" +mBufferInfo.flags +
                        " MediaCodec.BUFFER_FLAG_END_OF_STREAM:" + MediaCodec.BUFFER_FLAG_END_OF_STREAM +
                        ",result :" +(mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM));
                // end of stream
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
        }
    }

    public void release() {
        if (mCodec != null) {
            mCodec.stop();
            mCodec.release();
            mCodec = null;
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }
}
