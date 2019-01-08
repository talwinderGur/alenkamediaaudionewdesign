package com.alenkasmartaudioplayer.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alenkasmartaudioplayer.R;
import com.alenkasmartaudioplayer.activities.HomeActivity;
import com.alenkasmartaudioplayer.models.Songs;

import java.util.ArrayList;

public class SearchAdapter extends BaseAdapter {
    ArrayList<Songs> SongsModal;
    Context context;
    Typeface font;
    Typeface fontBold;


    public SearchAdapter(Context context, ArrayList<Songs> SongsModal) {
        this.context = context;
        this.SongsModal = SongsModal;
        fontBold = Typeface.createFromAsset(this.context.getAssets(), this.context.getString(R.string.century_font_bold));
        font = Typeface.createFromAsset(this.context.getAssets(), this.context.getString(R.string.century_font));

    }

    @Override
    public int getCount() {
        return SongsModal.size();
    }

    @Override
    public Object getItem(int position) {
        return SongsModal.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public class ViewHolder {

        public TextView textview1, textview2, textview3, textview4;
        public ImageView img1, img2;
        RelativeLayout rlBackground;

    }

   /* @Override
    public boolean isEnabled(int position) {
        return false;
    }*/


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SearchAdapter.ViewHolder holder;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            // get layout from list_item.xml ( Defined Below )
            convertView = inflater.inflate(R.layout.song_list_item, null);

            holder = new ViewHolder();
            holder.textview1 = (TextView) convertView.findViewById(R.id.songtitle);
            holder.textview3 = (TextView) convertView.findViewById(R.id.listsongduration);
            holder.textview2 = (TextView) convertView.findViewById(R.id.songArtist);
            holder.img1 = (ImageView) convertView.findViewById(R.id.music_icon);
            holder.img2 = (ImageView) convertView.findViewById(R.id.recyclebin);

            holder.rlBackground = (RelativeLayout) convertView.findViewById(R.id.background);

            holder.textview1.setTypeface(fontBold);
            holder.textview2.setTypeface(font);
            holder.textview3.setTypeface(font);
            holder.textview1.setTextColor(Color.WHITE);
            holder.textview2.setTextColor(Color.BLUE);
            holder.textview3.setTextColor(Color.WHITE);
            convertView.setTag(holder);

        } else {
            holder = (SearchAdapter.ViewHolder) convertView.getTag();
        }
        final Songs modal = (Songs) getItem(position);

        holder.textview1.setText(modal.getTitle());
        holder.textview2.setText(modal.getAr_Name());
        holder.textview3.setText(modal.getT_Time());

        holder.rlBackground.setBackgroundColor(Color.TRANSPARENT);

        holder.img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((HomeActivity) context).showDialogToPlayAnySong(position);

            }
        });
        holder.img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("listviewdelete", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("TitleidDelete", modal.getTitle_Id());
                editor.putString("TitleDelete", modal.getTitle());
                editor.commit();
                ((HomeActivity) context).deletesongslistview(position);

            }
        });
        return convertView;
    }

}
