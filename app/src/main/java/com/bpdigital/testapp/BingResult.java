package com.bpdigital.testapp;


/**
 * Created by Anton on 08.11.2015.
 */
public class BingResult {
    private String mediaUrl;
    private String thumbnailUrl;

    public BingResult(String thumbnailUrl, String mediaUrl) {
        this.thumbnailUrl = thumbnailUrl;
        this.mediaUrl = mediaUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
