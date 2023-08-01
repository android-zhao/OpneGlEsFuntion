package com.st.livepull;

public class NativeLib {

    // Used to load the 'livepull' library on application startup.
    static {
        System.loadLibrary("livepull");
    }

    /**
     * A native method that is implemented by the 'livepull' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}