package com.opengldemo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.opengldemo.R;
import com.opengldemo.render.MvpTestRender;
import com.opengldemo.render.SimpleRender;

public class MVPTestActivity extends AppCompatActivity implements View.OnClickListener {

    GLSurfaceView mGLSurfaceView;
    Button mScaleBtn,mTranslateBtn,mRotateBtn;
    MvpTestRender mvpTestRender = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m_v_p_test);
        mGLSurfaceView = findViewById(R.id.glsurview);
        initEnvirment();
        initView();
    }

    private void initView() {
        mScaleBtn = findViewById(R.id.scale_btn);
        mScaleBtn.setOnClickListener(this::onClick);
        mTranslateBtn = findViewById(R.id.tranlate_btn);
        mTranslateBtn.setOnClickListener(this::onClick);
        mRotateBtn = findViewById(R.id.rotate_btn);
        mRotateBtn.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.scale_btn:
                mvpTestRender.setType(MvpTestRender.SCALE_TYPE);
                break;
            case R.id.tranlate_btn:
                mvpTestRender.setType(MvpTestRender.TRANSLATE_TYPE);
                break;
            case R.id.rotate_btn:
                mvpTestRender.setType(MvpTestRender.ROTATE_TYPE);
                break;
        }
    }

    private void initEnvirment() {
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
            mGLSurfaceView.setEGLContextClientVersion(2);
            mvpTestRender = new MvpTestRender(this);
            mGLSurfaceView.setRenderer(mvpTestRender);
        }else {
            Toast.makeText(this , "This device does not support OpenGL ES 3.0." ,
                    Toast.LENGTH_SHORT).show();
            return;
        }
    }
}