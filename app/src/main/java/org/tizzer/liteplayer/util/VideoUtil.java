package org.tizzer.liteplayer.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import org.tizzer.liteplayer.entity.VideoInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoUtil {

    public static List<VideoInfo> getVideoData(ContentResolver contentResolver) {
        List<VideoInfo> videoInfos = new ArrayList<>();
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION
        };
        String[] thumbProjection = {MediaStore.Video.Thumbnails.DATA};
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        if (cursor != null) {
            VideoInfo videoInfo;
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(3);
                if (new File(filePath).exists()) {
                    videoInfo = new VideoInfo();
                    videoInfo.setId(cursor.getInt(0));
                    videoInfo.setTitle(cursor.getString(1));
                    videoInfo.setResolution(cursor.getString(2));
                    videoInfo.setPath(filePath);
                    videoInfo.setDuration(TimeUtil.mills2timescale(cursor.getInt(4), false));

                    String thumbSelection = MediaStore.Video.Thumbnails.VIDEO_ID + "=" + videoInfo.getId();
                    Cursor thumbCursor = contentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                            thumbProjection, thumbSelection, null, null);
                    if (thumbCursor != null) {
                        if (thumbCursor.moveToNext()) {
                            videoInfo.setThumb(thumbCursor.getString(0));
                        }
                        thumbCursor.close();
                    }
                    videoInfos.add(videoInfo);
                }
            }
            cursor.close();
        }
        return videoInfos;
    }
}
