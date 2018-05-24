package org.tizzer.liteplayer.entity;

import java.io.Serializable;

public class VideoInfo implements Serializable {
    private int id;
    private String title;
    private String resolution;
    private String thumb;
    private String path;
    private String duration;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", resolution='" + resolution + '\'' +
                ", thumb='" + thumb + '\'' +
                ", path='" + path + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
