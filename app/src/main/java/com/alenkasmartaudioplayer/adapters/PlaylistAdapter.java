package com.alenkasmartaudioplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alenkasmartaudioplayer.R;
import com.alenkasmartaudioplayer.activities.HomeActivity;
import com.alenkasmartaudioplayer.alarm_manager.PlaylistWatcher;
import com.alenkasmartaudioplayer.models.Playlist;

import java.util.ArrayList;

/**
 * Created by ParasMobile on 6/17/2016.
 */
public class PlaylistAdapter extends BaseAdapter {
    ArrayList<Playlist> playlistModal;
    Context context;
    public int currentlyPlayingAt = 0;
    Typeface font;
    boolean isSideMenu;
    ListView mListView;


    public PlaylistAdapter(Context context, ArrayList<Playlist> playlistModal, boolean isSideMenu, ListView listView) {
        this.context = context;
        this.playlistModal = playlistModal;
        font = Typeface.createFromAsset(this.context.getAssets(),this.context.getString(R.string.century_font));
        this.isSideMenu = isSideMenu;
        this.mListView = listView;
    }

    public void setOnClickListener(AdapterView.OnItemClickListener listener){

        this.mListView.setOnItemClickListener(listener);
    }

    @Override
    public int getCount() {
        return playlistModal.size();
    }

    @Override
    public Object getItem(int position) {
        return playlistModal.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {

        TextView textview;
        ImageView playIcon;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final  ViewHolder holder;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            // get layout from list_item.xml ( Defined Below )
            convertView = inflater.inflate(R.layout.playlist_item, null);
            holder = new ViewHolder();
            holder.textview = (TextView) convertView.findViewById(R.id.grid_item_label);
            holder.textview.setTypeface(font);
            holder.playIcon = (ImageView) convertView.findViewById(R.id.play_icon);
            convertView.setTag(holder);
            convertView.setSelected(true);
            convertView.setPressed(true);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

       final Playlist modal = (Playlist)getItem(position);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                HomeActivity homeActivity = (HomeActivity) context;

                if (homeActivity.arrSongs == null || homeActivity.arrSongs.size() == 0){
                    return;
                }

                String playlistId = homeActivity.arrSongs.get(0).getSpl_PlaylistId();

                if (PlaylistAdapter.this.isSideMenu) {

                    if (modal.getsplPlaylist_Id().equals(playlistId)) {
                        holder.textview.setTextColor(Color.WHITE);
                        holder.playIcon.setImageResource(R.drawable.selected_playlist);
                    } else {
                        holder.textview.setTextColor(Color.WHITE);
                        holder.playIcon.setImageResource(R.drawable.list_icon);
                    }
                }

                else if (!PlaylistAdapter.this.isSideMenu){

                    if (modal.getsplPlaylist_Id().equals(playlistId)){
                        holder.textview.setTextColor(Color.BLACK);
                        holder.playIcon.setImageResource(R.drawable.selected_playlist);
                    } else {
                        holder.textview.setTextColor(Color.DKGRAY);
                        holder.playIcon.setImageResource(R.drawable.list_icon);
                    }
                }
            }
        },1000);

        holder.textview.setText(modal.getsplPlaylist_Name());



        holder.playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ((HomeActivity)context).showDialogToPlayAnyPlaylist(position);

            }
        });

        holder.textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ((HomeActivity)context).showsongsinlist(modal.getsplPlaylist_Id());

            }
        });



        return convertView;
    }

}