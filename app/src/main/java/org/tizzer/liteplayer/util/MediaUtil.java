package org.tizzer.liteplayer.util;

import android.media.MediaMetadataRetriever;

import org.tizzer.liteplayer.constants.FileType;
import org.tizzer.liteplayer.entity.MusicInfo;
import org.tizzer.liteplayer.entity.VideoInfo;

import java.io.File;
import java.util.List;

public class MediaUtil {
    private static final String[] VIDEO_SUFFIX = {".mp4", ".3gp"}; //视频文件后缀
    private static final String[] MUSIC_SUFFIX = {".mp3", ".aac"}; //音频文件后缀

    /**
     * 扫描媒体文件
     *
     * @param files 文件列表
     * @param root  扫描路径
     */
    public static void scanMediaFile(List<File> files, File root, FileType fileType) {
        if (root.isDirectory()) {
            File[] fileList = root.listFiles();
            if (fileList == null) {
                return;
            }
            for (File file : fileList) {
                scanMediaFile(files, file, fileType);
            }
        } else {
            switch (fileType) {
                case VIDEO:
                    for (String suffix : VIDEO_SUFFIX) {
                        if (root.getName().endsWith(suffix)) {
                            files.add(root);
                        }
                    }
                    break;
                case MUSIC:
                    for (String suffix : MUSIC_SUFFIX) {
                        if (root.getName().endsWith(suffix)) {
                            files.add(root);
                        }
                    }
            }

        }
    }

    /**
     * 获取视频文件信息
     *
     * @param file 视频文件
     * @return 视频文件信息
     */
    public static VideoInfo getVideoInfo(File file) {
        VideoInfo videoInfo;
        MediaMetadataRetriever mVideoMetadataRetriever = new MediaMetadataRetriever();
        mVideoMetadataRetriever.setDataSource(file.getAbsolutePath());
        String duration = mVideoMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        //过滤少于60秒的视频
        if (Integer.parseInt(duration) <= 60000) {
            return null;
        }
        String filename = file.getName();
        videoInfo = new VideoInfo(
                filename.substring(0, filename.lastIndexOf('.')),
                mVideoMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH),
                mVideoMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT),
                duration,
                mVideoMetadataRetriever.getFrameAtTime(),
                file.getAbsolutePath()
        );
        mVideoMetadataRetriever.release();
        return videoInfo;
    }

    /**
     * 获取音频文件信息
     *
     * @param file 音频文件
     * @return 音频文件信息
     */
    public static MusicInfo getMusicInfo(File file) {
        MusicInfo musicInfo;
        MediaMetadataRetriever mMusicMetadataRetriever = new MediaMetadataRetriever();
        mMusicMetadataRetriever.setDataSource(file.getAbsolutePath());
        String duration = mMusicMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        //过滤少于30秒的音频
        if (Integer.parseInt(duration) <= 30000) {
            return null;
        }
        musicInfo = new MusicInfo(
                mMusicMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                mMusicMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                mMusicMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                duration,
                file.getAbsolutePath()
        );
        mMusicMetadataRetriever.release();
        return musicInfo;
    }

    /**
     * 获取专辑封面
     *
     * @param path 文件路径
     * @return 专辑封面bitmap
     */
    public static byte[] getAlbumArt(String path) {
        byte[] bytes = null;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            bytes = retriever.getEmbeddedPicture();
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

}
