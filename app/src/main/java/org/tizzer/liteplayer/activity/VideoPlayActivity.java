package org.tizzer.liteplayer.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.githang.statusbar.StatusBarCompat;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.fragment.VideoFragment;
import org.tizzer.liteplayer.listener.OnSimpleSeekBarChangeListener;
import org.tizzer.liteplayer.util.TimeUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

public class VideoPlayActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "VideoPlayActivity"; //日志
    private static final int MSG_UPDATE = 0x2001; //更新消息标识
    private static final int CRITICAL_VALUE = 100; //手势操作临界值
    private static final int UPDATE_DELAY = 500; //更新状态的间隔
    private static final int REWIND_FORWARD_PROGRESS = 5000; //快退进跨度
    private static final int LONG_TIME_MAX = 3599999; //(59m 59s 999ms)
    private static final int HIDE_DELAY = 3000; //隐藏间隔
    private static final int FLAG_REWIND = 0; //快退操作
    private static final int FLAG_FORWARD = 1; //快进操作

    protected ImageView mBackView, mPlayView, mOrientationView, mTypeView, mPreviousView, mNextView, mRewindView, mForwardView;
    protected TextView mTitleView, mCurrentPositionView, mTotalPositionView, mProgressView;
    protected LinearLayout mTipLayout, mBottomLayout;
    protected RelativeLayout mTopLayout;
    protected VideoView mVideoView;
    protected ProgressBar mProgressBar;
    protected SeekBar mSeekBar;

    private ArrayList<String> mVideoPathList; //视频列表
    private int currentIndex = -1; //当前播放视频的索引
    private boolean isLongTime = false; //是否是长时间
    private boolean isFullScreen = false; //是否全屏
    private int position; //视频播放位置
    private boolean isPlaying = true; //视频播放状态

    private int screen_width; //屏幕宽度
    private int screen_height; //屏幕高度
    private int currentPosition = -1; //当前播放位置
    private int maxVolume; //最大音量
    private int currentVolume; //当前音量
    private float currentBrightness; //当前亮度
    private boolean isProgress = false; //是否符合手势操作
    private float lastX = 0; //记录上一次手处的X位置
    private float lastY = 0; //记录上一次手处的Y位置

    private AudioManager mAudioManager; //音频管理器
    private GestureDetector mGestureDetector; //手势监测

    private VideoPlayHandler mVideoPlayHandler = new VideoPlayHandler(this);
    private Runnable updateProgressTask = new Runnable() {
        @Override
        public void run() {
            if (mVideoView.isPlaying()) {
                Message message = Message.obtain();
                message.what = MSG_UPDATE;
                message.arg1 = mVideoView.getCurrentPosition();
                mVideoPlayHandler.sendMessage(message);
            }
            mVideoPlayHandler.postDelayed(this, UPDATE_DELAY);
        }
    };
    private Runnable hideOperationTask = new Runnable() {
        @Override
        public void run() {
            mTopLayout.setVisibility(View.GONE);
            mBottomLayout.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFeature();
        setContentView(R.layout.activity_video_play);
        initParams();
        initView();
        initData();
        setOnListener();
    }

    /**
     * 初始化activity特点
     */
    private void initFeature() {
        //隐藏actionbar
        Objects.requireNonNull(getSupportActionBar()).hide();
        //全屏 & 禁止熄屏
        View decorView = getWindow().getDecorView();
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.KEEP_SCREEN_ON;
        decorView.setSystemUiVisibility(flags);
    }

    /**
     * 初始化必要参数
     */
    private void initParams() {
        //获取屏幕尺寸
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screen_width = displayMetrics.widthPixels;
        screen_height = displayMetrics.heightPixels;
        //获取音频管理器
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (mAudioManager != null) {
            maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        //获取屏幕亮度
        currentBrightness = getBrightness();
        Log.e(TAG, "initParams: " + currentBrightness);
        //初始化手势监听器
        mGestureDetector = new GestureDetector(getApplicationContext(), new MyGestureListener());
        //设置状态栏为黑色
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.colorBlack));
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mBackView = findViewById(R.id.iv_back);
        mPlayView = findViewById(R.id.iv_video_play);
        mOrientationView = findViewById(R.id.iv_orientation);
        mTitleView = findViewById(R.id.tv_video_title);
        mCurrentPositionView = findViewById(R.id.tv_currentPosition);
        mTotalPositionView = findViewById(R.id.tv_totalPosition);
        mVideoView = findViewById(R.id.video);
        mSeekBar = findViewById(R.id.seek);
        mTopLayout = findViewById(R.id.ll_top);
        mBottomLayout = findViewById(R.id.ll_bottom);
        mTipLayout = findViewById(R.id.ll_tip);
        mTypeView = findViewById(R.id.iv_type);
        mProgressBar = findViewById(R.id.pb_gress);
        mProgressView = findViewById(R.id.tv_progress);
        mPreviousView = findViewById(R.id.iv_video_previous);
        mNextView = findViewById(R.id.id_video_next);
        mRewindView = findViewById(R.id.iv_video_rewind);
        mForwardView = findViewById(R.id.ic_video_forward);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Intent intent = getIntent();
        mVideoPathList = intent.getStringArrayListExtra(VideoFragment.VIDEO_LIST);
        currentIndex = intent.getIntExtra(VideoFragment.CURRENT_POSITION, 0);
        createVideo(mVideoPathList.get(currentIndex));
    }

    /**
     * 设置监听器
     */
    private void setOnListener() {
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                prepareVideo(mediaPlayer.getDuration());
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new OnSimpleSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPlaying = mVideoView.isPlaying();
                if (isPlaying) {
                    mVideoView.pause();
                    mVideoPlayHandler.removeCallbacks(updateProgressTask);
                }
                mVideoPlayHandler.removeCallbacks(hideOperationTask);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekTo(progress);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPlaying) {
                    mVideoView.start();
                    mVideoPlayHandler.post(updateProgressTask);
                }
                mVideoPlayHandler.postDelayed(hideOperationTask, HIDE_DELAY);
            }
        });

        mBackView.setOnClickListener(this);
        mOrientationView.setOnClickListener(this);
        mPlayView.setOnClickListener(this);
        mPreviousView.setOnClickListener(this);
        mNextView.setOnClickListener(this);
        mRewindView.setOnClickListener(this);
        mForwardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_orientation:
                changeOrientation();
                break;
            case R.id.iv_video_previous:
                previous();
                break;
            case R.id.iv_video_rewind:
                rewindOrForward(FLAG_REWIND);
                break;
            case R.id.id_video_next:
                next();
                break;
            case R.id.ic_video_forward:
                rewindOrForward(FLAG_FORWARD);
                break;
            case R.id.iv_video_play:
                togglePlay();
                break;
        }
    }

    /**
     * 下一部
     */
    private void next() {
        if (currentIndex < mVideoPathList.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        createVideo(mVideoPathList.get(currentIndex));
    }

    /**
     * 上一部
     */
    private void previous() {
        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = mVideoPathList.size() - 1;
        }
        createVideo(mVideoPathList.get(currentIndex));
    }

    /**
     * 快退/快进
     */
    private void rewindOrForward(int flag) {
        mVideoPlayHandler.removeCallbacks(hideOperationTask);
        int progress = 0;
        switch (flag) {
            case FLAG_REWIND:
                progress = mVideoView.getCurrentPosition() - REWIND_FORWARD_PROGRESS;
                break;
            case FLAG_FORWARD:
                progress = mVideoView.getCurrentPosition() + REWIND_FORWARD_PROGRESS;
                break;
        }

        mVideoView.seekTo(progress);
        mSeekBar.setProgress(progress);
        mCurrentPositionView.setText(TimeUtil.mills2timescale(progress, isLongTime));
        mVideoPlayHandler.postDelayed(hideOperationTask, HIDE_DELAY);
    }

    /**
     * VideoView触碰事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        float x = event.getX(); //当前手处X位置
        float y = event.getY(); //当前手处Y位置
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float movedX = x - lastX; //滑动的X距离
                float movedY = y - lastY; //滑动的Y距离
                float absMovedX = Math.abs(movedX); //对距离取绝对值
                float absMovedY = Math.abs(movedY); //对距离取绝对值
                if (absMovedX < CRITICAL_VALUE && absMovedY < CRITICAL_VALUE) {
                    break;
                }
                if (absMovedX < CRITICAL_VALUE && absMovedY > CRITICAL_VALUE) {
                    isProgress = false;
                } else if (absMovedX > CRITICAL_VALUE && absMovedY < CRITICAL_VALUE) {
                    isProgress = true;
                }
                if (!isProgress) {
                    if (x < screen_width / 2) {
                        changeBrightness(-movedY);
                    } else {
                        changeVolume(-movedY);
                    }
                } else {
                    changeProgress(movedX);
                }

                break;
            case MotionEvent.ACTION_UP:
                if (isProgress) {
                    if (isPlaying) {
                        mVideoView.start();
                        mVideoPlayHandler.post(updateProgressTask);
                    }
                    currentPosition = -1;
                } else {
                    currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentBrightness = getWindow().getAttributes().screenBrightness;
                }
                mTipLayout.setVisibility(View.GONE);
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 快进 & 快退
     *
     * @param movedX
     */
    private void changeProgress(float movedX) {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            mVideoPlayHandler.removeCallbacks(updateProgressTask);
        }
        currentPosition = currentPosition == -1 ? mVideoView.getCurrentPosition() : currentPosition;
        int progress = (int) (movedX * 120 / screen_width);
        if (progress > 0) {
            mTypeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_forward));
            mProgressView.setText(String.valueOf("+" + progress + "s"));
        } else {
            mTypeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_rewind));
            mProgressView.setText(String.valueOf(progress + "s"));
        }
        mProgressView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mTipLayout.setVisibility(View.VISIBLE);
        seekTo(currentPosition + progress * 1000);
    }

    /**
     * 改变音量
     *
     * @param movedY
     */
    private void changeVolume(float movedY) {
        int volume = currentVolume + (int) (movedY / (screen_height / maxVolume));
        if (volume > maxVolume) {
            volume = maxVolume;
        } else if (volume < 0) {
            volume = 0;
        }
        mTypeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume));
        mProgressBar.setProgress(volume * 100 / maxVolume);
        mProgressView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mTipLayout.setVisibility(View.VISIBLE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.ADJUST_SAME);
    }

    /**
     * 改变亮度
     *
     * @param movedY
     */
    private void changeBrightness(float movedY) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        float brightness = currentBrightness + movedY / screen_height;
        if (brightness > 1) {
            brightness = 1;
        } else if (brightness < 0) {
            brightness = 0;
        }
        attributes.screenBrightness = brightness;
        mTypeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_brightness));
        mProgressBar.setProgress((int) (brightness * 100));
        mProgressView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mTipLayout.setVisibility(View.VISIBLE);
        getWindow().setAttributes(attributes);
    }

    /**
     * 获取屏幕亮度
     *
     * @return
     */
    private int getBrightness() {
        int brightness = 0;
        try {
            brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightness;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.seekTo(position);
        if (isPlaying) {
            mVideoView.start();
            mPlayView.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_video));
            mVideoPlayHandler.post(updateProgressTask);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPlaying = mVideoView.isPlaying();
        if (isPlaying) {
            mVideoView.pause();
            mVideoPlayHandler.removeCallbacks(updateProgressTask);
            mPlayView.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_video));
        }
        position = mVideoView.getCurrentPosition();
    }

    /**
     * 重写activity的配置变化监听事件
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            swapWithHeight();
            isFullScreen = true;
        } else {
            swapWithHeight();
            isFullScreen = false;
        }
    }

    /**
     * 交换屏幕宽高
     */
    private void swapWithHeight() {
        screen_width = screen_height ^ screen_width;
        screen_height = screen_height ^ screen_width;
        screen_width = screen_height ^ screen_width;
    }

    /**
     * 创建播放资源
     *
     * @param path
     */
    private void createVideo(String path) {
        File file = new File(path);
        mTitleView.setText(file.getName());

        mVideoView.setVideoPath(path);
        mVideoView.start();
    }

    /**
     * 准备播放资源
     *
     * @param duration
     */
    private void prepareVideo(int duration) {
        isLongTime = duration > LONG_TIME_MAX;
        mSeekBar.setMax(duration);
        mTotalPositionView.setText(TimeUtil.mills2timescale(duration, isLongTime));
        mVideoPlayHandler.post(updateProgressTask);
        mVideoPlayHandler.postDelayed(hideOperationTask, HIDE_DELAY);
    }

    /**
     * 改变视频方向
     */
    private void changeOrientation() {
        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    /**
     * 暂停/播放
     */
    private void togglePlay() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            mVideoPlayHandler.removeCallbacks(updateProgressTask);
            mPlayView.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_video));
            isPlaying = false;
        } else {
            mVideoView.start();
            mPlayView.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_video));
            mVideoPlayHandler.post(updateProgressTask);
            isPlaying = true;
        }
    }

    /**
     * 拖动
     *
     * @param progress
     */
    private void seekTo(int progress) {
        mVideoView.seekTo(progress);
        updatePosition(progress);
    }

    /**
     * 更新播放事件和位置
     *
     * @param position
     */
    private void updatePosition(int position) {
        mCurrentPositionView.setText(TimeUtil.mills2timescale(position, isLongTime));
        mSeekBar.setProgress(position);
    }

    /**
     * 播放处理机
     */
    private static class VideoPlayHandler extends Handler {
        private final WeakReference<VideoPlayActivity> mWeakReference;
        private VideoPlayActivity mVideoPlayActivity;

        VideoPlayHandler(VideoPlayActivity videoPlayActivity) {
            this.mWeakReference = new WeakReference<>(videoPlayActivity);
            mVideoPlayActivity = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE:
                    mVideoPlayActivity.updatePosition(msg.arg1);
                    break;
            }
        }
    }

    /**
     * 手势监听器
     */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * 双击事件
         *
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            togglePlay();
            return true;
        }

        /**
         * 单击结束事件
         *
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            int visibility = mBottomLayout.getVisibility();
            if (visibility == View.VISIBLE) {
                mTopLayout.setVisibility(View.GONE);
                mBottomLayout.setVisibility(View.GONE);
                mVideoPlayHandler.removeCallbacks(hideOperationTask);
                mVideoPlayHandler.removeCallbacks(updateProgressTask);
            } else {
                mTopLayout.setVisibility(View.VISIBLE);
                mBottomLayout.setVisibility(View.VISIBLE);
                mVideoPlayHandler.post(updateProgressTask);
                mVideoPlayHandler.postDelayed(hideOperationTask, HIDE_DELAY);
            }
            return true;
        }
    }
}
