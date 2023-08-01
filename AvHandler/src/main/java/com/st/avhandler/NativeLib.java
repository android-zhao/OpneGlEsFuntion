package com.st.avhandler;

public class NativeLib {

    // Used to load the 'avhandler' library on application startup.
    static {
        System.loadLibrary("avhandler");
    }

    /**
     * A native method that is implemented by the 'avhandler' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}