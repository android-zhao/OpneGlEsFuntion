package com.opengldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.opengldemo.activity.AnimationActivity;
import com.opengldemo.activity.CameraTestActivity;
import com.opengldemo.activity.FboActivity;
import com.opengldemo.activity.GlsurfaceActivity;
import com.opengldemo.activity.MVPTestActivity;
import com.opengldemo.activity.NativeEglActivity;
import com.opengldemo.activity.OpenGlTransitionActivity;
import com.opengldemo.activity.PreviewAndVideoActivity;
import com.opengldemo.activity.RecordActivity;
import com.opengldemo.activity.ShaderToyActivity;
import com.opengldemo.activity.VideoPlaybyOpenGLActivity;
import com.opengldemo.activity.WaterMarkActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        Button mglButton = findViewById(R.id.glsurface_activity);
        mglButton.setOnClickListener(this);
        Button cameraButton = findViewById(R.id.camera_test);
        cameraButton.setOnClickListener(this::onClick);
        Button mvptest = findViewById(R.id.mvptest);
        mvptest.setOnClickListener(this::onClick);
        Button waterButn = findViewById(R.id.glsurface_watermarkactivity);
        waterButn.setOnClickListener(this::onClick);

//        Button animationActivity = findViewById(R.id.glsurface_animation_activity);
//        waterButn.setOnClickListener(this::onClick);
        Button fboActivity = findViewById(R.id.glsurface_fboactivity);
        fboActivity.setOnClickListener(this::onClick);

        Button codecActivity = findViewById(R.id.glsurface_codec);
        codecActivity.setOnClickListener(this::onClick);

        Button nativeEgl = findViewById(R.id.glsurface_codec_nativeegl);
        nativeEgl.setOnClickListener(this::onClick);

        Button transition = findViewById(R.id.glsurface_opengl_transition);
        transition.setOnClickListener(this::onClick);

        Button shadertoy = findViewById(R.id.glsurface_shader);
        shadertoy.setOnClickListener(this::onClick);

        Button previewAndVideo = findViewById(R.id.glsurface_preview_video);
        previewAndVideo.setOnClickListener(this::onClick);

        Button playVideoByOpenGl = findViewById(R.id.glsurface_playvideo);
        playVideoByOpenGl.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.glsurface_activity:
                intent = new Intent(this, GlsurfaceActivity.class);
                break;
            case R.id.camera_test:
                intent = new Intent(this, CameraTestActivity.class);
                break;
            case R.id.mvptest:
                intent = new Intent(this, MVPTestActivity.class);
                break;
            case R.id.glsurface_watermarkactivity:
                intent = new Intent(this, WaterMarkActivity.class);
                break;
//            case R.id.glsurface_animation_activity:
//                intent = new Intent(this, AnimationActivity.class);
//                break;
            case R.id.glsurface_fboactivity:
                intent = new Intent(this, FboActivity.class);
                break;
            case  R.id.glsurface_codec:
                intent = new Intent(this, RecordActivity.class);
                break;
            case R.id.glsurface_codec_nativeegl:
                intent = new Intent(this, NativeEglActivity.class);
                break;
            case R.id.glsurface_opengl_transition:
                intent = new Intent(this, OpenGlTransitionActivity.class);
                break;
            case R.id.glsurface_shader:
                intent = new Intent(this, ShaderToyActivity.class);
                break;
            case R.id.glsurface_preview_video:
                intent = new Intent(this, PreviewAndVideoActivity.class);
                break;
            case R.id.glsurface_playvideo:
                intent = new Intent(this, VideoPlaybyOpenGLActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private boolean requestPermission() {
        return Utils.checkPermissionsAndRequest(this
                , Utils.CAMERA, 1000, "请求相机权限被拒绝")
                && Utils.checkPermissionsAndRequest(this,
                Utils.STORAGE, 1000, "请求访问SD卡权限被拒绝");
    }
}