package org.tizzer.liteplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.tizzer.liteplayer.constants.ResultCode;
import org.tizzer.liteplayer.entity.MusicInfo;

import java.lang.ref.WeakReference;

public class MusicPlayService extends Service {
    /**
     * 服务标识
     */
    public static final int MSG_BIND = 0x1001;
    public static final int MSG_UPDATE = 0x1002;
    public static final int MSG_START = 0x1003;
    public static final int MSG_PAUSE = 0x1004;
    public static final int MSG_SEEK = 0x1005;
    public static final int MSG_SWITCH = 0x1006;
    public static final int MSG_STOP = 0x1007;
    public static final int MSG_REMOVE = 0x1008;
    public static final int MSG_STATE = 0x1009;

    private static final String TAG = "MusicPlayService"; //日志
    private static final int UPDATE_DELAY = 500;

    /**
     * 服务传值
     */
    private Messenger mActivityMessenger;
    private MessageHandler mMessageHandler = new MessageHandler(this);

    /**
     * 服务资源
     */
    private MediaPlayer mMediaPlayer;
    private MusicInfo mCurrentMusicInfo;

    /**
     * 更新状态任务
     */
    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer.isPlaying()) {
                Message message = Message.obtain();
                message.what = MSG_UPDATE;
                message.arg1 = mMediaPlayer.getCurrentPosition();
                sendMessageToActivity(message);
            }
            mMessageHandler.postDelayed(this, UPDATE_DELAY);
        }
    };

    /**
     * 绑定
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Messenger messenger = new Messenger(mMessageHandler);
        return messenger.getBinder();
    }

    /**
     * 重写销毁事件
     */
    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMessageHandler.removeCallbacks(updateTask);
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        stopSelf();
        super.onDestroy();
    }

    /**
     * 绑定服务
     *
     * @param msg
     */
    private void handleBind(Message msg) {
        mActivityMessenger = msg.replyTo;

        Message message = Message.obtain();
        message.what = MSG_BIND;
        message.arg1 = ResultCode.OK;
        sendMessageToActivity(message);
    }

    /**
     * 开始播放
     *
     * @param msg
     */
    private void handleStart(Message msg) {
        mCurrentMusicInfo = (MusicInfo) msg.obj;
        create(mCurrentMusicInfo);

        Message message = Message.obtain();
        message.what = MSG_START;
        message.obj = mCurrentMusicInfo;
        message.arg1 = mMediaPlayer.getDuration();
        sendMessageToActivity(message);
    }

    /**
     * 创建播放资源
     *
     * @param musicInfo
     */
    private void create(MusicInfo musicInfo) {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mMediaPlayer = MediaPlayer.create(getApplicationContext(),
                Uri.parse(musicInfo.getPath()));
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
        mMediaPlayer.start();
        mMessageHandler.post(updateTask);
    }

    /**
     * 暂停/播放
     */
    private void handlePause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            removeUpdate();
        } else {
            mMediaPlayer.start();
            mMessageHandler.post(updateTask);
        }
        Message message = Message.obtain();
        message.what = MSG_PAUSE;
        message.obj = mMediaPlayer.isPlaying();
        sendMessageToActivity(message);
    }

    /**
     * 拖动
     *
     * @param msg
     */
    private void handleSeek(Message msg) {
        mMediaPlayer.seekTo(msg.arg1);
        mMessageHandler.post(updateTask);
    }

    /**
     * 切歌
     *
     * @param msg
     */
    private void handleSwitch(Message msg) {
        mCurrentMusicInfo = (MusicInfo) msg.obj;
        create(mCurrentMusicInfo);

        Message message = Message.obtain();
        message.what = MSG_SWITCH;
        message.obj = mCurrentMusicInfo;
        message.arg1 = mMediaPlayer.getDuration();
        sendMessageToActivity(message);
    }

    /**
     * 移除更新
     */
    private void removeUpdate() {
        mMessageHandler.removeCallbacks(updateTask);
    }

    /**
     * 暂停播放并更新状态
     */
    private void handleState() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            handlePause();
        }
    }

    /**
     * 播放结束
     */
    private void stop() {
        Message message = Message.obtain();
        message.what = MSG_STOP;
        sendMessageToActivity(message);
    }

    /**
     * 发送消息到activity
     *
     * @param msg
     */
    private void sendMessageToActivity(Message msg) {
        try {
            mActivityMessenger.send(msg);
        } catch (RemoteException e) {
            switch (msg.what) {
                case MSG_BIND:
                    Log.e(TAG, "sendMessageToActivity: 绑定出错" + e.getMessage());
                    break;
                case MSG_UPDATE:
                    Log.e(TAG, "sendMessageToActivity: 更新出错" + e.getMessage());
                    break;
                case MSG_START:
                    Log.e(TAG, "sendMessageToActivity: 播放出错" + e.getMessage());
                    break;
                case MSG_PAUSE:
                    Log.e(TAG, "sendMessageToActivity: 暂停出错" + e.getMessage());
                    break;
                case MSG_SEEK:
                    Log.e(TAG, "sendMessageToActivity: 拖动出错" + e.getMessage());
                    break;
                case MSG_SWITCH:
                    Log.e(TAG, "sendMessageToActivity: 切歌出错" + e.getMessage());
                    break;
                case MSG_STOP:
                    Log.e(TAG, "sendMessageToActivity: 停止出错" + e.getMessage());
            }
        }
    }

    /**
     * 处理activity消息
     */
    private static class MessageHandler extends Handler {


        private WeakReference<MusicPlayService> mWeakReference;

        private MusicPlayService mMusicPlayService;

        MessageHandler(MusicPlayService musicPlayService) {
            this.mWeakReference = new WeakReference<>(musicPlayService);
            mMusicPlayService = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BIND:
                    mMusicPlayService.handleBind(msg);
                    break;
                case MSG_START:
                    mMusicPlayService.handleStart(msg);
                    break;
                case MSG_PAUSE:
                    mMusicPlayService.handlePause();
                    break;
                case MSG_SEEK:
                    mMusicPlayService.handleSeek(msg);
                    break;
                case MSG_SWITCH:
                    mMusicPlayService.handleSwitch(msg);
                    break;
                case MSG_REMOVE:
                    mMusicPlayService.removeUpdate();
                    break;
                case MSG_STATE:
                    mMusicPlayService.handleState();
            }
        }

    }
}
