package org.tizzer.liteplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.entity.VideoInfo;

import java.util.List;

public class VideoListAdapter extends BaseAdapter {
    private List<VideoInfo> videoInfos; //视频列表
    private Context context; //上下文环境

    public VideoListAdapter(Context context, List<VideoInfo> videoInfos) {
        this.videoInfos = videoInfos;
        this.context = context;
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
        viewHolder.mResolutionView.setText(videoInfo.getResolution());
        viewHolder.mDurationView.setText(videoInfo.getDuration());
        Glide.with(context)
                .load(videoInfo.getThumb())
                .placeholder(R.drawable.ic_launcher_background)
                .into(viewHolder.mThumbView);
        return convertView;
    }

    private class ViewHolder {
        ImageView mThumbView;
        TextView mTitleView;
        TextView mResolutionView;
        TextView mDurationView;
    }
}
