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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.activity.VideoPlayActivity;
import org.tizzer.liteplayer.adapter.VideoListAdapter;
import org.tizzer.liteplayer.constants.FileType;
import org.tizzer.liteplayer.entity.VideoInfo;
import org.tizzer.liteplayer.helper.ScanHelper;
import org.tizzer.liteplayer.listener.OnMusicPauseListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends Fragment {
    public static final String VIDEO_LIST = "video_list"; //intent视频路径标记
    public static final String CURRENT_POSITION = "current_position";
    private static final String TAG = "VideoFragment"; //日志

    /**
     * 控件
     */
    protected SwipeRefreshLayout mRefreshView;
    protected ListView mVideoList;
    protected FloatingActionButton mExitButton;
    private VideoListAdapter mVideoListAdapter; //适配器

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
        mVideoListAdapter = new VideoListAdapter(getContext(), mVideoInfos);
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

        mVideoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoInfo currentVideoInfo = (VideoInfo) mVideoListAdapter.getItem(position);
                if (new File(currentVideoInfo.getPath()).exists()) {
                    Intent intent = new Intent();
                    intent.setClass(getContext(), VideoPlayActivity.class);
                    ArrayList<String> videoPathList = new ArrayList<>();
                    for (VideoInfo videoInfo : mVideoInfos) {
                        videoPathList.add(videoInfo.getPath());
                    }
                    intent.putStringArrayListExtra(VIDEO_LIST, videoPathList);
                    intent.putExtra(CURRENT_POSITION, position);
                    getActivity().startActivity(intent);
                    pauseListener.onMusicPause();
                } else {
                    deleteVideo(currentVideoInfo);
                    Toast.makeText(getContext(), R.string.video_lose, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mVideoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                VideoInfo videoInfo = (VideoInfo) mVideoListAdapter.getItem(position);
                VideoInfoDialogFragment.instance(videoInfo).show(getFragmentManager(), VideoInfoDialogFragment.VIDEO_INFO);
                return true;
            }
        });

        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
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
     *
     * @param videoInfo
     */
    public void deleteVideo(VideoInfo videoInfo) {
        mVideoListAdapter.removeItem(videoInfo);
    }

    /**
     * 扫描视频
     */
    private void scanVideo() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ScanHelper.scanMediaFile(getContext().getContentResolver(), FileType.VIDEO, new ScanHelper.OnScanListener() {
                @Override
                public void onStop(List list) {
                    Log.e(TAG, "scanMusic: " + list);
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
