package com.st.livepush;

public class NativeLib {

    // Used to load the 'livepush' library on application startup.
    static {
        System.loadLibrary("livepush");
    }

    /**
     * A native method that is implemented by the 'livepush' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}