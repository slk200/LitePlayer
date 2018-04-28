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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.adapter.MusicListAdapter;
import org.tizzer.liteplayer.constants.FileType;
import org.tizzer.liteplayer.constants.ResultCode;
import org.tizzer.liteplayer.entity.MusicInfo;
import org.tizzer.liteplayer.helper.ScanHelper;
import org.tizzer.liteplayer.listener.OnSimpleSeekBarChangeListener;
import org.tizzer.liteplayer.service.MusicPlayService;
import org.tizzer.liteplayer.util.MediaUtil;

import java.io.File;
import java.lang.ref.WeakReference;

public class MusicFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MusicFragment"; //日志

    /**
     * 控件
     */
    protected SwipeRefreshLayout mRefreshView;
    protected ListView mMusicList;
    protected RelativeLayout mPlayBar;
    protected SeekBar mSeekBar;
    protected TextView mTitleView, mArtistAlbumView;
    protected ImageView mAlbumView, mPlayView, mNextView;
    private MusicListAdapter mMusicListAdapter; //适配器

    private int currentPosition = -1; //当前播放位置
    private boolean isFirstTime = true; //是否第一次加载

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

        //适配视频列表
        mMusicListAdapter = new MusicListAdapter(getContext());
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

        mMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentPosition != position) {
                    currentPosition = position;
                    MusicInfo musicInfo = (MusicInfo) mMusicListAdapter.getItem(position);
                    if (new File(musicInfo.getPath()).exists()) {
                        start(musicInfo);
                    } else {
                        Toast.makeText(getContext(), R.string.music_lose, Toast.LENGTH_SHORT).show();
                        deleteMusic(musicInfo);
                    }
                } else {
                    pause();
                }
            }
        });

        mMusicList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MusicInfo musicInfo = (MusicInfo) mMusicListAdapter.getItem(position);
                MusicInfoDialogFragment.instance(musicInfo).show(getFragmentManager(), MusicInfoDialogFragment.MUSIC_INFO);
                return true;
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
        }
    }

    /**
     * 删除列表中的音乐
     *
     * @param musicInfo
     */
    public void deleteMusic(MusicInfo musicInfo) {
        mMusicListAdapter.removeItem(musicInfo);
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
            File root = Environment.getExternalStorageDirectory();
            ScanHelper.scanMediaFile(FileType.MUSIC, root, new ScanHelper.OnScanListener() {

                @Override
                public void onProcess(Object obj) {
                    if (obj != null) {
                        if (isFirstTime) {
                            mMusicListAdapter.addItem((MusicInfo) obj);
                        } else {
                            mMusicListAdapter.addTempItem((MusicInfo) obj);
                        }
                    }
                }

                @Override
                public void onStop() {
                    if (!isFirstTime) {
                        mMusicListAdapter.refresh();
                    } else {
                        isFirstTime = false;
                    }
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
        MusicInfo musicInfo;
        if (currentPosition < mMusicListAdapter.getCount() - 1) {
            currentPosition++;
            musicInfo = (MusicInfo) mMusicListAdapter.getItem(currentPosition);
        } else {
            currentPosition = 0;
            musicInfo = (MusicInfo) mMusicListAdapter.getItem(currentPosition);
        }

        Message message = Message.obtain();
        message.what = MusicPlayService.MSG_NEXT;
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
                case MusicPlayService.MSG_NEXT:
                    Log.e(TAG, "sendMessageToService: 切换出错" + e.getMessage());
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
                case MusicPlayService.MSG_NEXT:
                    handleNext(msg);
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
                Log.d(TAG, "handleMessage: 服务绑定成功");
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
                    .load(MediaUtil.getAlbumArt(musicInfo.getPath()))
                    .placeholder(R.drawable.ic_album)
                    .into(mMusicFragment.mAlbumView);
            if (mMusicFragment.mPlayBar.getVisibility() == View.GONE) {
                mMusicFragment.mPlayBar.setVisibility(View.VISIBLE);
                mMusicFragment.mPlayBar.requestFocus();
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
            mMusicFragment.mPlayView.setBackground(
                    flag ? mMusicFragment.getResources().getDrawable(R.drawable.ic_pause_circle) :
                            mMusicFragment.getResources().getDrawable(R.drawable.ic_play_circle));
        }

        /**
         * 处理下一首消息
         *
         * @param msg
         */
        private void handleNext(Message msg) {
            mMusicFragment.mSeekBar.setMax(msg.arg1);
            mMusicFragment.mPlayView.setBackground(mMusicFragment.getResources().getDrawable(R.drawable.ic_pause_circle));
            MusicInfo musicInfo = (MusicInfo) msg.obj;
            mMusicFragment.mTitleView.setText(musicInfo.getTitle());
            mMusicFragment.mArtistAlbumView.setText(String.valueOf(musicInfo.getArtist() + " - " + musicInfo.getAlbum()));
            Glide.with(mMusicFragment.getContext())
                    .load(MediaUtil.getAlbumArt(musicInfo.getPath()))
                    .placeholder(R.drawable.ic_album)
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
