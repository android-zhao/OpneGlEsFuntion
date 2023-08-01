package com.opengldemo.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.opengldemo.R;
import com.opengldemo.render.NativeEglRender;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class NativeEglActivity extends Activity {

    NativeEglRender nativeEglRender;
    Button btn1 ;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_egl);

        initListener();
    }

    void  initListener(){
        nativeEglRender = new NativeEglRender();
        nativeEglRender.native_EglRenderInit();

        imageView = findViewById(R.id.native_egl_image_display);

        btn1 = findViewById(R.id.native_eg11);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRender();
            }
        });
    }

    void startRender(){
        //第二步：将图片资源传递给native层
        loadImage2Native(R.drawable.leg,nativeEglRender);
        //第三步：
        //todo 暂定传递的shader处理类型
        nativeEglRender.native_EglRenderSetIntParams(200 ,1);

        //第四步：开始绘制
        nativeEglRender.native_EglRenderDraw();
        //拿到渲染完成的数据
        imageView.setImageBitmap(createBitmapFromGLSurface(0,0,933,1440) );
    }

    void loadImage2Native(int resId,NativeEglRender render){
        InputStream is = this.getResources().openRawResource(resId);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                int bytes = bitmap.getByteCount();
                ByteBuffer buf = ByteBuffer.allocate(bytes);
                bitmap.copyPixelsToBuffer(buf);
                byte[] byteArray = buf.array();
                //第二步：将拿到的图片 传到native层
                render.native_EglRenderSetImageData(byteArray, bitmap.getWidth(), bitmap.getHeight());
            }
        }
        finally
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
    }

    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h) {

        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);
        try {
            GLES20.glReadPixels(x, y, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                    intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }
        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);

    }

}