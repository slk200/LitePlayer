package org.tizzer.liteplayer.listener;

import org.tizzer.liteplayer.entity.MusicInfo;
import org.tizzer.liteplayer.entity.VideoInfo;

/**
 * 媒体文件删除监听器
 */
public interface OnDeleteListener {
    void onVideoDelete(VideoInfo videoInfo);

    boolean onMusicDelete(MusicInfo musicInfo);
}
