package com.photor.camera.event.setting;

import com.otaliastudios.cameraview.VideoQuality;
import com.photor.R;

public enum VideoQualityEnum {
    LOWEST(0, VideoQuality.LOWEST, R.string.camera_video_quality_LOWEST),
    HIGHEST(1, VideoQuality.HIGHEST, R.string.camera_video_quality_HIGHEST),
    MAX_QVGA(2, VideoQuality.MAX_QVGA, R.string.camera_video_quality_MAX_QVGA),
    MAX_480P(3, VideoQuality.MAX_480P, R.string.camera_video_quality_MAX_480P),
    MAX_720P(4, VideoQuality.MAX_720P, R.string.camera_video_quality_MAX_720P),
    MAX_1080P(5, VideoQuality.MAX_1080P, R.string.camera_video_quality_MAX_1080P),
    MAX_2160P(6, VideoQuality.MAX_2160P, R.string.camera_video_quality_MAX_2160P);

    private int index;
    private VideoQuality videoQuality;
    private int messageId;

    public int getMessageId() {
        return messageId;
    }

    public VideoQuality getVideoQuality() {
        return videoQuality;
    }

    public int getIndex() {
        return index;
    }

    public static int getIndexByVideoQuality(VideoQuality videoQuality) {
        for (VideoQualityEnum vqe: VideoQualityEnum.values()) {
            if (vqe.getVideoQuality() == videoQuality) {
                return vqe.getIndex();
            }
        }
        return -1;
    }

    public static VideoQuality getVideoQualityByIndex(int index) {
        for (VideoQualityEnum vqe: VideoQualityEnum.values()) {
            if (vqe.getIndex() == index) {
                return vqe.getVideoQuality();
            }
        }
        return null;
    }

    VideoQualityEnum(int index, VideoQuality videoQuality, int messageId) {
        this.index = index;
        this.videoQuality = videoQuality;
        this.messageId = messageId;
    }
}
