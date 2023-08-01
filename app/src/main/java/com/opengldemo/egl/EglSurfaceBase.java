package com.opengldemo.egl;

import android.opengl.EGL14;
import android.opengl.EGLSurface;

public class EglSurfaceBase {

    protected EglCore mEglCore;
    protected int mWidth = -1;
    protected int mHeight = -1;

    protected EglSurfaceBase(EglCore eglCore) {
        mEglCore = eglCore;
    }

    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    public void createWindowSurface(Object surface) {
        if (mEGLSurface != EGL14.EGL_NO_SURFACE) {
            throw new IllegalStateException("egl surface has already created");
        }
        mEGLSurface = mEglCore.createWindowSurface(surface);
    }
    public int getWidth() {
        if (mWidth <= 0) {
            mWidth = mEglCore.querySurface(mEGLSurface, EGL14.EGL_WIDTH);
        }
        return mWidth;
    }

    public int getHeight() {
        if (mHeight <= 0) {
            mHeight = mEglCore.querySurface(mEGLSurface, EGL14.EGL_HEIGHT);
        }
        return mHeight;
    }

    public void releaseEglSurface() {
        mEglCore.releaseSurface(mEGLSurface);
        mEGLSurface = EGL14.EGL_NO_SURFACE;
        mWidth = -1;
        mHeight = -1;
    }

    public void makeCurrent() {
        mEglCore.makeCurrent(mEGLSurface);
    }

    public void setPresentationTime(long nsec) {
        mEglCore.setPresentationTime(mEGLSurface, nsec);
    }
    public boolean swapBuffers() {
        return mEglCore.swapBuffers(mEGLSurface);
    }
}
