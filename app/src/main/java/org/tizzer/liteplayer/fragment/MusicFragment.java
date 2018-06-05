package org.tizzer.liteplayer.fragment;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.adapter.MusicRecyclerViewAdapter;
import org.tizzer.liteplayer.constants.FileType;
import org.tizzer.liteplayer.constants.ResultCode;
import org.tizzer.liteplayer.entity.MusicInfo;
import org.tizzer.liteplayer.helper.ScanHelper;
import org.tizzer.liteplayer.listener.OnSimpleSeekBarChangeListener;
import org.tizzer.liteplayer.service.MusicPlayService;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MusicFragment extends Fragment implements View.OnClickListener, MusicRecyclerViewAdapter.OnClickListener {
    private static final String TAG = "MusicFragment"; //日志

    /**
     * 控件
     */
    protected SwipeRefreshLayout mRefreshView;
    protected RecyclerView mMusicList;
    protected RelativeLayout mPlayBar;
    protected SeekBar mSeekBar;
    protected TextView mTitleView, mArtistAlbumView;
    protected ImageView mAlbumView, mPlayView, mNextView, mPreviousView;
    private MusicRecyclerViewAdapter mMusicListAdapter; //适配器
    private List<MusicInfo> mMusicInfos = new ArrayList<>();

    private int currentPosition = -1; //当前播放位置
    private int longClickPosition = -1; //当前查看位置
    private long currentId = -1; //当前歌曲id

    /**
     * 服务
     */
    private boolean flag; //服务绑定标志
    private Intent mIntent; //全局intent
    private Messenger mServiceMessenger; //消息发送器
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            flag = true;

            Log.e(TAG, "onServiceConnected: ");
            Message message = Message.obtain();
            message.what = MusicPlayService.MSG_BIND;
            sendMessageToService(message);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: disconnect");
        }
    };

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, null);
        initView(view);
        setOnListener();
        initData();
        bindService();
        return view;
    }

    /**
     * 初始化视图
     *
     * @param view
     */
    private void initView(View view) {
        mRefreshView = view.findViewById(R.id.srl_music);
        mMusicList = view.findViewById(R.id.list_music);
        mPlayBar = view.findViewById(R.id.layout_play_bar);
        mSeekBar = view.findViewById(R.id.seek);
        mAlbumView = view.findViewById(R.id.iv_album);
        mTitleView = view.findViewById(R.id.tv_play_title);
        mArtistAlbumView = view.findViewById(R.id.tv_play_artist_album);
        mPlayView = view.findViewById(R.id.iv_music_play);
        mNextView = view.findViewById(R.id.iv_next);
        mPreviousView = view.findViewById(R.id.iv_previous);

        //适配视频列表
        mMusicList.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mMusicListAdapter = new MusicRecyclerViewAdapter(getActivity().getApplicationContext(), mMusicInfos, this);
        mMusicList.setAdapter(mMusicListAdapter);
    }

    /**
     * 设置监听器
     */
    private void setOnListener() {
        mRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scanMusic();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new OnSimpleSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Message message = Message.obtain();
                message.what = MusicPlayService.MSG_REMOVE;
                sendMessageToService(message);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                seek(progress);
            }
        });

        mPlayView.setOnClickListener(this);
        mNextView.setOnClickListener(this);
        mPreviousView.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mRefreshView.setRefreshing(true);
        scanMusic();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_music_play:
                pause();
                break;
            case R.id.iv_next:
                next();
                break;
            case R.id.iv_previous:
                previous();
                break;
        }
    }

    @Override
    public void onItemClick(int position, MusicInfo musicInfo) {
        if (currentId != musicInfo.getId()) {
            if (new File(musicInfo.getPath()).exists()) {
                currentId = musicInfo.getId();
                currentPosition = position;
                start(musicInfo);
            } else {
                deleteMusic(musicInfo, position);
                Toast.makeText(getContext(), R.string.music_lose, Toast.LENGTH_SHORT).show();
            }
        } else {
            pause();
        }
    }

    @Override
    public void onItemLongClick(int position, MusicInfo musicInfo) {
        longClickPosition = position;
        MusicInfoDialogFragment.instance(musicInfo, position).show(getFragmentManager(), MusicInfoDialogFragment.MUSIC_INFO);
    }

    /**
     * 删除列表中的音乐
     *
     * @param musicInfo
     */
    public boolean deleteMusic(MusicInfo musicInfo, int position) {
        Log.e(TAG, "deleteMusic: " + musicInfo.getId() + "  ?  " + currentId);
        if (musicInfo.getId() != currentId) {
            mMusicInfos.remove(position);
            mMusicListAdapter.notifyItemRemoved(position);
            if (longClickPosition < currentPosition) {
                currentPosition--;
            }
            Toast.makeText(getContext(), R.string.music_playing, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 暂停音乐
     * <code>
     * 当用户播放电影时调用
     * </code>
     */
    public void pauseMusic() {
        Message message = Message.obtain();
        message.what = MusicPlayService.MSG_STATE;
        sendMessageToService(message);
    }

    /**
     * 绑定服务
     */
    private void bindService() {
        if (!flag) {
            mIntent = new Intent(getContext(), MusicPlayService.class);
            getActivity().startService(mIntent);
            getActivity().bindService(mIntent, mServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    /**
     * 扫描音频
     */
    private void scanMusic() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ScanHelper.scanMediaFile(getContext().getContentResolver(), FileType.MUSIC, new ScanHelper.OnScanListener() {
                @Override
                public void onStop(List list) {
                    Log.e(TAG, "scanMusic: " + list);
                    if (!mMusicInfos.isEmpty()) {
                        mMusicInfos.clear();
                    }
                    mMusicInfos.addAll(list);
                    mMusicListAdapter.notifyDataSetChanged();
                    mRefreshView.setRefreshing(false);
                }
            });
        }
    }

    /**
     * 重写销毁事件
     */
    @Override
    public void onDestroy() {
        if (flag) {
            getActivity().unbindService(mServiceConnection);
            getActivity().stopService(mIntent);
        }
        super.onDestroy();
    }

    /**
     * 播放
     *
     * @param musicInfo
     */
    private void start(MusicInfo musicInfo) {
        Message message = Message.obtain();
        message.what = MusicPlayService.MSG_START;
        message.obj = musicInfo;
        sendMessageToService(message);
    }

    /**
     * 暂停开始
     */
    private void pause() {
        Message message = Message.obtain();
        message.what = MusicPlayService.MSG_PAUSE;
        sendMessageToService(message);
    }

    /**
     * 拖动
     *
     * @param progress
     */
    private void seek(int progress) {
        Message message = Message.obtain();
        message.what = MusicPlayService.MSG_SEEK;
        message.arg1 = progress;
        sendMessageToService(message);
    }

    /**
     * 下一首
     */
    private void next() {
        if (currentPosition < mMusicListAdapter.getItemCount() - 1) {
            currentPosition++;
        } else {
            currentPosition = 0;
        }
        switchSource();
    }

    /**
     * 上一曲
     */
    private void previous() {
        if ((currentPosition > 0)) {
            currentPosition--;
        } else {
            currentPosition = mMusicListAdapter.getItemCount() - 1;
        }
        switchSource();
    }

    /**
     * 切歌
     */
    private void switchSource() {
        MusicInfo musicInfo = mMusicInfos.get(currentPosition);
        Message message = Message.obtain();
        message.what = MusicPlayService.MSG_SWITCH;
        message.obj = musicInfo;
        sendMessageToService(message);
    }

    /**
     * 发送消息到服务
     *
     * @param message
     */
    private void sendMessageToService(Message message) {
        try {
            if (message.what == MusicPlayService.MSG_BIND) {
                MessageHandler mMessageHandler = new MessageHandler(this);
                message.replyTo = new Messenger(mMessageHandler);
            }
            mServiceMessenger.send(message);
        } catch (RemoteException e) {
            switch (message.what) {
                case MusicPlayService.MSG_BIND:
                    Log.e(TAG, "sendMessageToService: 绑定出错" + e.getMessage());
                    break;
                case MusicPlayService.MSG_START:
                    Log.e(TAG, "sendMessageToService: 播放出错" + e.getMessage());
                    break;
                case MusicPlayService.MSG_PAUSE:
                    Log.e(TAG, "sendMessageToService: 暂停出错" + e.getMessage());
                    break;
                case MusicPlayService.MSG_SEEK:
                    Log.e(TAG, "sendMessageToService: 拖动出错" + e.getMessage());
                    break;
                case MusicPlayService.MSG_SWITCH:
                    Log.e(TAG, "sendMessageToService: 切歌出错" + e.getMessage());
                    break;
            }
        }
    }

    /**
     * 处理服务发送的消息
     */
    private static class MessageHandler extends Handler {

        private WeakReference<MusicFragment> mWeakReference;
        private MusicFragment mMusicFragment;

        MessageHandler(MusicFragment musicFragment) {
            this.mWeakReference = new WeakReference<>(musicFragment);
            mMusicFragment = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MusicPlayService.MSG_BIND:
                    handleBind(msg);
                    break;
                case MusicPlayService.MSG_START:
                    handleStart(msg);
                    break;
                case MusicPlayService.MSG_UPDATE:
                    handleUpdate(msg);
                    break;
                case MusicPlayService.MSG_PAUSE:
                    handlePause(msg);
                    break;
                case MusicPlayService.MSG_SWITCH:
                    handleSwitch(msg);
                    break;
                case MusicPlayService.MSG_STOP:
                    handleStop();
            }
        }

        /**
         * 处理绑定消息
         *
         * @param msg
         */
        private void handleBind(Message msg) {
            if (msg.arg1 == ResultCode.OK) {
                Log.e(TAG, "handleMessage: 服务绑定成功");
            }
        }

        /**
         * 处理开始播放消息
         *
         * @param msg
         */
        private void handleStart(Message msg) {
            mMusicFragment.mSeekBar.setMax(msg.arg1);
            MusicInfo musicInfo = (MusicInfo) msg.obj;
            mMusicFragment.mTitleView.setText(musicInfo.getTitle());
            mMusicFragment.mArtistAlbumView.setText(String.valueOf(musicInfo.getArtist() + " - " + musicInfo.getAlbum()));
            Glide.with(mMusicFragment.getContext())
                    .load(musicInfo.getAlbumArt())
                    .asBitmap()
                    .placeholder(R.drawable.ic_album_placeholder)
                    .into(mMusicFragment.mAlbumView);
            if (mMusicFragment.mPlayBar.getVisibility() == View.GONE) {
                mMusicFragment.mPlayBar.setVisibility(View.VISIBLE);
            } else {
                mMusicFragment.mPlayView.setBackgroundResource(R.drawable.ic_pause_circle);
            }
        }

        /**
         * 处理更新消息
         *
         * @param msg
         */
        private void handleUpdate(Message msg) {
            mMusicFragment.mSeekBar.setProgress(msg.arg1);
        }

        /**
         * 处理暂停消息
         *
         * @param msg
         */
        private void handlePause(Message msg) {
            boolean flag = (boolean) msg.obj;
            mMusicFragment.mPlayView.setBackgroundResource(flag ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
        }

        /**
         * 处理切歌消息
         *
         * @param msg
         */
        private void handleSwitch(Message msg) {
            mMusicFragment.mSeekBar.setMax(msg.arg1);
            mMusicFragment.mPlayView.setBackgroundResource(R.drawable.ic_pause_circle);
            MusicInfo musicInfo = (MusicInfo) msg.obj;
            mMusicFragment.mTitleView.setText(musicInfo.getTitle());
            mMusicFragment.mArtistAlbumView.setText(String.valueOf(musicInfo.getArtist() + " - " + musicInfo.getAlbum()));
            Glide.with(mMusicFragment.getContext())
                    .load(musicInfo.getAlbumArt())
                    .asBitmap()
                    .placeholder(R.drawable.ic_album_placeholder)
                    .into(mMusicFragment.mAlbumView);
        }

        /**
         * 处理播放结束消息
         */
        private void handleStop() {
            mMusicFragment.mSeekBar.setProgress(0);
            mMusicFragment.next();
        }
    }
}
