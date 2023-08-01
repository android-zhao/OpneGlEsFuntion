package com.opengldemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;


import com.opengldemo.R;
import com.opengldemo.bean.CameraInfo;
import com.opengldemo.render.CameraRender;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

public class CameraTestActivity extends AppCompatActivity {

    GLSurfaceView mGlSurface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        initEnvirment();
        initView();
    }

    private void initEnvirment() {
        mGlSurface = findViewById(R.id.glsurface);

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
            mGlSurface.setEGLContextClientVersion(2);
        }else {
            Toast.makeText(this , "This device does not support OpenGL ES 3.0." ,
                    Toast.LENGTH_SHORT).show();
            return;
        }

    }
    SurfaceTexture mSurfaceTexture = null;
    protected int imageWidth, imageHeight;//图像宽高
    private void openCamera()   {
        if (mPreviewSize != null ) {
            if (mCamera == null) {
                mCamera = Camera.open(mCameraID);
                if (mCamera == null) {
                    throw new RuntimeException("Unable to open camera");
                }
                Log.e(TAG, "openCamera" );

                CameraInfo info = getCameraInfo();
                if(info.orientation == 90 || info.orientation == 270){
                    imageWidth = info.previewHeight;
                    imageHeight = info.previewWidth;
                }else{
                    imageWidth = info.previewWidth;
                    imageHeight = info.previewHeight;
                }

                cameraRender.adjustSize(imageWidth,imageHeight,info.orientation, info.isFront, true);
                if(cameraRender == null){
                    Log.e(TAG, "openCamera,cameraRender is null " );
                    return;
                }
                int cameraTexture = cameraRender.getmTextureId() ;
                mSurfaceTexture = new SurfaceTexture(cameraTexture);
                cameraRender.setSurfaceTexture(mSurfaceTexture);
                mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        Log.i(TAG,"onFrameAvailable");

                        mGlSurface.requestRender();
                    }
                });
                try {
                    mCamera.setPreviewTexture(mSurfaceTexture);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
           }
    }


    public  CameraInfo getCameraInfo(){
        CameraInfo info = new CameraInfo();
        Camera.Size size = getPreviewSize();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);
        info.previewWidth = size.width;
        info.previewHeight = size.height;

        Log.i(TAG, "getCameraInfo.width:" + size.width);
        Log.i(TAG, "getCameraInfo.height:" + size.height);

        info.orientation = cameraInfo.orientation;
        info.isFront = mCameraID == 1 ? true : false;
        size = getPictureSize();
        info.pictureWidth = size.width;
        info.pictureHeight = size.height;
        Log.i(TAG,"previewWidth:" +info.previewWidth +
                ",previewHeight:"+info.previewHeight + ",orientation:" + info.orientation +
                ",isFront :" +info.isFront + ",pictureWidth：" +info.pictureWidth +
                "，pictureHeight：" +info.pictureHeight);
        return info;
    }

    private  Camera.Size getPreviewSize(){
        return mCamera.getParameters().getPreviewSize();
    }
    private  Camera.Size getPictureSize(){
        return mCamera.getParameters().getPictureSize();
    }



    private Handler mHandler  = null;
    private final int OPEN_CAMERA = 0;
    private CameraRender cameraRender = null;
    private void initView() {


        mHandler = new Handler(getMainLooper()){

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case OPEN_CAMERA:
                        mPreviewSize = cameraRender.getmPreviewSize();
                        openCamera();
//                        startPreview(CameraTestActivity.this,
//                                mPreviewSize.getWidth(),mPreviewSize.getHeight());
                }
            }
        };
        cameraRender = new CameraRender(this,mHandler);

        mGlSurface.setRenderer(cameraRender);
        mGlSurface.setRenderMode(RENDERMODE_WHEN_DIRTY);

    }
    public  int getCameraPreviewOrientation(Activity activity, int cameraId) {
        if (mCamera == null) {
            throw new RuntimeException("mCamera is null");
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        int degrees = getRotation(activity);
        //前置
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        }
        //后置
        else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;

    }

    public static int getRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }



    /**
     * 获取最合适的尺寸
     * @param supportList
     * @param width
     * @param height
     * @return
     */
    private static Camera.Size getOptimalSize(List<Camera.Size> supportList, int width, int height) {
        // camera的宽度是大于高度的，这里要保证expectWidth > expectHeight
        int expectWidth = Math.max(width, height);
        int expectHeight = Math.min(width, height);
        // 根据宽度进行排序
        Collections.sort(supportList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size pre, Camera.Size after) {
                if (pre.width > after.width) {
                    return 1;
                } else if (pre.width < after.width) {
                    return -1;
                }
                return 0;
            }
        });

        Camera.Size result = supportList.get(0);
        boolean widthOrHeight = false; // 判断存在宽或高相等的Size
        // 辗转计算宽高最接近的值
        for (Camera.Size size: supportList) {
            // 如果宽高相等，则直接返回
            if (size.width == expectWidth && size.height == expectHeight) {
                result = size;
                break;
            }
            // 仅仅是宽度相等，计算高度最接近的size
            if (size.width == expectWidth) {
                widthOrHeight = true;
                if (Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
            // 高度相等，则计算宽度最接近的Size
            else if (size.height == expectHeight) {
                widthOrHeight = true;
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)) {
                    result = size;
                }
            }
            // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
            else if (!widthOrHeight) {
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)
                        && Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
        }
        return result;
    }


    private Camera mCamera = null;
    private static int mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private static final String TAG = "CameraTest";


    public  void startPreview(Activity activity, int width, int height) {
        if (mCamera != null) {
            int mOrientation = getCameraPreviewOrientation(activity, mCameraID);
            mCamera.setDisplayOrientation(mOrientation);

            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size bestPreviewSize = getOptimalSize(parameters.getSupportedPreviewSizes(), width, height);
            parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
            Camera.Size bestPictureSize = getOptimalSize(parameters.getSupportedPictureSizes(), width, height);
            parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            Log.e(TAG, "camera startPreview: (" + width + " x " + height +")");
        }
    }
    private Size mPreviewSize;;

    class TemRender implements GLSurfaceView.Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.i(TAG,"onSurfaceCreated:");
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.i(TAG,"width:" +width + ",height:" +height);
            mPreviewSize = new Size(width,height);
            Message obtain = Message.obtain();
            obtain.what = OPEN_CAMERA;
            mHandler.sendMessage( obtain);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Log.i(TAG,"onDrawFrame:");
        }
    }
}