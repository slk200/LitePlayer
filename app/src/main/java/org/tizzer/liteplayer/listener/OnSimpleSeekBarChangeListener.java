package org.tizzer.liteplayer.listener;

import android.widget.SeekBar;

/**
 * 简化SeekBar监听器
 */
public abstract class OnSimpleSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
