package org.tizzer.liteplayer.helper;

import android.os.AsyncTask;

import org.tizzer.liteplayer.constants.FileType;
import org.tizzer.liteplayer.entity.MusicInfo;
import org.tizzer.liteplayer.entity.VideoInfo;
import org.tizzer.liteplayer.util.MediaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScanHelper {

    /**
     * 扫描媒体文件
     *
     * @param fileType
     * @param root
     * @param scanListener
     */
    public static void scanMediaFile(FileType fileType, File root, OnScanListener scanListener) {
        ScanTask scanTask = new ScanTask(fileType, root, scanListener);
        scanTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 扫描监听器
     */
    public interface OnScanListener {

        void onProcess(Object obj);

        void onStop();
    }

    /**
     * 扫描任务
     */
    private static class ScanTask extends AsyncTask<Void, Object, Void> {

        private FileType fileType;
        private File root;
        private OnScanListener scanListener;

        ScanTask(FileType fileType, File root, OnScanListener scanListener) {
            this.fileType = fileType;
            this.root = root;
            this.scanListener = scanListener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<File> files = new ArrayList<>();
            MediaUtil.scanMediaFile(files, root, fileType);
            int size = files.size();
            switch (fileType) {
                case VIDEO:
                    for (int i = 0; i < size; i++) {
                        VideoInfo videoInfo = MediaUtil.getVideoInfo(files.get(i));
                        publishProgress(videoInfo);
                    }
                    break;
                case MUSIC:
                    for (int i = 0; i < size; i++) {
                        MusicInfo musicInfo = MediaUtil.getMusicInfo(files.get(i));
                        publishProgress(musicInfo);
                    }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            if (scanListener != null) {
                scanListener.onProcess(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (scanListener != null) {
                scanListener.onStop();
            }
        }
    }
}
