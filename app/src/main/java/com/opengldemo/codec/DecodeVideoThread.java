package com.opengldemo.codec;


import android.graphics.Rect;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;
import androidx.annotation.NonNull;

import com.opengldemo.bean.MediaData;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DecodeVideoThread extends Thread  implements MediaPlayer.OnVideoSizeChangedListener {

    private static String TAG = "decodeThread";

    private String mVideoPath = "";
    private MediaCodec mMediaCodec = null;//编解码
    private MediaExtractor mMediaExtractor = null;//解视频
    private IDecodeStatusListener mDecodeStatusListener = null;//解码对外的接口回调
    private IDecodeDataListener mDecodeDataListener = null;//对外需要使用的是byte数据流接口
    private Surface mPreviewSurface = null;

    private int mVideoTrackIndex = -1;//视频轨道
    private MediaFormat mVideoFormat;//视频参数
    private int mWidth = -1;//视频宽
    private int mHeight = -1;//视频高
    private List<MediaData> mYuvBuffer = new ArrayList<>();

    private int mMediaPlayCallbackWidth = -1;
    private int mMediaPlayCallbackHeight = -1;
    private MediaPlayer mMediaPlayer;

//    public DecodeVideoThread(MediaCodec mMediaCodec, MediaExtractor mMediaExtractor) {
//        this.mMediaCodec = mMediaCodec;
//        this.mMediaExtractor = mMediaExtractor;
//    }


    //外部传入保证listener不为空
    public DecodeVideoThread(@NonNull String name,String videoPath,IDecodeStatusListener listener ) {
        super(name);
        if(videoPath == null) throw new RuntimeException("解码视频路劲不能为空");
        mVideoPath = videoPath;

        if(listener == null) {
            throw new RuntimeException("解码状态回调不能为空");
        }
        mDecodeStatusListener = listener;
        init();
    }

    public void setDecodeOutPut(Surface surface){
        this.mPreviewSurface = surface;
    }

    private final int YUV_BUFFER_COUNT = 19;
    private  int mColorFormat = -1;
    public void setDecodeOutPut(IDecodeDataListener listener,int type){
        if(listener == null){
            throw new RuntimeException("数据回调接口不正确");
        }
        mColorFormat = type;
        if( isColorFormatInValid(type) ){
            throw new RuntimeException("数据格式设置不正确");
        }
        //承载解码完成之后的数据载体对象
        MediaData mediaData;
        byte yuvData[] ;
        for (int i = 0;i<YUV_BUFFER_COUNT;i++){
            yuvData = new byte[mWidth *mHeight * 3 /2];
            mediaData = new MediaData(yuvData,mYuvBuffer);
            mediaData.setFileType(mColorFormat);
//            mediaData.setMediaDataLength(mWidth *mHeight * 3 /2);
            mediaData.setWidth(mWidth);
            mediaData.setHeight(mHeight);
            mYuvBuffer.add(mediaData);
        }
        Log.i(TAG,"setDecodeOutPut end");
    }

    /**
     *
     * @param type
     * @return true 无效 false 有效
     */
    private boolean isColorFormatInValid(int type ){
        if(type != CodecConstant.COLOR_FORMAT_I420 &&
                type!= CodecConstant.COLOR_FORMAT_NV12 &&
                type != CodecConstant.COLOR_FORMAT_NV21){
            return true;
        }
        return false;
    }

    private void init() {
        initExtractor();
        initMediaCodec();
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mVideoPath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG,"initMediaPlayer happen exception");
            mDecodeStatusListener.onStatus(CodecConstant.DECODE_VIDEO_PATH_NULL);
        }

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setOnVideoSizeChangedListener(this);

        Log.i(TAG,"initMediaPlayer end");
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.e(TAG,
                "线程名: " + Thread.currentThread().getName() +
                        "-----onVideoSizeChanged: (" + width + " , " + height + ")"
        );
        mMediaPlayCallbackWidth = width;
        mMediaPlayCallbackHeight = height;
    }

    private void initMediaCodec() {
        mVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        try {
            mMediaCodec = MediaCodec.createDecoderByType(mVideoFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"initMediaCodec -->mVideoFormat:" +mVideoFormat);
    }

    private void initExtractor(){
        Log.i(TAG, "initExtractor begin ");

        mMediaExtractor = new MediaExtractor();
        String videoPath = mVideoPath;
        // 指定视频位置
        try {
            mMediaExtractor.setDataSource(videoPath);
        } catch (IOException e) {
            Log.e(TAG, "initExtractor setDataSource exception: " ,e);
            e.printStackTrace();
            mDecodeStatusListener.onStatus(CodecConstant.DECODE_VIDEO_PATH_NULL);
            return;
        }

        Log.i(TAG, "initExtractor getTrackCount: " + mMediaExtractor.getTrackCount());
        // 获取track id 即 音频轨 和视频轨
        for(int i = 0; i < mMediaExtractor.getTrackCount(); i++){
            Log.i(TAG,"initCodec :for loop " +i + " begin");
            MediaFormat format = mMediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            // 获取时间是微秒
            long during = format.getLong(MediaFormat.KEY_DURATION);

            Log.i(TAG,"initCodec :mime :" +mime + ",during:" +during);

            //只解码 视频帧
            if (mime.startsWith("video")) {
                mVideoTrackIndex = i;
                mVideoFormat = format;
                mMediaExtractor.selectTrack(i);
                break;
            }
        }

        mWidth = mVideoFormat.getInteger(MediaFormat.KEY_WIDTH);
        mHeight = mVideoFormat.getInteger(MediaFormat.KEY_HEIGHT);
        Log.i(TAG,"initExtractor  , mWidth -->" +mWidth + ",mHeight -->" +mHeight );
    }


    @Override
    public void run() {
        super.run();
        if(mPreviewSurface == null && mDecodeDataListener == null){
            Log.i(TAG,"无输出对象");
            return;
        }


        //设计实现 如果存在surface 显示就 优先显示到surface上面
        // 如果没有设置 显示的surface 才考虑将解码的yuv数据传递出去
        //如果 同时设置了surface 和listener 则只显示surface 不会在对数据进行解析显示

        if(mPreviewSurface != null){
            decodeVideo2Surface();
            return;
        }

        if(mDecodeDataListener != null){
            decodeVideo2Listener();
        }

    }

    private void decodeVideo2Surface(){
        startDecode2Surface();
    }

    private void startDecode2Surface() {
        mMediaPlayer.setSurface(mPreviewSurface);
        try {
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException t) {
            Log.e("Prepare ERROR", "onSurfaceCreated: ");
        }
    }

    private void decodeVideo2Listener(){
        decodeVideo();
    }


    private static final int TIMEOUT_USEC = 2500;
    private int mAllFrame = 0;
    private void decodeVideo(){
        mMediaCodec.configure(mVideoFormat, mPreviewSurface, null, 0);
        mMediaCodec.start();
        //开始解码
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        boolean decoderDone = false;
        boolean inputDone = false;
        long videoStartTimeUs = -1;
        int decodeTryAgainCount = 0;

        boolean isNeedSaveOneFrame = true ;
        while (!decoderDone) {
            //还有帧数据，输入解码器
            if (!inputDone) {
                boolean eof = false;
                int index = mMediaExtractor.getSampleTrackIndex();
                if (index == mVideoTrackIndex) {
                    int inputBufIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufIndex >= 0) {
                        ByteBuffer inputBuf = mMediaCodec.getInputBuffer(inputBufIndex);
                        int chunkSize = mMediaExtractor.readSampleData(inputBuf, 0);
                        if (chunkSize < 0) {
                            mMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            decoderDone = true;
                        } else {
                            mAllFrame++;
                            long sampleTime = mMediaExtractor.getSampleTime();
                            mMediaCodec.queueInputBuffer(inputBufIndex, 0, chunkSize, sampleTime,
                                    0);
                            mMediaExtractor.advance();
                        }
                    }
                } else if (index == -1) {
                    Log.i(TAG,
                            "doVideoDecodeWithObjectPool dequeueInputBuffer index = -1,and eof set true");
                    eof = true;
                }

                if (eof) {
                    //解码输入结束
                    int inputBufIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufIndex >= 0) {
                        mMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                    }
                }
            }
            boolean decoderOutputAvailable = !decoderDone;
            if (decoderDone) {
                Log.i(TAG, "decoderOutputAvailable:" + decoderOutputAvailable);
            }

            while (decoderOutputAvailable) {
                int outputBufferIndex =
                        mMediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                Log.i(TAG, "outputBufferIndex = " + outputBufferIndex);

                if (inputDone && outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    decodeTryAgainCount++;
                    if (decodeTryAgainCount > 10) {
                        Log.i(TAG, "INFO_TRY_AGAIN_LATER 10 times,force End!");
                        decoderDone = true;
                        break;
                    }
                } else {
                    decodeTryAgainCount = 0;
                }
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break;
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = mMediaCodec.getOutputFormat();
                    Log.i(TAG, "decode newFormat = " + newFormat);
                } else if (outputBufferIndex < 0) {
                    // ignore
                    Log.i(TAG, "unexpected result from decoder.dequeueOutputBuffer: " +
                            outputBufferIndex,null);
                } else {
                    boolean doRender = true;
                    Log.i(TAG,"bufferInfo.presentationTimeUs:" +bufferInfo.presentationTimeUs);

                    if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                        decoderDone = true;
                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        Log.i(TAG, "decoderDone");
                        break;
                    }

                    // 2.获取到解码后的Image对象，yuv数据就包含在这里面
                    Image image = mMediaCodec.getOutputImage(outputBufferIndex);

                    MediaData dataFromImageWithObjectPool = null;
                    if(mColorFormat == CodecConstant.COLOR_FORMAT_I420){
                        dataFromImageWithObjectPool = changeImage2I420(image);
                    }else {
                        dataFromImageWithObjectPool = getDataFromImageWithObjectPool(image, mColorFormat, mWidth, mHeight);//自定义的方法，后面介绍
                    }

//                    if(isNeedSaveOneFrame){
//                        String filename = mColorFormat+"_" +mWidth+"_"+mHeight+".yuv";
//
//                        File file = new File(AVConstant.FILE_PRE+filename);
//                        String absolutePath = file.getAbsolutePath();
//                        VideoUtils.dumpFile(absolutePath,dataFromImageWithObjectPool.getMediaData());
//                        isNeedSaveOneFrame = false;
//                    }

                    if(dataFromImageWithObjectPool == null){
                        Log.i(TAG,"dataFromImageWithObjectPool IS NULL ");
                        image.close();
                        continue;
                    }

                    if(mDecodeDataListener != null ){
                        mDecodeDataListener.onDecodeData(dataFromImageWithObjectPool.getMediaData());
                        image.close();
                    }else {
                       Log.i(TAG,"mYuvDataListener is null");
                        image.close();
                        continue;
                    }

                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, doRender);
                }
            }
        }
        Log.i(TAG,"DECODE END :" + mAllFrame);

    }

    private long frameId  = 0;
    private MediaData getDataFromImageWithObjectPool(Image image, int colorFormat, int width, int height) {
        MediaData mediaData;
        frameId ++;
        if(mYuvBuffer.size() >0){
            mediaData = mYuvBuffer.remove(0);
            mediaData.setFrameId(frameId);
        } else {
           Log.i(TAG,"mYuvBuffer size is 0,so drop " +frameId +"frame");
            if(image != null)image.close();
            return null;
        }

        Rect crop = image.getCropRect();
        Log.i(TAG, "crop width: " + crop.width() + ", height: " + crop.height());
        Image.Plane[] planes = image.getPlanes();
        byte[] rowData = new byte[planes[0].getRowStride()];

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat ==  CodecConstant.COLOR_FORMAT_I420  ) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == CodecConstant.COLOR_FORMAT_NV21 ) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    } else if (colorFormat == CodecConstant.COLOR_FORMAT_NV12) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == CodecConstant.COLOR_FORMAT_I420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == CodecConstant.COLOR_FORMAT_NV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    } else if (colorFormat == CodecConstant.COLOR_FORMAT_NV12) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                default:
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(mediaData.getMediaData(), channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        mediaData.getMediaData()[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return mediaData;
    }

    private MediaData changeImage2I420(Image image){
        MediaData mediaData;
        frameId ++;
        if(mYuvBuffer.size() >0){
            mediaData = mYuvBuffer.remove(0);
            mediaData.setFrameId(frameId);
        } else {
           Log.i(TAG,"mYuvBuffer size is 0,so drop " +frameId +"frame");
            image.close();
            return null;
        }
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();
            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();
            byte[] data = new byte[ySize + uSize + vSize];
            yBuffer.get(data, 0, ySize);
            vBuffer.get(data, ySize, vSize);
            uBuffer.get(data, ySize + vSize, uSize);
            byte[] i420Data = mediaData.getMediaData();
            // Convert NV21 to I420
            System.arraycopy(data, 0, i420Data, 0, ySize);
            for (int i = 0; i < uSize; i++) {
                i420Data[ySize + i * 2] = data[ySize + i];
                i420Data[ySize + i * 2 + 1] = data[ySize + vSize + i];
            }
            // Process i420Data
//         image.close();
            return mediaData;
        }else {
            Log.i(TAG,"changeImage2I420 but image is null");
            return null;
        }

    }

    public class DecodeParams {

    }

    public interface IDecodeStatusListener{
        void onStatus(int status);
    }

    public interface IDecodeDataListener{
        void onDecodeData(byte[] data);
    }


}
