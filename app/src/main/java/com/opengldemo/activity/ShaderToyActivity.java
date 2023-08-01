package com.opengldemo.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.opengldemo.R;
import com.opengldemo.render.ShaderToyRender;

public class ShaderToyActivity extends Activity {

    GLSurfaceView mGlSurfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shader_toy);
        initView();
    }

    void initView(){
        mGlSurfaceView =  findViewById(R.id.shader_gl);

        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsGLES3 = configurationInfo.reqGlEsVersion >= 0x30000
                ||(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                &&(Build.FINGERPRINT.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("unknow")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86");

        if (supportsGLES3){
            Log.i("SimpleRender"," GlsurfaceActivity with SimpleRender ");
            mGlSurfaceView.setEGLContextClientVersion(3);
        }else {
            Toast.makeText(this , "This device does not support OpenGL ES 3.0." ,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ShaderToyRender render = new ShaderToyRender(this);
        mGlSurfaceView.setRenderer(render);
    }
}