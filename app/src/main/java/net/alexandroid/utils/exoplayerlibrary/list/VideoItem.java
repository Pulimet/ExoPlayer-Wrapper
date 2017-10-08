package net.alexandroid.utils.exoplayerlibrary.list;

public class VideoItem {
    String videoUrl;
    String thumbUrl;

    public VideoItem(String videoUrl, String thumbUrl) {
        this.videoUrl = videoUrl;
        this.thumbUrl = thumbUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }
}

