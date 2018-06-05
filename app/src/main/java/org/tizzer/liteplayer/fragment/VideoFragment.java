package org.tizzer.liteplayer.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.activity.VideoPlayActivity;
import org.tizzer.liteplayer.adapter.VideoRecyclerViewAdapter;
import org.tizzer.liteplayer.constants.FileType;
import org.tizzer.liteplayer.entity.VideoInfo;
import org.tizzer.liteplayer.helper.ScanHelper;
import org.tizzer.liteplayer.listener.OnMusicPauseListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends Fragment implements VideoRecyclerViewAdapter.OnClickListener {
    public static final String VIDEO_LIST = "video_list"; //intent视频路径标记
    public static final String CURRENT_POSITION = "current_position";
    private static final String TAG = "VideoFragment"; //日志

    /**
     * 控件
     */
    protected SwipeRefreshLayout mRefreshView;
    protected RecyclerView mVideoList;
    protected FloatingActionButton mExitButton;
    private VideoRecyclerViewAdapter mVideoListAdapter; //适配器

    private OnMusicPauseListener pauseListener; //暂停音乐播放回调

    private ArrayList<VideoInfo> mVideoInfos = new ArrayList<>();

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, null);
        initView(view);
        setOnListener();
        initData();
        return view;
    }

    /**
     * 初始化视图
     *
     * @param view
     */
    private void initView(View view) {
        mRefreshView = view.findViewById(R.id.srl_video);
        mVideoList = view.findViewById(R.id.list_video);
        mExitButton = view.findViewById(R.id.fab_exit);

        //适配视频列表
        mVideoList.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mVideoListAdapter = new VideoRecyclerViewAdapter(getActivity().getApplicationContext(), mVideoInfos, this);
        mVideoList.setAdapter(mVideoListAdapter);
    }

    /**
     * 设置监听器
     */
    private void setOnListener() {
        mRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scanVideo();
            }
        });

        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    @Override
    public void onItemClick(int position, VideoInfo videoInfo) {
        if (new File(videoInfo.getPath()).exists()) {
            Intent intent = new Intent();
            intent.setClass(getContext(), VideoPlayActivity.class);
            ArrayList<String> videoPathList = new ArrayList<>();
            for (VideoInfo info : mVideoInfos) {
                videoPathList.add(info.getPath());
            }
            intent.putStringArrayListExtra(VIDEO_LIST, videoPathList);
            intent.putExtra(CURRENT_POSITION, position);
            getActivity().startActivity(intent);
            pauseListener.onMusicPause();
        } else {
            deleteVideo(position);
            Toast.makeText(getContext(), R.string.video_lose, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void OnItemLongClick(int position, VideoInfo videoInfo) {
        VideoInfoDialogFragment.instance(videoInfo, position).show(getFragmentManager(), VideoInfoDialogFragment.VIDEO_INFO);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mRefreshView.setRefreshing(true);
        scanVideo();
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
            pauseListener = (OnMusicPauseListener) context;
        } catch (Exception e) {
            Log.e(TAG, "onAttach: " + e.getMessage());
        }
    }

    /**
     * 删除列表中的视频
     */
    public void deleteVideo(int position) {
        mVideoInfos.remove(position);
        mVideoListAdapter.notifyItemRemoved(position);
    }

    /**
     * 扫描视频
     */
    private void scanVideo() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ScanHelper.scanMediaFile(getContext().getContentResolver(), FileType.VIDEO, new ScanHelper.OnScanListener() {
                @Override
                public void onStop(List list) {
                    Log.e(TAG, "scanVideo: " + list);
                    if (!mVideoInfos.isEmpty()) {
                        mVideoInfos.clear();
                    }
                    mVideoInfos.addAll(list);
                    mVideoListAdapter.notifyDataSetChanged();
                    mRefreshView.setRefreshing(false);
                }
            });
        }
    }
}
