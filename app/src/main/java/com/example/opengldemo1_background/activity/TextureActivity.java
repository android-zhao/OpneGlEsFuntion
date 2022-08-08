package com.example.opengldemo1_background.activity;



import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.opengldemo1_background.R;
import com.example.opengldemo1_background.render.TextureRender;

public class TextureActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);
        
        init();
    }

    GLSurfaceView mGlSurface;
    private void init() {
        mGlSurface = (GLSurfaceView) findViewById(R.id.glsurface_texture);
        mGlSurface.setEGLContextClientVersion(2);

        TextureRender textureRender = new TextureRender(this);
        mGlSurface.setRenderer(textureRender);

        mGlSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}