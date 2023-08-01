package com.opengldemo.egl;

import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.view.Surface;

public class WindowEglSurface extends EglSurfaceBase {

    private Surface mSurface;
    private boolean mReleaseSurface;


    public WindowEglSurface(EglCore eglCore, Surface surface) {
        this(eglCore,surface,false);
    }

    public WindowEglSurface(EglCore eglCore, Surface surface, boolean releaseSurface) {
        super(eglCore);
        createWindowSurface(surface);
        mSurface = surface;
        mReleaseSurface = releaseSurface;
    }

    public void release() {
        releaseEglSurface();
        if (mSurface != null && mReleaseSurface) {
            mSurface.release();
        }
        mSurface = null;
    }

    public void recreate(EglCore newEglCore) {
        if (mSurface == null) {
            throw new RuntimeException("Surface is null");
        }
        mEglCore = newEglCore;
        createWindowSurface(mSurface);
    }

}
