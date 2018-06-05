package org.tizzer.liteplayer.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.tizzer.liteplayer.entity.MusicInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicUtil {

    private static final String EXTERNAL_ALBUM_URI = "content://media/external/audio/albums";
    private static final String ALBUM_ART = "album_art";
    private static final String SUFFIX_REGEX = "(\\s|\\S)+(.mp3|.aac)";

    public static List<MusicInfo> getMusicData(ContentResolver contentResolver) {
        List<MusicInfo> musicInfos = new ArrayList<>();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        String[] albumArtProjection = {ALBUM_ART};
        if (cursor != null) {
            MusicInfo musicInfo;
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(5);
                if (new File(filePath).exists() && filePath.matches(SUFFIX_REGEX)) {
                    musicInfo = new MusicInfo();
                    musicInfo.setId(cursor.getInt(0));
                    musicInfo.setTitle(cursor.getString(1));
                    musicInfo.setArtist(cursor.getString(2));
                    musicInfo.setAlbum(cursor.getString(3));
                    musicInfo.setDuration(TimeUtil.mills2timescale(cursor.getInt(4), false));
                    musicInfo.setPath(filePath);

                    int albumId = cursor.getInt(6);
                    Cursor albumArtCursor = contentResolver.query(Uri.parse(EXTERNAL_ALBUM_URI + "/" + albumId),
                            albumArtProjection, null, null, null);
                    if (albumArtCursor != null) {
                        if (albumArtCursor.moveToNext()) {
                            musicInfo.setAlbumArt(albumArtCursor.getString(0));
                        }
                        albumArtCursor.close();
                    }
                    musicInfos.add(musicInfo);
                }
            }
            cursor.close();
        }
        return musicInfos;
    }
}
