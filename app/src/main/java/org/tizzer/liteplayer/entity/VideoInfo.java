package org.tizzer.liteplayer.entity;

import android.graphics.Bitmap;

import java.io.Serializable;

public class VideoInfo implements Serializable {
    private String title;
    private String width;
    private String height;
    private String duration;
    private Bitmap thumb;
    private String path;

    public VideoInfo() {
    }

    public VideoInfo(String title, String width, String height, String duration, Bitmap thumb, String path) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.duration = duration;
        this.thumb = thumb;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
                "title='" + title + '\'' +
                ", width='" + width + '\'' +
                ", height='" + height + '\'' +
                ", duration='" + duration + '\'' +
                ", thumb=" + thumb +
                ", path='" + path + '\'' +
                '}';
    }
}
