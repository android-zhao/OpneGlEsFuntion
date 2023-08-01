package com.opengldemo.bean;

import java.util.ArrayList;
import java.util.List;

public class MediaData {

    public long frameId = -1l;
    private int width = -1;
    private int height = -1;
    private byte [] mediaData = null;
    private int fileType = -1;
    private List<MediaData> mMediaDataQueue = new ArrayList<>();


    public MediaData(byte[] mediaData, List<MediaData> mMediaDataQueue) {
        this.mediaData = mediaData;
        this.mMediaDataQueue = mMediaDataQueue;
    }

    public long getFrameId() {
        return frameId;
    }

    public void setFrameId(long frameId) {
        this.frameId = frameId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public byte[] getMediaData() {
        return mediaData;
    }
}
