package com.opengldemo.view;

import android.media.MediaMetadataRetriever;

public class MediaUtils {

   public static int getVideoWidth(String path){
      MediaMetadataRetriever retriever = new MediaMetadataRetriever();
      retriever.setDataSource(path);
      //获取视频宽度（单位：px）
      String width_s = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
      int width = Integer.valueOf(width_s);
      return width;
   }

   public static int getVideoHeight(String path){
      MediaMetadataRetriever retriever = new MediaMetadataRetriever();
      retriever.setDataSource(path);
      //获取视频高度（单位：px）
      String hight_s = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
      int hight = Integer.valueOf(hight_s);
      return hight;
   }
}
