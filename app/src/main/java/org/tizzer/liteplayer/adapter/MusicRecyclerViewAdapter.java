package org.tizzer.liteplayer.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.entity.MusicInfo;

import java.util.List;

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.MusicViewHolder> {

    private List<MusicInfo> musicInfos;
    private Context context;
    private OnClickListener onClickListener;

    public MusicRecyclerViewAdapter(Context context, List<MusicInfo> musicInfos, OnClickListener onClickListener) {
        this.musicInfos = musicInfos;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onItemClick(int position, MusicInfo musicInfo);

        void onItemLongClick(int position, MusicInfo musicInfo);
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MusicViewHolder(LayoutInflater.from(context).inflate(R.layout.item_music_list, parent, false));
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        final int index = position;
        final MusicInfo musicInfo = musicInfos.get(index);
        holder.mTitleView.setText(musicInfo.getTitle());
        holder.mArtistAlbumView.setText(String.valueOf(musicInfo.getArtist() + " - " + musicInfo.getAlbum()));
        holder.mDurationView.setText(musicInfo.getDuration());
        holder.mMusicCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(index, musicInfo);
            }
        });
        holder.mMusicCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onClickListener.onItemLongClick(index, musicInfo);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicInfos.size();
    }

    class MusicViewHolder extends RecyclerView.ViewHolder {

        CardView mMusicCard;
        TextView mTitleView;
        TextView mArtistAlbumView;
        TextView mDurationView;

        MusicViewHolder(View itemView) {
            super(itemView);
            mMusicCard = itemView.findViewById(R.id.music_card);
            mTitleView = itemView.findViewById(R.id.tv_music_title);
            mArtistAlbumView = itemView.findViewById(R.id.tv_artist_album);
            mDurationView = itemView.findViewById(R.id.tv_music_duration);
        }
    }
}
