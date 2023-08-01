package com.st.openglfilter;

public class NativeLib {

    // Used to load the 'openglfilter' library on application startup.
    static {
        System.loadLibrary("openglfilter");
    }

    /**
     * A native method that is implemented by the 'openglfilter' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}