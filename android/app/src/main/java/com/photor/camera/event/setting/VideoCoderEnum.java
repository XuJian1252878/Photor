package com.photor.camera.event.setting;

import com.otaliastudios.cameraview.VideoCodec;
import com.photor.R;

public enum VideoCoderEnum {

    DEVICE_DEFAULT(0, VideoCodec.DEVICE_DEFAULT, R.string.camera_video_codec_DEVICE_DEFAULT),
    H_263(1, VideoCodec.H_263, R.string.camera_video_codec_H_263),
    H_264(2, VideoCodec.H_264, R.string.camera_video_codec_H_264);

    private int index;
    private VideoCodec videoCodec;
    private int messageId;

    public int getMessageId() {
        return messageId;
    }

    public VideoCodec getVideoCodec() {
        return  videoCodec;
    }

    public static int getIndexByVideoCodec(VideoCodec videoCodec) {
        for (VideoCoderEnum vce: VideoCoderEnum.values()) {
            if (vce.getVideoCodec() == videoCodec) {
                return vce.index;
            }
        }
        return -1;
    }

    public static VideoCodec getVideoCodecByIndex(int index) {
        for (VideoCoderEnum vce: VideoCoderEnum.values()) {
            if (vce.index == index) {
                return vce.getVideoCodec();
            }
        }
        return null;
    }

    VideoCoderEnum(int index, VideoCodec videoCodec, int messageId) {
        this.index = index;
        this.videoCodec = videoCodec;
        this.messageId = messageId;
    }
}
