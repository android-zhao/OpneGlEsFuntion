package com.opengldemo.activity;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.opengldemo.R;
import com.opengldemo.filter.FboOnePicFilter;
import com.opengldemo.render.FboRender;

import java.nio.ByteBuffer;

public class FboActivity extends Activity implements FboOnePicFilter.OnDataDrawWithFilter
        /*, FboRender.OnDataDrawByBuffer */{

    GLSurfaceView mFboGlSurface;
    private Handler mHandler;
    Handler mainHandler;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fbo);
        initEnvirment();
        FboRender fbrender = new FboRender(this,this);
        mFboGlSurface.setRenderer(fbrender);
        mFboGlSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        HandlerThread handlerThread = new HandlerThread("fboThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("FboActivity", "glsurview enable render");
                mFboGlSurface.requestRender();
            }
        },5000);


        mainHandler = new Handler(getMainLooper());
//        mFboGlSurface.requestRender();

    }


    private void initEnvirment() {
        mFboGlSurface = findViewById(R.id.fbo_glsurfaceview);
        mImageView = findViewById(R.id.display_fboBuffer);

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
            Log.i("FboActivity"," FboActivity supportsGLES3");
            mFboGlSurface.setEGLContextClientVersion(2);
        }else {
            Toast.makeText(this , "This device does not support OpenGL ES 3.0." ,
                    Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void onData(ByteBuffer buffer) {

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
//                handle512Bitmap(buffer);
//                handle4KBitmap(buffer);
                handle1080Bitmap(buffer);
            }
        });
    }

    private void handle512Bitmap(ByteBuffer buffer) {
        // 图片资源是 BitmapFactory.decodeStream(context.getAssets().open("lena.jpg"));
        Bitmap bitmap=Bitmap.createBitmap(512,512, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        Toast.makeText(FboActivity.this, "fbo 处理成功", Toast.LENGTH_SHORT).show();
        mImageView.setImageBitmap(bitmap);
    }

    private void handle4KBitmap(ByteBuffer buffer) {
        // 图片资源是 BitmapFactory.decodeStream(context.getAssets().open("lena.jpg"));
        Bitmap bitmap=Bitmap.createBitmap(3840,2160, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        Toast.makeText(FboActivity.this, "fbo 处理成功", Toast.LENGTH_SHORT).show();
        mImageView.setImageBitmap(bitmap);
    }

    private void handle1080Bitmap(ByteBuffer buffer) {
        // 图片资源是 BitmapFactory.decodeStream(context.getAssets().open("lena.jpg"));
        Bitmap bitmap=Bitmap.createBitmap(1920,1080, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        Toast.makeText(FboActivity.this, "fbo 处理成功", Toast.LENGTH_SHORT).show();
        mImageView.setImageBitmap(bitmap);
    }





//    @Override
//    public void onDataFromRender(final Bitmap bitmap) {
//        mainHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(FboActivity.this, "fbo 处理成功", Toast.LENGTH_SHORT).show();
//                mImageView.setImageBitmap(bitmap);
//            }
//        });
//
//    }


//    @Override
//    public void onDataFromRender(ByteBuffer buffer,int width,int height) {
//        mainHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(FboActivity.this, "fbo 处理成功", Toast.LENGTH_SHORT).show();
//                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//                Log.i("zhf_opengl","createBitmap end");
//                // 从缓存区读二进制缓冲数据
//                bitmap.copyPixelsFromBuffer(buffer);
//                mImageView.setImageBitmap(bitmap);
//            }
//        });
//    }
}