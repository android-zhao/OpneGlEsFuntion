package com.opengldemo.activity;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.opengldemo.R;
import com.opengldemo.render.PlayVideoByOpenglRender;

public class VideoPlaybyOpenGLActivity extends Activity {

    GLSurfaceView mPlayVideoView;
    PlayVideoByOpenglRender mRender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playby_open_glactivity);
        //1:本地播放视频(MediaPlayer/MediaCodec)
        //2:通过glsurface 进行播放
        //3:通过opengl对播放出来的视频进行特效处理

        initView();
    }

    private void initView() {
        mPlayVideoView = findViewById(R.id.play_video);
        mPlayVideoView.setEGLContextClientVersion(3);
        mRender = new PlayVideoByOpenglRender(this);
        mPlayVideoView.setRenderer(mRender);
//        mPlayVideoView.
        mPlayVideoView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


}