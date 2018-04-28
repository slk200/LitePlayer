package org.tizzer.liteplayer.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.entity.MusicInfo;
import org.tizzer.liteplayer.entity.VideoInfo;
import org.tizzer.liteplayer.fragment.MusicFragment;
import org.tizzer.liteplayer.fragment.VideoFragment;
import org.tizzer.liteplayer.listener.OnDeleteListener;
import org.tizzer.liteplayer.listener.OnMusicPauseListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements OnDeleteListener, OnMusicPauseListener {
    public static final int REQUEST_CODE = 0; //权限请求码

    /**
     * 控件
     */
    protected VideoFragment mVideoFragment;
    protected MusicFragment mMusicFragment;
    protected MenuItem item;
    private FragmentManager mFragmentManager; //fragment管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.video);
        checkPermission();
    }

    /**
     * 权限检测
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                requestPermissions(permissions.toArray(new String[0]), REQUEST_CODE);
                return;
            }
        }
        initView();
    }

    /**
     * 请求权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, R.string.permission_message, Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    }
                    initView();
                }
        }
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mVideoFragment = new VideoFragment();
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .add(R.id.fl_container, mVideoFragment)
                .commit();
    }

    /**
     * 删除视频回调
     *
     * @param videoInfo
     */
    @Override
    public void onVideoDelete(VideoInfo videoInfo) {
        mVideoFragment.deleteVideo(videoInfo);
    }

    /**
     * 删除音乐回调
     *
     * @param musicInfo
     */
    @Override
    public void onMusicDelete(MusicInfo musicInfo) {
        mMusicFragment.deleteMusic(musicInfo);
    }

    /**
     * 暂停音乐回调
     */
    @Override
    public void onMusicPause() {
        if (mMusicFragment != null) {
            mMusicFragment.pauseMusic();
        }
    }

    /**
     * 创建菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        item = menu.findItem(R.id.menu_trans);
        return true;
    }

    /**
     * 监听菜单
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_trans:
                if (mMusicFragment == null) {
                    loadMusic(item);
                    break;
                }
                if (mMusicFragment.isHidden()) {
                    transToMusic(item);
                    break;
                }
                transToVideo(item);
        }
        return true;
    }

    /**
     * 重写物理返回键效果
     */
    @Override
    public void onBackPressed() {
        //没有进入过音乐界面则直接退出
        if (mMusicFragment == null) {
            super.onBackPressed();
            return;
        }
        //当前在音乐界面则返回视频界面
        if (mVideoFragment.isHidden()) {
            transToVideo(item);
            return;
        }
        moveTaskToBack(true);
    }

    /**
     * 加载音乐
     *
     * @param item
     */
    private void loadMusic(MenuItem item) {
        setTitle(R.string.music);
        item.setIcon(R.drawable.ic_video);
        item.setTitle(R.string.video);
        mMusicFragment = new MusicFragment();
        mFragmentManager.beginTransaction()
                .add(R.id.fl_container, mMusicFragment)
                .hide(mVideoFragment)
                .commit();
    }

    /**
     * 切换到音乐
     *
     * @param item
     */
    private void transToMusic(MenuItem item) {
        setTitle(R.string.music);
        item.setIcon(R.drawable.ic_video);
        item.setTitle(R.string.video);
        mFragmentManager.beginTransaction()
                .hide(mVideoFragment)
                .show(mMusicFragment)
                .commit();
    }

    /**
     * 切换到视频
     *
     * @param item
     */
    private void transToVideo(MenuItem item) {
        setTitle(R.string.video);
        item.setIcon(R.drawable.ic_music);
        item.setTitle(R.string.music);
        mFragmentManager.beginTransaction()
                .show(mVideoFragment)
                .hide(mMusicFragment)
                .commit();
    }
}
