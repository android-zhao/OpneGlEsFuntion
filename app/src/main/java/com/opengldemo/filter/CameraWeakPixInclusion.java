package com.opengldemo.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;

public class CameraWeakPixInclusion extends BaseFilter{

    public CameraWeakPixInclusion(Context context) {
        super(GLesUtils.readTextFileFromResource(context, R.raw.weak_pixl_inclusion),
                GLesUtils.readTextFileFromResource(context,R.raw.weak_pixl_inclusion_fragment));
    }

    private int uniformTexelWidthLocation;
    private int uniformTexelHeightLocation;
    @Override
    protected void onInit() {
        super.onInit();
        uniformTexelWidthLocation = GLES20.glGetUniformLocation(getProgramId(), "texelWidth");
        uniformTexelHeightLocation = GLES20.glGetUniformLocation(getProgramId(), "texelHeight");

    }

    private float texelWidth;
    private float texelHeight;
    @Override
    protected void onInitialized() {
        super.onInitialized();

        if (texelWidth != 0) {
            updateTexelValues();
        }
    }

    private void updateTexelValues(){
        setFloat(uniformTexelWidthLocation, texelWidth);
        setFloat(uniformTexelHeightLocation, texelHeight);
    }

    private boolean hasOverriddenImageSizeFactor = false;
    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);

        if (!hasOverriddenImageSizeFactor) {
            setLineSize(lineSize);
        }
    }

    private float lineSize = 0.001f;
    public void setLineSize(final float size) {
        lineSize = size;
        texelWidth = size / getOutputWidth();
        texelHeight = size / getOutputHeight();
        updateTexelValues();
    }
}

