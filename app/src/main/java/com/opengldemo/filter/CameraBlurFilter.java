package com.opengldemo.filter;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;

public class CameraBlurFilter extends BaseFilter {
    private static String TAG = "CameraBlurFilter";
    //todo 2个glsl 文件 待确认
    public CameraBlurFilter(Context context) {
//        super(GLesUtils.readTextFileFromResource(context, R.raw.camera_filter_blur_vertex),
//                GLesUtils.readTextFileFromResource(context, R.raw.camera_filter_blur_fragement));
        super(NORMAL_VERTEX_SHADER,
                GLesUtils.readTextFileFromResource(context, R.raw.camera_filter_blur_fragement));
    }

    private int blurSize;
    protected void onInit() {
        Log.i(TAG,"onInit begin");
        super.onInit();
        blurSize = GLES30.glGetUniformLocation(getProgramId(), "blurSize");
        Log.i(TAG,"onInit end");
    }

    protected void onInitialized() {
        Log.i(TAG,"onInitialized begin");
        super.onInitialized();
        setFloat(blurSize, 0.3f);
        Log.i(TAG,"onInitialized end");
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        Log.i(TAG,"onInputSizeChanged begin");
        super.onInputSizeChanged(width, height);
        Log.i(TAG,"onInputSizeChanged end");
    }

}
