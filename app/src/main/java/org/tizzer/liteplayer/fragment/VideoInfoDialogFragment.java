package org.tizzer.liteplayer.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.entity.VideoInfo;
import org.tizzer.liteplayer.listener.OnDeleteListener;
import org.tizzer.liteplayer.util.SpaceUtil;
import org.tizzer.liteplayer.util.TimeUtil;

import java.io.File;

public class VideoInfoDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    public static final String VIDEO_INFO = "video_info"; //bundle视频对象标志
    public static final String VIDEO_POSITION = "video_position"; //bundle视频位置标志

    private static final String TAG = "VideoInfoDialogFragment"; //日志

    private VideoInfo mVideoInfo; //视频对象

    private OnDeleteListener deleteListener; //删除回调

    /**
     * 创建视频信息窗口对象
     *
     * @param videoInfo
     * @return
     */
    public static VideoInfoDialogFragment instance(VideoInfo videoInfo, int position) {
        VideoInfoDialogFragment videoInfoDialogFragment = new VideoInfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VIDEO_INFO, videoInfo);
        bundle.putInt(VIDEO_POSITION, position);
        videoInfoDialogFragment.setArguments(bundle);
        return videoInfoDialogFragment;
    }

    /**
     * 获取activity上下文环境
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            deleteListener = (OnDeleteListener) context;
        } catch (Exception e) {
            Log.e(TAG, "onAttach: " + e.getMessage());
        }
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_video, null);
        initView(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(mVideoInfo.getTitle())
                .setView(view)
                .setNeutralButton(R.string.delete, this)
                .setPositiveButton(R.string.close, this);
        return builder.create();
    }

    /**
     * 初始化视图
     *
     * @param view
     */
    private void initView(View view) {
        mVideoInfo = (VideoInfo) getArguments().getSerializable(VIDEO_INFO);

        TextView filenameView = view.findViewById(R.id.tv_filename);
        TextView pathView = view.findViewById(R.id.tv_path);
        TextView sizeView = view.findViewById(R.id.tv_size);
        TextView timeView = view.findViewById(R.id.tv_time);
        TextView resolutionView = view.findViewById(R.id.tv_resolution);
        TextView durationView = view.findViewById(R.id.tv_duration);

        File file = new File(mVideoInfo.getPath());
        filenameView.setText(file.getName());
        pathView.setText(mVideoInfo.getPath());
        sizeView.setText(String.valueOf(SpaceUtil.byte2space(file.length()) + "（" + file.length() + "字节）"));
        timeView.setText(TimeUtil.timestamp2datetime(file.lastModified()));
        resolutionView.setText(mVideoInfo.getResolution());
        durationView.setText(mVideoInfo.getDuration());
    }

    /**
     * dialog按钮点击事件
     *
     * @param dialog
     * @param which
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_NEUTRAL:
                File file = new File(mVideoInfo.getPath());
                if (file.exists()) {
                    file.delete();
                }
                Toast.makeText(getContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                deleteListener.onVideoDelete(getArguments().getInt(VIDEO_POSITION));
                break;
            case DialogInterface.BUTTON_POSITIVE:
                dismiss();
        }
    }
}
