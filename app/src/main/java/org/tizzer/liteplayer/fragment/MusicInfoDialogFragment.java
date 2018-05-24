package org.tizzer.liteplayer.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.entity.MusicInfo;
import org.tizzer.liteplayer.listener.OnDeleteListener;
import org.tizzer.liteplayer.util.SpaceUtil;
import org.tizzer.liteplayer.util.TimeUtil;

import java.io.File;

public class MusicInfoDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    public static final String MUSIC_INFO = "music_info"; //bundle音频对象标志

    private static final String TAG = "MusicInfoDialogFragment"; //日志

    private MusicInfo mMusicInfo; //视频对象

    private OnDeleteListener deleteListener; //删除回调

    /**
     * 创建视频信息窗口对象
     *
     * @param musicInfo
     * @return
     */
    public static MusicInfoDialogFragment instance(MusicInfo musicInfo) {
        MusicInfoDialogFragment videoInfoDialogFragment = new MusicInfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MUSIC_INFO, musicInfo);
        videoInfoDialogFragment.setArguments(bundle);
        return videoInfoDialogFragment;
    }

    /**
     * 获取activity的上下文环境
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_music, null);
        initView(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(mMusicInfo.getTitle())
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
        mMusicInfo = (MusicInfo) getArguments().getSerializable(MUSIC_INFO);

        TextView filenameView = view.findViewById(R.id.tv_filename);
        TextView pathView = view.findViewById(R.id.tv_path);
        TextView sizeView = view.findViewById(R.id.tv_size);
        TextView timeView = view.findViewById(R.id.tv_time);
        TextView titleView = view.findViewById(R.id.tv_title);
        TextView artistView = view.findViewById(R.id.tv_artist);
        TextView albumView = view.findViewById(R.id.tv_album);
        TextView durationView = view.findViewById(R.id.tv_duration);

        File file = new File(mMusicInfo.getPath());
        filenameView.setText(file.getName());
        pathView.setText(mMusicInfo.getPath());
        sizeView.setText(String.valueOf(SpaceUtil.byte2space(file.length()) + "（" + file.length() + "字节）"));
        timeView.setText(TimeUtil.timestamp2datetime(file.lastModified()));
        titleView.setText(mMusicInfo.getTitle());
        artistView.setText(mMusicInfo.getArtist());
        albumView.setText(mMusicInfo.getAlbum());
        durationView.setText(mMusicInfo.getDuration());
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
                boolean result = deleteListener.onMusicDelete(mMusicInfo);
                if (result) {
                    File file = new File(mMusicInfo.getPath());
                    if (file.exists()) {
                        file.delete();
                    }
                    Toast.makeText(getContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                }
                break;
            case DialogInterface.BUTTON_POSITIVE:
                dismiss();
        }
    }
}
