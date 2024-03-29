package com.opengldemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.util.Log;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLUtils.texImage2D;

import java.io.IOException;


public class TextureUtils {
    private static  String TAG = "TextureUtils";

    public static int createTexture(String text, int width, int height, int textSize){
        Bitmap bitmap = createBitmap(text,width,height,textSize);
        if(bitmap == null){
            Log.i(TAG,"bitmap is null");
            return -1;
        }

        final int[] textureObjectIds = new int[1];
        glGenTextures(1,textureObjectIds,0);
        glBindTexture(GL_TEXTURE_2D,textureObjectIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);

        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        glBindTexture(GL_TEXTURE_2D,0);
        return textureObjectIds[0];
    }

    public static Bitmap createBitmap(String text, int width, int height, int textSize){
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.argb(255,255,255,255));
        paint.setShadowLayer(1,0,1,Color.DKGRAY);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text,width/2,height/2,paint);

        return bitmap;
    }

    public static Bitmap readBitmapFromRes(Context context){
        Bitmap result = null;
        try {
            result = BitmapFactory.decodeStream(context.getAssets().open("lena.jpg"));
        } catch (IOException e) {
            Log.i(TAG,"loadPng2Texture : IOException");
            e.printStackTrace();
        }

        return result;
    }

    public static Bitmap read4KBitmapFromRes(Context context){
        Bitmap result = null;
        try {
            result = BitmapFactory.decodeStream(context.getAssets().open("4k.png"));
        } catch (IOException e) {
            Log.i(TAG,"loadPng2Texture : IOException");
            e.printStackTrace();
        }

        return result;
    }
    public static Bitmap read1080pBitmapFromRes(Context context){
        Bitmap result = null;
        try {
            result = BitmapFactory.decodeStream(context.getAssets().open("1080test.png"));
        } catch (IOException e) {
            Log.i(TAG,"loadPng2Texture : IOException");
            e.printStackTrace();
        }

        return result;
    }


}
