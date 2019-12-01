package com.copell.upscale.model;

public class Photo {
    public String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Photo(String filename) {
        this.filename = filename;
    }
}
