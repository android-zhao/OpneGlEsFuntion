package com.opengldemo.activity;

import static android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY;
import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.opengldemo.R;
import com.opengldemo.render.PreviewAndPlayVideoRender;

import java.util.Arrays;

public class PreviewAndVideoActivity extends Activity {

    private static String TAG = "PreviewAndVideoActivity";
    GLSurfaceView mGlPreview = null;
    PreviewAndPlayVideoRender mRender = null;
    Handler mHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_and_video);
        init();
    }


    private void init() {
        //1:使用camera2 启动预览，并且将glsurface 设置给camera2，将相机输出的预览流输出到纹理上 完成

        //2:初始化编解码，将MediaCodec启动，启动之后就开始解码视频，解码出来的视频展示也是纹理，此纹理时glsurface上的纹理

        //3：将glsurfaceview的render设置成启动预览之后就开始播放视频

        mGlPreview = findViewById(R.id.glsurface_preview);
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        openCamera();
                        break;
                }
            }
        };
        mGlPreview.setEGLContextClientVersion(2);
        mRender = new PreviewAndPlayVideoRender(this, mHandler);
        mGlPreview.setRenderer(mRender);
        mGlPreview.setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    private CameraDevice mCamera = null;

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIdList = new String[0];
        try {
            cameraIdList = cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "openCamera" + Arrays.toString(cameraIdList));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        try {
            cameraManager.openCamera(String.valueOf(cameraIdList[0]), new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.i(TAG,"onOpened");
                    mCamera = camera;
                    startPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.i(TAG,"onDisconnected");
                    mCamera = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.i(TAG,"onError:" +error);
                    mCamera= null;
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private CameraCaptureSession mCurrentSession ;
    private void startPreview()  {
//        int cameraTextureId = mRender.getCameraTextureId();
        SurfaceTexture surfaceTexture = mRender.getTexture();

        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                Log.i(TAG,"onFrameAvailable");
                mGlPreview.requestRender();
            }
        });

        Surface mSurface = new Surface(surfaceTexture);

        try {
            mCamera.createCaptureSession(Arrays.asList(mSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCurrentSession = session;
                    CaptureRequest.Builder captureRequest = null;
                    try {
                        captureRequest = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        captureRequest.addTarget(mSurface);
                        mCurrentSession.setRepeatingRequest(captureRequest.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                                super.onCaptureFailed(session, request, failure);
                            }
                        },mHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            },mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}