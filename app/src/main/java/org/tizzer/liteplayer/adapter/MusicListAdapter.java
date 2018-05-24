package org.tizzer.liteplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.tizzer.liteplayer.R;
import org.tizzer.liteplayer.entity.MusicInfo;

import java.util.List;

public class MusicListAdapter extends BaseAdapter {
    private List<MusicInfo> musicInfos; //音乐列表
    private Context context; //上下文环境

    public MusicListAdapter(Context context, List<MusicInfo> musicInfos) {
        this.musicInfos = musicInfos;
        this.context = context;
    }

    /**
     * 移除元素
     *
     * @param musicInfo
     */
    public void removeItem(MusicInfo musicInfo) {
        musicInfos.remove(musicInfo);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return musicInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return musicInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return musicInfos.get(position).getId();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_music_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mTitleView = convertView.findViewById(R.id.tv_music_title);
            viewHolder.mArtistAlbumView = convertView.findViewById(R.id.tv_artist_album);
            viewHolder.mDurationView = convertView.findViewById(R.id.tv_music_duration);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MusicInfo musicInfo = musicInfos.get(position);
        viewHolder.mTitleView.setText(musicInfo.getTitle());
        viewHolder.mArtistAlbumView.setText(String.valueOf(musicInfo.getArtist() + " - " + musicInfo.getAlbum()));
        viewHolder.mDurationView.setText(musicInfo.getDuration());
        return convertView;
    }

    private class ViewHolder {
        TextView mTitleView;
        TextView mArtistAlbumView;
        TextView mDurationView;
    }
}
