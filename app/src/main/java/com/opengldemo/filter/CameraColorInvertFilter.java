package com.opengldemo.filter;

import android.content.Context;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;


public class CameraColorInvertFilter extends BaseFilter{


    public CameraColorInvertFilter(Context context) {

        super(NORMAL_VERTEX_SHADER,
                GLesUtils.readTextFileFromResource(context, R.raw.camera_filter_invert_color)
        );
    }

    protected void onInit() {
        super.onInit();
    }

    protected void onInitialized() {
        super.onInitialized();
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
    }

}
