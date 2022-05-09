package net.alexandroid.utils.exoplayerlibrary.list;

public class VideoItem {
    private final String videoUrl;
    private final String thumbUrl;

    public VideoItem(String videoUrl, String thumbUrl) {
        this.videoUrl = videoUrl;
        this.thumbUrl = thumbUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }
}

