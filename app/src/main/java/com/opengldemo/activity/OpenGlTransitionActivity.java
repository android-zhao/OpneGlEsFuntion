package com.opengldemo.activity;

import static android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY;
import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

import static com.opengldemo.render.MyNativeGLRenderProxy.SAMPLE_TYPE_KEY_TRANSITIONS_1;
import static com.opengldemo.render.MyNativeGLRenderProxy.SAMPLE_TYPE_KEY_TRANSITIONS_4;
import static com.opengldemo.view.MyGlSurfaceView.IMAGE_FORMAT_RGBA;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;


import com.opengldemo.R;
import com.opengldemo.render.MyNativeGLRenderProxy;
import com.opengldemo.view.MyGlSurfaceView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class OpenGlTransitionActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener {

    private String TAG = "OpenGlTransition_ac";
    MyGlSurfaceView mGlSurfaceView;
    private ViewGroup mRootView;
    private MyNativeGLRenderProxy mNativeRenderProxy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"onCreate begin");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl_transition);
        mRootView = (ViewGroup) findViewById(R.id.parent);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    private void loadAllBitmap(){
        loadBitmap(R.drawable.lye,0);
        loadBitmap(R.drawable.lye2,1);
        loadBitmap(R.drawable.lye4,2);
        loadBitmap(R.drawable.lye5,3);
        loadBitmap(R.drawable.lye6,4);
        Bitmap bitmap =  loadBitmap(R.drawable.lye7,5);
        Log.i(TAG,"loadAllBitmap end");
        mGlSurfaceView.setAspectRatio(bitmap.getWidth(),bitmap.getHeight());
        mGlSurfaceView.setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    private Bitmap loadBitmap(int resId, int index){
        InputStream is = this.getResources().openRawResource(resId);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                int bytes = bitmap.getByteCount();
                ByteBuffer buf = ByteBuffer.allocate(bytes);
                bitmap.copyPixelsToBuffer(buf);
                byte[] byteArray = buf.array();
                mNativeRenderProxy.setImageDataWithIndex(index, IMAGE_FORMAT_RGBA, bitmap.getWidth(), bitmap.getHeight(), byteArray);
            }
        } finally
        {
            try
            {
                is.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    @Override
    public void onGlobalLayout() {
        Log.i(TAG,"onGlobalLayout begin");
        mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        mNativeRenderProxy = new MyNativeGLRenderProxy();
        mGlSurfaceView = new MyGlSurfaceView(this);
        mGlSurfaceView.init(mNativeRenderProxy);
        mRootView.addView(mGlSurfaceView, lp);
        mGlSurfaceView.setRenderMode(RENDERMODE_CONTINUOUSLY);

        mNativeRenderProxy.init();
        mNativeRenderProxy.setParamsInt(MyNativeGLRenderProxy.SAMPLE_TYPE,SAMPLE_TYPE_KEY_TRANSITIONS_1,0);
        loadAllBitmap();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNativeRenderProxy.unInit();
    }


}