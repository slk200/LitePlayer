package org.tizzer.liteplayer.helper;

import android.content.ContentResolver;
import android.os.AsyncTask;

import org.tizzer.liteplayer.constants.FileType;
import org.tizzer.liteplayer.entity.VideoInfo;
import org.tizzer.liteplayer.util.MusicUtil;
import org.tizzer.liteplayer.util.VideoUtil;

import java.util.ArrayList;
import java.util.List;

public class ScanHelper {

    /**
     * 扫描媒体文件
     *
     * @param contentResolver
     * @param fileType
     * @param scanListener
     */
    public static void scanMediaFile(ContentResolver contentResolver, FileType fileType, OnScanListener scanListener) {
        ScanTask scanTask = new ScanTask(contentResolver, fileType, scanListener);
        scanTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 扫描监听器
     */
    public interface OnScanListener {
        void onStop(List list);
    }

    /**
     * 扫描任务
     */
    private static class ScanTask extends AsyncTask<Void, Void, List> {

        private ContentResolver contentResolver;
        private FileType fileType;
        private OnScanListener scanListener;

        ScanTask(ContentResolver contentResolver, FileType fileType, OnScanListener scanListener) {
            this.contentResolver = contentResolver;
            this.fileType = fileType;
            this.scanListener = scanListener;
        }

        @Override
        protected List doInBackground(Void... params) {
            List list = new ArrayList();
            switch (fileType) {
                case VIDEO:
                    list = VideoUtil.getVideoData(contentResolver);
                    break;
                case MUSIC:
                    list = MusicUtil.getMusicData(contentResolver);
                    break;
            }
            return list;
        }

        @Override
        protected void onPostExecute(List list) {
            if (scanListener != null) {
                scanListener.onStop(list);
            }
        }
    }
}
