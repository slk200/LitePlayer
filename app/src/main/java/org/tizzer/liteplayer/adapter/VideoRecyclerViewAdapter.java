package org.tizzer.liteplayer.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.entity.VideoInfo;

import java.util.List;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.VideoViewHolder> {

    private List<VideoInfo> videoInfos;
    private Context context;
    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onItemClick(int position, VideoInfo videoInfo);

        void OnItemLongClick(int position, VideoInfo videoInfo);
    }

    public VideoRecyclerViewAdapter(Context context, List<VideoInfo> videoInfos, OnClickListener onClickListener) {
        this.videoInfos = videoInfos;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(context).inflate(R.layout.item_video_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final VideoViewHolder holder, int position) {
        final int index = position;
        final VideoInfo videoInfo = videoInfos.get(index);
        holder.mTitleView.setText(videoInfo.getTitle());
        holder.mResolutionView.setText(videoInfo.getResolution());
        holder.mDurationView.setText(videoInfo.getDuration());
        Glide.with(context)
                .load(videoInfo.getThumb())
                .placeholder(R.drawable.ic_video_placeholder)
                .into(holder.mThumbView);
        holder.mVideoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(index, videoInfo);
            }
        });
        holder.mVideoCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onClickListener.OnItemLongClick(index, videoInfo);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoInfos.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {

        CardView mVideoCard;
        ImageView mThumbView;
        TextView mTitleView;
        TextView mResolutionView;
        TextView mDurationView;

        VideoViewHolder(View itemView) {
            super(itemView);
            mVideoCard = itemView.findViewById(R.id.video_card);
            mTitleView = itemView.findViewById(R.id.tv_video_title);
            mResolutionView = itemView.findViewById(R.id.tv_video_resolution);
            mDurationView = itemView.findViewById(R.id.tv_video_duration);
            mThumbView = itemView.findViewById(R.id.iv_video_thumb);
        }
    }
}
