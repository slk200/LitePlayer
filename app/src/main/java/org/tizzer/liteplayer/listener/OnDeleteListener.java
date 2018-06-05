package org.tizzer.liteplayer.listener;

import org.tizzer.liteplayer.entity.MusicInfo;

/**
 * 媒体文件删除监听器
 */
public interface OnDeleteListener {
    void onVideoDelete(int position);

    boolean onMusicDelete(MusicInfo musicInfo, int position);
}
