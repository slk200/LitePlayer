package org.tizzer.liteplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.entity.VideoInfo;
import org.tizzer.liteplayer.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class VideoListAdapter extends BaseAdapter {
    private List<VideoInfo> videoInfos; //视频列表
    private List<VideoInfo> tempList; //临时列表
    private Context context; //上下文环境

    public VideoListAdapter(Context context) {
        this.videoInfos = new ArrayList<>();
        this.tempList = new ArrayList<>();
        this.context = context;
    }

    /**
     * 增加元素
     *
     * @param videoInfo
     */
    public void addItem(VideoInfo videoInfo) {
        videoInfos.add(videoInfo);
        notifyDataSetChanged();
    }

    /**
     * 增加临时元素
     *
     * @param videoInfo
     */
    public void addTempItem(VideoInfo videoInfo) {
        tempList.add(videoInfo);
    }

    /**
     * 刷新数据
     */
    public void refresh() {
        videoInfos.clear();
        videoInfos.addAll(tempList);
        tempList.clear();
        notifyDataSetChanged();
    }

    /**
     * 移除元素
     *
     * @param videoInfo
     */
    public void removeItem(VideoInfo videoInfo) {
        videoInfos.remove(videoInfo);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return videoInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return videoInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_video_list, null);
            viewHolder = new ViewHolder();
            viewHolder.mTitleView = convertView.findViewById(R.id.tv_video_title);
            viewHolder.mResolutionView = convertView.findViewById(R.id.tv_video_resolution);
            viewHolder.mDurationView = convertView.findViewById(R.id.tv_video_duration);
            viewHolder.mThumbView = convertView.findViewById(R.id.iv_video_thumb);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        VideoInfo videoInfo = videoInfos.get(position);
        viewHolder.mTitleView.setText(videoInfo.getTitle());
        viewHolder.mResolutionView.setText(String.valueOf(videoInfo.getWidth() + "×" + videoInfo.getHeight()));
        viewHolder.mDurationView.setText(TimeUtil.mills2timescale(Integer.parseInt(videoInfo.getDuration()), false));
        viewHolder.mThumbView.setImageBitmap(videoInfo.getThumb());
        return convertView;
    }

    private class ViewHolder {
        ImageView mThumbView;
        TextView mTitleView;
        TextView mResolutionView;
        TextView mDurationView;
    }
}
