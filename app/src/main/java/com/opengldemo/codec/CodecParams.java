package com.opengldemo.codec;

public class CodecParams {
    private String videoPath = "";

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public String toString() {
        return "CodecParams{" +
                "videoPath='" + videoPath + '\'' +
                '}';
    }
}
