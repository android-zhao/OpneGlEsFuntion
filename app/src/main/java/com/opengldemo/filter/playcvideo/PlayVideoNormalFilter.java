package com.opengldemo.filter.playcvideo;

import android.content.Context;

import com.opengldemo.GLesUtils;
import com.opengldemo.R;
import com.opengldemo.TextureRotateUtil;

public class PlayVideoNormalFilter extends AbsPlayVideoBaseFilter {


    public PlayVideoNormalFilter(Context context){
        super(GLesUtils.readTextFileFromResource(context, R.raw.base_fliter_normal_vertex),
                GLesUtils.readTextFileFromResource(context, R.raw.base_filter_play_video_fragment));
    }

    public PlayVideoNormalFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    protected void onInit() {
        super.onInit();
        mVertexBuffer.clear();
        mVertexBuffer.put(TextureRotateUtil.VERTEX).position(0);
        mTextureBuffer.clear();
        mTextureBuffer.put(TextureRotateUtil.TEXTURE_ROTATE_0).position(0);
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
    }


}
