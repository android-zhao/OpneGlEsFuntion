package com.opengldemo.activity;


import static com.opengldemo.factory.BeautyFilterType.WEAK_PIXEL_INCLUSION;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.Surface;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.opengldemo.R;
import com.opengldemo.factory.BeautyFilterType;
import com.opengldemo.render.CodecRender;

import java.io.File;

public class RecordActivity extends Activity {

    private static final String TAG = "RecordActivity";
    public static final  int START_OPEN_CAMERA = 0;
    GLSurfaceView surfaceView;
    Surface previewSurface;
    CodecRender codecRender;
    Handler mMainHandler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        Log.i(TAG,"onCreate");
        initView();
//        initCodec();
    }
    private static  int bitrate = 6 * 1024 * 1024;
    private float mBp = 0.3f;
    private String outputFiles = "";

    private String getCacheFilePath(){
        File file = new File(getCacheDir(),"/test.mp4");
        outputFiles = file.getAbsolutePath();
        Log.i(TAG,"getCacheFilePath -->"+outputFiles);
        return outputFiles;
    }

//    private BeautyFilterType[] mFilters = new BeautyFilterType[]{BeautyFilterType.NONE,BeautyFilterType.BLUR,
//            BeautyFilterType.COLOR_INVERT,BeautyFilterType.HUE,BeautyFilterType.WHITE_BALANCE,BeautyFilterType.SKETCH };

    private BeautyFilterType[] mFilters = new BeautyFilterType[]{BeautyFilterType.NONE,BeautyFilterType.BLUR,
            BeautyFilterType.COLOR_INVERT };
    private int mCurrentIndex = 0;
    private void initView() {
        surfaceView = findViewById(R.id.preview_glsv);

        mMainHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case START_OPEN_CAMERA:
//                        initCamera();
                        break;
                }
            }
        };

        Button startRecord = findViewById(R.id.start_record);
        Button stopRecord = findViewById(R.id.stop_record);
        Button aloneFilter = findViewById(R.id.change_filter_alone);


        codecRender = new CodecRender(this,mMainHandler,surfaceView);
        Button changeFilter = findViewById(R.id.change_filter);
        changeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(codecRender == null){
                    Log.i(TAG,"changeFilter onclick ,codecRender is null ");
                    return;
                }
                if(mCurrentIndex == mFilters.length -1){
                    mCurrentIndex = 0;
                }
                mCurrentIndex++;
                codecRender.setFilter(mFilters[mCurrentIndex]);
            }
        });
        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(codecRender == null){
                    Log.i(TAG,"startRecord onclick ,codecRender is null ");
                    return;

                }
                codecRender.setRecord(true);
            }
        });


        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(codecRender == null){
                    Log.i(TAG,"startRecord onclick ,codecRender is null ");
                    return;

                }
                codecRender.setRecord(false);
            }
        });
        aloneFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(codecRender == null){
                    Log.i(TAG,"startRecord onclick ,codecRender is null ");
                    return;
                }
                codecRender.setFilter(WEAK_PIXEL_INCLUSION);
            }
        });
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
            Log.i(TAG," GlsurfaceActivity with SimpleRender ");
            surfaceView.setEGLContextClientVersion(2);
        }else {
            Toast.makeText(this , "This device does not support OpenGL ES 3.0." ,
                    Toast.LENGTH_SHORT).show();
            return;
        }
    }

}