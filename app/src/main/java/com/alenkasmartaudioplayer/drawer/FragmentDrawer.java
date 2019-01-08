package com.alenkasmartaudioplayer.drawer;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alenkasmartaudioplayer.R;
import com.alenkasmartaudioplayer.adapters.PlaylistAdapter;
import com.alenkasmartaudioplayer.models.Playlist;
import com.alenkasmartaudioplayer.utils.Constants;

import java.util.ArrayList;




public class FragmentDrawer extends Fragment {

    private static String TAG = FragmentDrawer.class.getSimpleName();

    private ListView lstPlaylist;

    private PlaylistAdapter playlistAdapter;

    public ArrayList<Playlist> playlistArrayList = new ArrayList<Playlist>();

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;

    private View containerView;

    private FragmentDrawerListener drawerListener;

    TextView playerId;

    RelativeLayout rlPlaylistHeader;

    public FragmentDrawer() {

    }

    public static FragmentDrawer newInstance(ArrayList<String> arrPlaylist) {

        FragmentDrawer myFragment = new FragmentDrawer();

        Bundle args = new Bundle();
        args.putStringArrayList(Constants.KEY_PLAYLIST_NAMES_ARRAY,arrPlaylist);
        myFragment.setArguments(args);

        return myFragment;
    }

    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }

    public void setPlaylistArray(ArrayList<Playlist> playlistArray) {
        clearPlaylistArray();
        this.playlistArrayList.addAll(playlistArray);
    }

    public void clearPlaylistArray() {
        this.playlistArrayList.clear();
    }

    public void notifyPlaylistAdapterOfDataSetChange() {
        playlistAdapter.notifyDataSetChanged();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating view layout
        View layout = inflater.inflate(R.layout.fragment_nav_drawer, container, false);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(),getString(R.string.century_font_bold));

        TextView playerIdStatic = (TextView) layout.findViewById(R.id.playerID_static);
        playerIdStatic.setTypeface(font);

        playerId = (TextView) layout.findViewById(R.id.playerID);
        playerId.setTypeface(font);

        TextView playlist = (TextView) layout.findViewById(R.id.playlist);
        playlist.setTypeface(font);

        lstPlaylist = (ListView) layout.findViewById(R.id.lstPlaylist);
        playlistAdapter = new PlaylistAdapter(getActivity(), this.playlistArrayList,true, lstPlaylist);
        lstPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playlistAdapter.currentlyPlayingAt = position;
                FragmentDrawer.this.drawerListener.onDrawerItemSelected(view,position);
            }
        });

        lstPlaylist.setAdapter(playlistAdapter);

        rlPlaylistHeader = (RelativeLayout) layout.findViewById(R.id.playlist_label);

        return layout;
    }

    public void setPlayerId(String id){
        playerId.setText(id);
    }

    public void setVisibilityForPlaylist(boolean shouldSetVisible){

        if (!shouldSetVisible){
            lstPlaylist.setVisibility(View.INVISIBLE);
            rlPlaylistHeader.setVisibility(View.INVISIBLE);
        } else {
            lstPlaylist.setVisibility(View.VISIBLE);
            rlPlaylistHeader.setVisibility(View.VISIBLE);
        }

    }
    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {

        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                //toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

    }

    public void setPlaylistEnabled(boolean isEnabled){
        if (isEnabled){
            lstPlaylist.setEnabled(true);
        } else {
            lstPlaylist.setEnabled(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    public static interface ClickListener {
        public void onClick(View view, int position);

        public void onLongClick(View view, int position);
    }


    public interface FragmentDrawerListener {
        public void onDrawerItemSelected(View view, int position);
    }
}
