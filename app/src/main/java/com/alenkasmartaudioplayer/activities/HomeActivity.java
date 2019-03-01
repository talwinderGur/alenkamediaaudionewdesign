package com.alenkasmartaudioplayer.activities;
import android.support.v7.widget.SearchView;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alenkasmartaudioplayer.R;
import com.alenkasmartaudioplayer.adapters.DialogAdapter;
import com.alenkasmartaudioplayer.adapters.PlaylistAdapter;
import com.alenkasmartaudioplayer.adapters.SearchAdapter;
import com.alenkasmartaudioplayer.adapters.SongAdapter;
import com.alenkasmartaudioplayer.alarm_manager.PlaylistWatcher;
import com.alenkasmartaudioplayer.api_manager.DownloadService;
import com.alenkasmartaudioplayer.api_manager.OkHttpUtil;
import com.alenkasmartaudioplayer.application.AlenkaMedia;
import com.alenkasmartaudioplayer.database.MySQLiteHelper;
import com.alenkasmartaudioplayer.database.SongsDataSource;
import com.alenkasmartaudioplayer.drawer.FragmentDrawer;
import com.alenkasmartaudioplayer.exomediaplayer.EncryptedFileDataSourceFactory;
import com.alenkasmartaudioplayer.exomediaplayer.FadingMediaPlayer;
import com.alenkasmartaudioplayer.interfaces.DownloadListener;
import com.alenkasmartaudioplayer.interfaces.PlaylistLoaderListener;
import com.alenkasmartaudioplayer.mediamanager.AdvertisementsManager;
import com.alenkasmartaudioplayer.mediamanager.PlayerStatusManager;
import com.alenkasmartaudioplayer.mediamanager.PlaylistManager;
import com.alenkasmartaudioplayer.models.Advertisements;
import com.alenkasmartaudioplayer.models.PlayerStatus;
import com.alenkasmartaudioplayer.models.Playlist;
import com.alenkasmartaudioplayer.models.Songs;
import com.alenkasmartaudioplayer.utils.AlenkaMediaPreferences;
import com.alenkasmartaudioplayer.utils.ConnectivityReceiver;
import com.alenkasmartaudioplayer.utils.Constants;
import com.alenkasmartaudioplayer.utils.FileUtil;
import com.alenkasmartaudioplayer.utils.MediaPlayerUtils;
import com.alenkasmartaudioplayer.utils.NetworkUtil;
import com.alenkasmartaudioplayer.utils.SharedPreferenceUtil;
import com.alenkasmartaudioplayer.utils.StorageUtils;
import com.alenkasmartaudioplayer.utils.Utilities;
import com.crashlytics.android.Crashlytics;
import com.devbrackets.android.exomedia.core.video.scale.ScaleType;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;


import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.alenkasmartaudioplayer.utils.MediaPlayerUtils.CROSSFADE_DURATION;

/**
 * Created by love on 30/5/17.
 */
public class HomeActivity extends AppCompatActivity implements DownloadListener, OkHttpUtil.OkHttpResponse,
        PlaylistWatcher.PlaylistStatusListener, FragmentDrawer.FragmentDrawerListener,
        View.OnClickListener {

    public static final String CALL_INFO_WORKER = "Call worker";

    public static final String TAG = "HomeActivity";

    private static final int VIDEO_VIEW_TAG = 1;

    private static final int VIDEO_AD_VIEW_TAG = 2;


   public Dialog pickerDialog;
    public Dialog pickerDialog1;

    private DownloadService mDownloadService;

    private boolean mIsBound;

    //private ProgressBar circularProgressBar;

    private ListView lvPlaylist;

    private ListView lvSongs;
    private ListView lvSongsDialog;
    private ListView lvSearch;

    private PlaylistAdapter playlistAdapter;

    private SongAdapter songAdapter;
    private SearchAdapter searchAdapter;
    private DialogAdapter dialogAdapter;

    private ArrayList<Playlist> arrPlaylists = new ArrayList<Playlist>();

    public ArrayList<Songs> arrSongs = new ArrayList<Songs>();

    public ArrayList<Songs> arrSearch = new ArrayList<Songs>();
    public ArrayList<Songs>arrDialog=new ArrayList<Songs>();


    private ArrayList<Advertisements> arrAdvertisements = new ArrayList<Advertisements>();

    private int currentlyPlayingSongAtIndex = 0;
    private int deletePlayingSongAtIndex=0;

    public static int currentlyPlayingAdAtIndex = -1;

    private VideoView videoView;

    private VideoView videoViewAds;

    private PlaylistWatcher alarm;

//    Handler checkForPlaylistStatus   = new Handler();

    int delay = 1000; //milliseconds

    private int currentPlaylistStatus = -2;

    private boolean doubleBackToExitPressedOnce;

    IntentFilter intentFilter = new IntentFilter(Constants.ALARM_ACTION);

    IntentFilter intentConnectivity = new IntentFilter(Constants.CONNECTIVITY_CHANGED);

    BroadcastReceiver broadcastReceiver;

    BroadcastReceiver networkChangeReceiver;

    boolean shouldPlaySoftStopAd = false;


    private String sdCardLocation = "";

    private ArrayList<String> arrVideoFiles = new ArrayList<>();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /********countOfTotalSongsToBeDownloaded********
     This variable is used for updating the count of
     songs which are downloaded every 15 minutes. It stores
     the total number of songs which are to be downloaded.
     /********countOfTotalSongsToBeDownloaded********/

    private int countOfTotalSongsToBeDownloaded = 0;

    /*********************Broadcast Receiver Starts**************************/



    /*********************Broadcast Receiver Ends**************************/

    /***********************Download Videos Service Methods Start****************************************/

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDownloadService = ((DownloadService.LocalBinder)service).getService();
            mDownloadService.registerListener(HomeActivity.this);
        }
        public void onServiceDisconnected(ComponentName className) {
            mDownloadService = null;
        }
    };
    private String targetFileName;
    private String sourceFileLocation;
    private int EXPORT_VIDEO_INDEX = 0;
    //private TextView txtFileWriter;

    private CountDownTimer mCountDownTimer;
    boolean isCountDownTimerRunning = false;

    AlertDialog.Builder dialogBuilder = null;

    AlertDialog dialog;

    @BindView(R.id.fragment_navigation_drawer)
    FragmentDrawer drawerFragment;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    TextView txtSongTitle;
    TextView NextxtSongTitle;

    ImageButton btnNextsong;
    //ImageButton btnplay;
    SearchView searchview;

    TextView txtSongArtist;
    TextView NexttxtSongArtist;

    Toolbar playerToolbar;

    LinearLayout layoutProgress;

    ProgressBar pbProcessing;

    TextView txtProgressPercent;
    TextView txtDownload;

    TextView txtCurrentDuration;
    TextView txtTotalDuration;
    TextView txtListDuration;

    SeekBar seekBar;

    ImageButton btnPlayPause;

    ImageButton btnStop;
    ImageButton btnImghelp;
    public ImageView btnVolume;

    RelativeLayout rlNoPlaylist;

    SimpleExoPlayerView simpleExoPlayer;

    Typeface fontNormal;
    Typeface fontBold;

    /**
     * currentPlaylist is used to check if the fading is active.
     */
    Playlist currentPlaylist;

    private int REQUEST_CODE_STORAGE_SELECTOR = 42;
    private int REQUEST_CODE_STORAGE_FOLDER_SELECTOR = 43;

    private int playNextSongIndex = -1;

    private FadingMediaPlayer fadingMediaPlayer;


    void doBindService() {
        bindService(new Intent(HomeActivity.this,
                DownloadService.class), mConnection, 0);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            if (mDownloadService != null) {
                mDownloadService.unregisterListener(this);
            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private void setToolbarAsPlayer() {

       playerToolbar = (Toolbar) findViewById(R.id.tool);
        txtSongTitle = (TextView) findViewById(R.id.title);
        NextxtSongTitle =(TextView) findViewById(R.id.text);

        txtSongArtist = (TextView) findViewById(R.id.artist);
        NexttxtSongArtist = (TextView) findViewById(R.id.NextsongArtist);
        txtCurrentDuration = (TextView) findViewById(R.id.current_duration);
        txtTotalDuration = (TextView) findViewById(R.id.total_duration);
        txtDownload=(TextView) findViewById(R.id.download);
        txtListDuration = (TextView) findViewById(R.id.listsongduration);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
//        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        btnNextsong = (ImageButton) findViewById(R.id.next);
        btnPlayPause = (ImageButton) findViewById(R.id.playPause);
        btnImghelp= (ImageButton) findViewById(R.id.helpimg);
        searchview = (SearchView) findViewById(R.id.search);
        searchview.setQueryHint("Enter search");
        btnStop = (ImageButton) findViewById(R.id.stop);
        btnVolume = (ImageView) findViewById(R.id.volume);
        rlNoPlaylist = (RelativeLayout) findViewById(R.id.layoutNoPlaylist);

        btnPlayPause.setOnClickListener(this);
        btnVolume.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnNextsong.setOnClickListener(this);
        btnImghelp.setOnClickListener(this);
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
            public boolean onQueryTextSubmit(String query) {
               arrSearch.clear();
                ArrayList<Songs> searchque = new MySQLiteHelper(HomeActivity.this).searchDictionaryWords(query);
                arrSearch.addAll(searchque);
                if(arrSearch.size()>0) {
                    searchAdapter = new SearchAdapter(HomeActivity.this, arrSearch);
                    lvSearch.setAdapter(searchAdapter);
                    lvSongs.setVisibility(View.GONE);
                    lvSearch.setVisibility(View.VISIBLE);
                }
                else {
                    searchNotFound();
                }

               return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
              return false;
            }



        });
        searchview.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {

                getsongsafterdelete(arrPlaylists.get(0));
                arrSongs.size();
                lvSongs.setVisibility(View.VISIBLE);
                arrSearch.clear();
                lvSearch.setVisibility(View.GONE);
                return false;
            }
        });
        simpleExoPlayer = (SimpleExoPlayerView) findViewById(R.id.simpleExoPlayer);

        setSupportActionBar(playerToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


    }

    @Override
    public void refreshPlayerControls() {

        Handler handler = new Handler(HomeActivity.this.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (videoView != null && videoView.isPlaying()){

                    long timeRemaining = videoView.getDuration() - videoView.getCurrentPosition();

                    txtTotalDuration.setText(""
                            + Utilities.milliSecondsToTimer(timeRemaining));
                    txtCurrentDuration.setText(""
                            + Utilities.milliSecondsToTimer(videoView.getCurrentPosition()));

                    seekBar.setProgress((int) videoView.getCurrentPosition());
                    seekBar.setMax((int) videoView.getDuration());

                    int remainingTimeInSeconds = (int) (timeRemaining / 1000);

                        if (remainingTimeInSeconds < CROSSFADE_DURATION/1000){

                            boolean isAdvertisementPending = isAdvertisementPendingTobePlayed();

                            if (((int)videoView.getTag()) == VIDEO_VIEW_TAG && currentPlaylist.getIsFadingActive() == 1
                                    && !isAdvertisementPending){

                            fadeOutVolume();
                        }
                    }
                }
            }
        });
    }

    private boolean isAdvertisementPendingTobePlayed() {

        boolean isAdvertisementPending = false;

        if (arrAdvertisements.size() > 0){

            String advertisementTypeSong =  SharedPreferenceUtil.getStringPreference(HomeActivity.this,
                    AlenkaMediaPreferences.is_song_Adv);

            if (advertisementTypeSong.equals("1")){

                if (PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER == PlaylistWatcher.PLAY_AD_AFTER_SONGS){
                    isAdvertisementPending = true;
                }
            }
        }
        return isAdvertisementPending;
    }

    boolean isFadeInProgress;

    private void fadeOutVolume() {

        if(!isFadeInProgress){

            isFadeInProgress = true;

            int nextSongIndex = 0;

            if (playNextSongIndex >= 0){
                nextSongIndex = playNextSongIndex;

            }else if (arrSongs.size() - 1 > currentlyPlayingSongAtIndex){
                nextSongIndex = currentlyPlayingSongAtIndex + 1;
            }

            Songs nextSongToFadeIn = arrSongs.get(nextSongIndex);

            fadingMediaPlayer = new FadingMediaPlayer(HomeActivity.this,
                    new FadingMediaPlayer.FadingMediaPlayerListener() {
                        @Override
                        public void fadingMediaPlayerCompleted(long currentPosition) {
                            videoView.setVolume(1);
                            videoView.seekTo(currentPosition);
                            isFadeInProgress = false;
                            fadingMediaPlayer = null;
                        }
                    });
            MediaPlayerUtils.fadeOutForVideoPlayer(videoView,null,this);
            fadingMediaPlayer.load(nextSongToFadeIn.getSongPath());
            fadingMediaPlayer.play();
        }
    }


    @Override
    public void onUpdate(final long value) {
        Handler handler = new Handler(HomeActivity.this.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (layoutProgress.getVisibility() == View.GONE){
                    layoutProgress.setVisibility(View.VISIBLE);
                }

                pbProcessing.setProgress((int) value);
                txtProgressPercent.setText((int) value + "%");
        /*When a song is being downloaded its progress is shown here.*/
                Log.e("Download Status","" + value);
                txtDownload.setVisibility(View.VISIBLE);
                //txtFileWriter.setText("Copying song " + currentSong + " of " + totalSongs);
            }

        });
       // circularProgressBar.setProgress((int) value);

    }

    @Override
    public void downloadCompleted(boolean shouldPlay, Songs song) {

        Handler handler = new Handler(HomeActivity.this.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (layoutProgress.getVisibility() == View.VISIBLE){
                    layoutProgress.setVisibility(View.GONE);
                    if(songAdapter!=null) {
                       songAdapter.notifyDataSetChanged();
                   }

                    int totalSongs = new PlaylistManager(HomeActivity.this,null).getTotalDownloadedSongs();
                    int countSongs=new PlaylistManager(HomeActivity.this,null).getTotalSongs();
                    if (totalSongs >= 0) {
                        txtDownload.setText("Songs Downloaded:"+totalSongs+"/"+countSongs);
                    }
                    if(arrSongs.size()-1>currentlyPlayingSongAtIndex) {
                        NextxtSongTitle.setText(arrSongs.get(currentlyPlayingSongAtIndex + 1).getTitle());
                        NexttxtSongArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex + 1).getAr_Name());
                    }
                    else{
                        NextxtSongTitle.setText(arrSongs.get(0).getTitle());
                        NexttxtSongArtist.setText(arrSongs.get(0).getAr_Name());
                    }
                    txtDownload.setVisibility(View.GONE);
                }
            }
        });



        /*A case where current time has no playlist but after one hour or so there is playlist
        * and the songs are being downloaded. After the first song finishes download shouldPlay will be true
        * and also the videoView will not be playing. Prevent video view from playing in this case.*/

        if (shouldPlay){

            /*If video view is not playing then only we start the player*/
            if (!videoView.isPlaying()){
                getPlaylistsForCurrentTime();
                return;
            }
        }

        if (arrSongs.size() > 0){

            String downloadedSongPlaylistId = song.getSpl_PlaylistId();
            String currentPlayingPlaylistId = arrSongs.get(0).getSpl_PlaylistId();

            if (downloadedSongPlaylistId.equals(PlaylistWatcher.currentPlaylistID)){
//                Toast.makeText(HomeActivity.this, "Downloaded and added song" + song.getTitle(), Toast.LENGTH_SHORT).show();
                arrSongs.add(song);
                updateSideMenuPlaylist();
            }
        }
    }

    @Override
    public void advertisementDownloaded(Advertisements advertisements) {

        Handler handler = new Handler(HomeActivity.this.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (layoutProgress.getVisibility() == View.VISIBLE){
                    layoutProgress.setVisibility(View.GONE);
                }
            }
        });

        if (advertisements != null){

            String advType = SharedPreferenceUtil.getStringPreference(HomeActivity.this,AlenkaMediaPreferences.is_Time_Adv);

            if (advType.equals("1")){

                if (advertisements.getStart_Adv_Time_Millis() >= System.currentTimeMillis()){

                    arrAdvertisements.add(advertisements);
                }

            } else {
                arrAdvertisements.add(advertisements);
            }
        }
    }

    @Override
    public void startedCopyingSongs(final int currentSong,final int totalSongs,final boolean isFinished) {

        Handler handler = new Handler(HomeActivity.this.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isFinished){
                    //txtFileWriter.setText("Copy successful");

                    alarm = new PlaylistWatcher();
                    alarm.setContext(HomeActivity.this);
                    alarm.setPlaylistStatusListener(HomeActivity.this);
                    alarm.setWatcher();

                    getPlaylistsForCurrentTime();
                    getAdvertisements();

                    /*PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
                    playerStatusManager.songsDownloaded = "" + songsThatHaveBeenDownloaded;
                    playerStatusManager.updateDownloadedSongsCountOnServer();*/

                    return;
                }
                //txtFileWriter.setText("Copying song " + currentSong + " of " + totalSongs);
            }
        });


        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (isFinished){
                    txtFileWriter.setText("Copy successful");
                    return;
                }
                txtFileWriter.setText("Copying song " + currentSong + " of " + totalSongs);
            }
        });*/
    }

    @Override
    public void finishedDownloadingSongs(int totalSongs) {

        if (totalSongs > 0){

            PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
            playerStatusManager.songsDownloaded = "" + totalSongs;
//            playerStatusManager.updateDownloadedSongsCountOnServer();

        } else {

            if (AlenkaMedia.getInstance().isUpdateInProgress){

                Log.e(TAG,"New songs downloaded now restarting");

               PlaylistManager playlistManager = new PlaylistManager(HomeActivity.this,playlistLoaderListener);
                playlistManager.publishTokenForUpdatedData();

            }
        }

    }


    public void showsongsinlist(String playlistindex) {

        pickerDialog1 = new Dialog(HomeActivity.this);
        pickerDialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pickerDialog1.setContentView(R.layout.custom_alert_dialog_playlist_song);
        lvSongsDialog = (ListView) pickerDialog1.findViewById(R.id.listViewCustomDialog);
        ImageButton btnCancel1 = (ImageButton) pickerDialog1.findViewById(R.id.btnCancel1);
        pickerDialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (arrDialog.size() > 0) arrDialog.clear();
        ArrayList<Songs> songs = new PlaylistManager(HomeActivity.this, null).getSongsForPlaylist(playlistindex);
        if (songs != null && songs.size() > 0) {
            arrDialog.addAll(songs);
        }
        if (arrDialog.size() > 0) {
            dialogAdapter = new DialogAdapter(HomeActivity.this, arrDialog);
            lvSongsDialog.setAdapter(dialogAdapter);

        }
        pickerDialog1.show();
        btnCancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog1.dismiss();
            }
        });

    }

    private void showDialogForExportDone(){

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        builder.setTitle("Songs copied from external storage.");

        builder.setCancelable(false);
// Set up the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    @Override
    public void showOfflineDownloadingAlert() {

    }


    /***********************Download Videos Service Methods Ends****************************************/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /* WorkManager workManager = WorkManager.getInstance();
        List<WorkStatus> value = workManager.getStatusesByTag(CALL_INFO_WORKER).getValue();
        if (value == null) {
            PeriodicWorkRequest callDataRequest = new PeriodicWorkRequest.Builder(ApplicationRunningChecker.class,
                    5, TimeUnit.MINUTES)
                    .addTag(CALL_INFO_WORKER)
                    .build();
            workManager.enqueueUniquePeriodicWork(CALL_INFO_WORKER, ExistingPeriodicWorkPolicy.REPLACE,callDataRequest);
        }*/

        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        //logUser();

        //enableScreenLogging();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        fontNormal = Typeface.createFromAsset(this.getAssets(),this.getString(R.string.century_font));
        fontBold = Typeface.createFromAsset(this.getAssets(),this.getString(R.string.century_font_bold));

        //circularProgressBar = (ProgressBar) findViewById(R.id.circularProgress);
        lvPlaylist = (ListView) findViewById(R.id.listViewPlaylists);
        lvSearch= (ListView) findViewById(R.id.listViewSearch);
        lvSongs = (ListView) findViewById(R.id.listViewSongs);

        lvSongs.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
        videoView = (VideoView)findViewById(R.id.video_view);

        setToolbarAsPlayer();

        layoutProgress = (LinearLayout) findViewById(R.id.layoutProgress);
        pbProcessing = (ProgressBar) findViewById(R.id.pbProcessing);
        txtProgressPercent = (TextView) findViewById(R.id.txtProgressPercent);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), playerToolbar);
        drawerFragment.setDrawerListener(this);
        drawerFragment.setPlayerId(SharedPreferenceUtil.getStringPreference(HomeActivity.this, Constants.TOKEN_ID));
        playerToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        disableControls();

        videoView.setTag(VIDEO_VIEW_TAG);

        setLockControls();

        videoView.setOnPreparedListener(videoViewPreparedListener);
        videoView.setOnCompletionListener(videoViewCompletionListener);
        videoView.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(Exception e) {

                Utilities.showToast(HomeActivity.this, e.getMessage());

                e.printStackTrace();

                return false;
            }
        });
        videoView.getCurrentPosition();
        videoView.setVisibility(View.GONE);
        videoView.setScaleType(ScaleType.NONE);
        videoView.showControls();
        videoView.setHandleAudioFocus(false);


        ArrayList<Songs> songs = getSongsToBeDownloaded();

        ArrayList<Advertisements> ads = getAdvertisementsToBeDownloaded();



        if (songs != null && songs.size() > 0 ||
                ads != null && ads.size() > 0){

            boolean isStorageAlertShownOnce = SharedPreferenceUtil.getBooleanPreference(HomeActivity.this,Constants.STORAGE_ALERT_SHOWN_ONCE,false);

            isStorageAlertShownOnce = true;

            if (!isStorageAlertShownOnce){

                SharedPreferenceUtil.setBooleanPreference(HomeActivity.this,Constants.STORAGE_ALERT_SHOWN_ONCE,true);
                showAlertDialogForStorageSelection();

                Utilities.showToast(HomeActivity.this,"Auto downloading songs in 1 minute." );

                mCountDownTimer = new CountDownTimer(60000,1000) {
                    @Override
                    public void onTick(long l) {

                        isCountDownTimerRunning = true;
                        Log.e(TAG,"seconds remaining: " + l / 1000);
                    }

                    @Override
                    public void onFinish() {

                        isCountDownTimerRunning = false;
                        if (dialog != null){
                            dialog.dismiss();
                        }

                        if (!AlenkaMedia.getInstance().isDownloadServiceRunning){

                            startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                            doBindService();
                        }

                        alarm = new PlaylistWatcher();
                        alarm.setContext(HomeActivity.this);
                        alarm.setPlaylistStatusListener(HomeActivity.this);
                        alarm.setWatcher();

                        getPlaylistsForCurrentTime();
                        getAdvertisements();

                    }
                };
                mCountDownTimer.start();

            } else {

                if (!AlenkaMedia.getInstance().isDownloadServiceRunning){

                 /*   if (Utilities.isVersionLowerThanLollipop()){

                    } else {
                        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_STORAGE_SELECTOR);


                    }*/


                    startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                    doBindService();
                }

                alarm = new PlaylistWatcher();
                alarm.setContext(HomeActivity.this);
                alarm.setPlaylistStatusListener(HomeActivity.this);
                alarm.setWatcher();

                getPlaylistsForCurrentTime();
                getAdvertisements();

            }

            if (songs != null)
            countOfTotalSongsToBeDownloaded = songs.size();

        } else {

            alarm = new PlaylistWatcher();
            alarm.setContext(HomeActivity.this);
            alarm.setPlaylistStatusListener(HomeActivity.this);
            alarm.setWatcher();

            getPlaylistsForCurrentTime();
            getAdvertisements();
        }

//        saveLogcatToFile(HomeActivity.this);
    }


    private void sendLastCrashLog() {

        try{

            String json =  SharedPreferenceUtil.getStringPreference(HomeActivity.this, Constants.CRASH_MESSAGE);

            if (json != null){

                JSONObject jsonObject = new JSONObject(json);
                new OkHttpUtil(HomeActivity.this, Constants.UPDATE_CRASH_LOG, jsonObject.toString(), new OkHttpUtil.OkHttpResponse() {
                    @Override
                    public void onResponse(String response, int tag) {

                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.has("Response")){

                                String status = jsonObject.getString("Response");

                                if (status.equalsIgnoreCase("1")){

                                    SharedPreferenceUtil.removeStringPreference(HomeActivity.this,Constants.CRASH_MESSAGE);
                                }
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception e, int tag) {
                        e.printStackTrace();
                    }
                },false,Constants.UPDATE_CRASH_LOG_TAG).execute();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar,final int i, boolean b) {

            if (((int)videoView.getTag()) == VIDEO_VIEW_TAG){

                videoView.seekTo(i);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    private void setLockControls(){


        try {
            int isStopcontrol = Integer.parseInt(SharedPreferenceUtil.getStringPreference(HomeActivity.this,AlenkaMediaPreferences.Is_Stop_Control));
//            isStopcontrol = 1;

            if (isStopcontrol == 1) {
                lvSongs.setVisibility(View.GONE); //hide songs listing
                lvPlaylist.setVisibility(View.VISIBLE); //show playlist listing on main screen
                drawerFragment.setVisibilityForPlaylist(false); //hide playlist listing from side menu
                btnPlayPause.setVisibility(View.INVISIBLE); //hide play/pause button
            } else {
                lvSongs.setVisibility(View.VISIBLE);
                lvPlaylist.setVisibility(View.GONE);
                drawerFragment.setVisibilityForPlaylist(true);
                btnPlayPause.setVisibility(View.VISIBLE);
            }

        }catch (Exception e){
            e.printStackTrace();
        }



    }
    private ArrayList<Songs> getSongsToBeDownloaded(){

        ArrayList<Playlist> playlists = new PlaylistManager(HomeActivity.this, null).getPlaylistFromLocallyToBedDownload();

        List<Playlist> noRepeat = new ArrayList<Playlist>();

        for (Playlist event : playlists) {

            boolean isFound = false;
            // check if the event name exists in noRepeat
            for (Playlist e : noRepeat) {
                if (e.getsplPlaylist_Id().equalsIgnoreCase(event.getsplPlaylist_Id()))
                isFound = true;
            }

            if (!isFound) {
                noRepeat.add(event);
            }
        }

        ArrayList<Songs> songsToBeDownloaded = null;

        if (noRepeat.size() > 0) {

            PlaylistManager songsLoader = new PlaylistManager(HomeActivity.this, null);

            songsToBeDownloaded = new ArrayList<>();

            for (Playlist playlist : noRepeat) {

                ArrayList<Songs> songs = songsLoader.getSongsThatAreNotDownloaded(playlist.getsplPlaylist_Id());

                if (songs != null && songs.size() > 0) {

                    if (playlist.getIsSeparatinActive() == 0){
                        sort(songs);
                    }

                    songsToBeDownloaded.addAll(songs);
                }
            }
            songsLoader = null;

            if (songsToBeDownloaded.size() > 0) {
                return songsToBeDownloaded;
            }
        }
        return null;
    }

    private ArrayList<Advertisements> getAdvertisementsToBeDownloaded(){
        return new AdvertisementsManager(this).
                getAdvertisementsToBeDownloaded();
    }

    private void getAdvertisements() {

        ArrayList<Advertisements> advertisements = new AdvertisementsManager(HomeActivity.this).
                getAdvertisementsThatAreDownloaded();

        String advType = SharedPreferenceUtil.getStringPreference(HomeActivity.this,AlenkaMediaPreferences.is_Time_Adv);

        if (advType.equals("1")){

            if (advertisements != null && advertisements.size() > 0) {

                for (Advertisements ad :advertisements) {

                    /*Add only those advertisements whose end time is greater than current time.*/
                    if (ad.getStart_Adv_Time_Millis() >= System.currentTimeMillis()) {
                        arrAdvertisements.add(ad);
                    }
                }
            }

        } else {
            if (advertisements != null && advertisements.size() > 0){
                arrAdvertisements.addAll(advertisements);
            }
        }
    }

    OnCompletionListener videoViewCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion() {

            if (arrAdvertisements.size() > 0){

                if ((Integer)videoView.getTag() == VIDEO_AD_VIEW_TAG){
                    PlaylistWatcher.ADVERTISEMENT_TIME_COUNTER = 0;
                    shouldPlaySoftStopAd = false;
                    PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER = 0;
                }

                String advertisementTypeSong =  SharedPreferenceUtil.getStringPreference(HomeActivity.this,
                        AlenkaMediaPreferences.is_song_Adv);

                String advertisementTypeMinute =  SharedPreferenceUtil.getStringPreference(HomeActivity.this,
                        AlenkaMediaPreferences.is_Minute_Adv);

                String advertisementTypeTime =  SharedPreferenceUtil.getStringPreference(HomeActivity.this,
                        AlenkaMediaPreferences.is_Time_Adv);


                if (advertisementTypeSong.equals("1")){

                    if (PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER == PlaylistWatcher.PLAY_AD_AFTER_SONGS){

//                        Toast.makeText(HomeActivity.this, "Should play ad from Home", Toast.LENGTH_SHORT).show();
                        setVisibilityAndPlayAdvertisement();

                        return;
                    }
                }  else if(advertisementTypeMinute.equals("1")){

                    if (shouldPlaySoftStopAd){

//                        Toast.makeText(HomeActivity.this, "Should play soft stop ad", Toast.LENGTH_SHORT).show();

                        setVisibilityAndPlayAdvertisement();

                        return;
                    }
                }  else if(advertisementTypeTime.equals("1")){

                    if (shouldPlaySoftStopAd){

                        setVisibilityAndPlayAdvertisement();

                        return;
                    }
                }
            }

            if (playNextSongIndex >= 0){
                if (currentPlaylist.getIsFadingActive() == 1){
                    videoView.setVolume(0);
                }
                playNextSong();
            }
            else {

                if (currentPlaylist.getIsFadingActive() == 1){
                    videoView.setVolume(0);
                }
                playSong();
            }

        }
    };

    private void playSong(){

          /*If song played was at last index then restart the playlist.*/

        if (arrSongs.size() - 1 > currentlyPlayingSongAtIndex){
            currentlyPlayingSongAtIndex++;
        } else {
            currentlyPlayingSongAtIndex = 0;
        }

        PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER++;

        videoView.setTag(VIDEO_VIEW_TAG);

        Handler mHandler = new Handler(getMainLooper());

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txtSongTitle.setText(arrSongs.get(currentlyPlayingSongAtIndex).getTitle());
                txtSongArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex).getAr_Name());

                if(arrSongs.size()-1>currentlyPlayingSongAtIndex) {
                    NextxtSongTitle.setText(arrSongs.get(currentlyPlayingSongAtIndex + 1).getTitle());
                    NexttxtSongArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex + 1).getAr_Name());
                }
                else{
                    NextxtSongTitle.setText(arrSongs.get(0).getTitle());
                    NexttxtSongArtist.setText(arrSongs.get(0).getAr_Name());
                }
                setVideoPath(arrSongs.get(currentlyPlayingSongAtIndex).getSongPath());

            }
        });

    }

    private void playNextSong(){

        PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER++;

        videoView.setTag(VIDEO_VIEW_TAG);

        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txtSongTitle.setText(arrSongs.get(playNextSongIndex).getTitle());
                txtSongArtist.setText(arrSongs.get(playNextSongIndex).getAr_Name());
                NextxtSongTitle.setText(arrSongs.get(currentlyPlayingSongAtIndex+1).getTitle());
                NexttxtSongArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex+1).getAr_Name());

                setVideoPath(arrSongs.get(playNextSongIndex).getSongPath());
            }
        });
    }

    private void  setVideoPath(String path){

        try {

            String username = "bob@google.org";
            String password = "Password1";
            String secretID = "BlahBlahBlah";
            String SALT2 = "deliciously salty";

            byte[] key = (SALT2 + username + password).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit


            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher decipher = Cipher.getInstance("AES");
            decipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] iv = new byte[16];
            IvParameterSpec mIvParameterSpec = new IvParameterSpec(iv);

            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            LoadControl loadControl = new DefaultLoadControl();
            SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

            simpleExoPlayer.setPlayer(player);
            DataSource.Factory dataSourceFactory = new EncryptedFileDataSourceFactory(decipher, secretKeySpec, null, bandwidthMeter);
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            try {

                Uri uri = Uri.fromFile(new File(path));
                MediaSource videoSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
               // player.prepare(videoSource);
                //player.setPlayWhenReady(true);
                videoView.setVideoURI(uri,videoSource);
            } catch (Exception e) {
                e.printStackTrace();
            }


            //videoView.setVideoPath(path);
        }catch (Exception e){
            e.printStackTrace();
            playSong();
        }
    }

    private void updateSideMenuPlaylist(){

        if (drawerFragment == null){
            drawerFragment = (FragmentDrawer)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        }

        ArrayList<Playlist> playlists = new PlaylistManager(HomeActivity.this,null).getAllPlaylistInPlayingOrder();

        if (playlists != null && playlists.size() > 0){

            drawerFragment.setPlaylistArray(playlists);
            drawerFragment.notifyPlaylistAdapterOfDataSetChange();
        }

    }

    private void clearSideMenuItems(){

        if (drawerFragment == null){
            drawerFragment = (FragmentDrawer)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        }
        drawerFragment.clearPlaylistArray();
    }

    private void setVisibilityAndPlayAdvertisement(){

        if (currentlyPlayingAdAtIndex < 0){ // If as is playing for the first time.
            currentlyPlayingAdAtIndex = 0;

        } else if(currentlyPlayingAdAtIndex == arrAdvertisements.size() - 1){ // If ad playing is at the last index
            currentlyPlayingAdAtIndex = 0;
        } else { // If ad is between 0 and index of ads array.
            currentlyPlayingAdAtIndex++;
        }

        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                videoView.setTag(VIDEO_AD_VIEW_TAG);
                txtSongTitle.setText(arrAdvertisements.get(currentlyPlayingAdAtIndex).getAdvtName());
                txtSongArtist.setText("");
                setVideoPath(arrAdvertisements.get(currentlyPlayingAdAtIndex).getAdvtFilePath());
            }
        });

    }

    OnPreparedListener videoViewPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared() {

            //txtFileWriter.setVisibility(View.INVISIBLE);
            if ((Integer)videoView.getTag() == VIDEO_VIEW_TAG){

                /*
                If there is song selected as play next, insert its status.
                 */
                if (playNextSongIndex >= 0){
                    insertsongStatus(playNextSongIndex);
                    playNextSongIndex = -1;
                } else{
                    insertsongStatus(currentlyPlayingSongAtIndex);
                }

                setControlStatesForAdvertisement(true);

                if (currentPlaylist.getIsFadingActive() == 1){

                    if (isFadeInProgress){
                        videoView.setVolume(0);
                    } else{
                        MediaPlayerUtils.fadeIn(videoView,null,HomeActivity.this);
                    }

                } else {
                    videoView.setVolume(1);
                }

                videoView.start();


            } else {
                insertAdvertisementStatus(currentlyPlayingAdAtIndex);
                setControlStatesForAdvertisement(false);
                videoView.start();
            }
        }
    };

    private void enableControls(){
        btnPlayPause.setSelected(false);
        btnVolume.setEnabled(true);
        btnPlayPause.setEnabled(true);
    }

    private void disableControls(){
        btnPlayPause.setSelected(true);
        btnVolume.setEnabled(false);
        btnPlayPause.setEnabled(false);
    }

    private void getsongsafterdelete(Playlist playlist)
    {
        if (arrSongs.size() > 0) arrSongs.clear();

        ArrayList<Songs> songs = new PlaylistManager(HomeActivity.this,null).getSongsForPlaylist(playlist.getsplPlaylist_Id());
        if (songs != null && songs.size() > 0) {
            arrSongs.addAll(songs);
        }

        if (arrSongs.size() > 0) {
            songAdapter = new SongAdapter(HomeActivity.this, arrSongs);
            lvSongs.setAdapter(songAdapter);

        }
    }

    private void getPlaylistsForCurrentTime(){

        if (arrPlaylists.size() > 0) arrPlaylists.clear();

//        ArrayList<Playlist> p = new PlaylistManager(HomeActivity.this,null).getAllPlaylistInPlayingOrder();

        ArrayList<Playlist> playlistsForCurrentTime = new PlaylistManager(HomeActivity.this,null).getPlaylistForCurrentTimeOnly();

        if (playlistsForCurrentTime != null && playlistsForCurrentTime.size() > 0){
            arrPlaylists.addAll(playlistsForCurrentTime);
        }

        updateSideMenuPlaylist();

        if (arrPlaylists.size() > 0) {

            getSongsForPlaylist(arrPlaylists.get(0));
            rlNoPlaylist.setVisibility(View.GONE);
        } else {
            rlNoPlaylist.setVisibility(View.VISIBLE);
        }

        ArrayList<Playlist> playlists = new PlaylistManager(HomeActivity.this,null).getAllPlaylistInPlayingOrder();

        if (playlists != null && playlists.size() > 0){

            playlistAdapter = new PlaylistAdapter(HomeActivity.this, playlists,false, lvPlaylist);
            lvPlaylist.setOnItemClickListener(playlistClickListener);
            lvPlaylist.setAdapter(playlistAdapter);
        }

    }


    private void getSongsForPlaylist(Playlist playlist){

        /*If there is not valid playlist set then set the current playlist.*/
        if (AlenkaMedia.currentPlaylistId.equals("")){
            AlenkaMedia.currentPlaylistId = playlist.getsplPlaylist_Id();
        }

        /*If the AlenkaMedia.currentPlaylistId is not equal to current playing playlist then set
        * the current playlist as AlenkaMedia.currentPlaylistId*/

        if (!AlenkaMedia.currentPlaylistId.equals(playlist.getsplPlaylist_Id())){
            AlenkaMedia.currentPlaylistId = playlist.getsplPlaylist_Id();
        }

        currentlyPlayingSongAtIndex = 0;

        currentPlaylist = playlist;

        if (currentPlaylist.getIsFadingActive() == 1){
            videoView.setVolume(0);
        }

        if (arrSongs.size() > 0) arrSongs.clear();

        ArrayList<Songs> songs = new PlaylistManager(HomeActivity.this,null).getSongsForPlaylist(playlist.getsplPlaylist_Id());

        if (songs != null && songs.size() > 0) {
            arrSongs.addAll(songs);
        }

        if (arrSongs.size() > 0) {

            if (playlist.getIsSeparatinActive() == 0){
                sort(arrSongs);
            }

            songAdapter = new SongAdapter(HomeActivity.this, arrSongs);
            lvSongs.setAdapter(songAdapter);
            txtDownload.setVisibility(View.GONE);
            //int totalSongs = new PlaylistManager(HomeActivity.this,null).getTotalDownloadedSongs();
           // int countSongs=new PlaylistManager(HomeActivity.this,null).getTotalSongs();
           // if (totalSongs >= 0 ) {
              //  txtDownload.setText("Songs Download"+totalSongs+"/"+countSongs);
           // }

            lvSongs.setOnItemClickListener(songClickListener);
        } else {

            ArrayList<Songs> songNotDownloaded = new PlaylistManager(HomeActivity.this,null).
                    getSongsThatAreNotDownloaded(playlist.getsplPlaylist_Id());

            if (songNotDownloaded.size() > 0){

                if (!Utilities.isMyServiceRunning(DownloadService.class, HomeActivity.this)){
                    startService(new Intent(HomeActivity.this, DownloadService.class));
                    doBindService();
                }

            }
            // If there are no songs to be played then hide the player and show logo.
//            simpleExoPlayerView.setVisibility(View.INVISIBLE);
        }


        if (arrSongs.size() > 0){

            Handler mHandler = new Handler(getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    enableControls();
                    videoView.setVisibility(View.VISIBLE);
                    videoView.setTag(VIDEO_VIEW_TAG);
                    PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER++;
                    txtSongTitle.setText(arrSongs.get(currentlyPlayingSongAtIndex).getTitle());
                    txtSongArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex).getAr_Name());
                    if(arrSongs.size()>1) {
                        NextxtSongTitle.setText(arrSongs.get(currentlyPlayingSongAtIndex+1).getTitle());
                        NexttxtSongArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex+1).getAr_Name());
                    }
                    setVideoPath(arrSongs.get(currentlyPlayingSongAtIndex).getSongPath());
                }
            });




        } else {
            videoView.setVisibility(View.INVISIBLE);
        }



    }

    protected void onDestroy() {

        doUnbindService();
        stopService(new Intent(HomeActivity.this, DownloadService.class));
        videoView.stopPlayback();

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        sendDatabaseToServer();

    }

    private void sendDatabaseToServer() {

        new SendDatabaseToServerAsyncTask().execute();
    }

    class SendDatabaseToServerAsyncTask extends AsyncTask<String, Void ,String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Utilities.showToast(HomeActivity.this,"Start sending database");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Utilities.showToast(HomeActivity.this,"Finished sending database");

        }

        @Override
        protected String doInBackground(String... strings) {

            final String error;

            String path = HomeActivity.this.getApplicationInfo().dataDir
                    + File.separator + Constants.ROOT_FOLDER
                    + File.separator + MySQLiteHelper.DATABASE_NAME;

            if (path != null) {

                File dbFile = new File(path);

                if (dbFile.exists()) {

                    MediaType mediaType = MediaType.parse("multipart/form-data;");

                    String token = SharedPreferenceUtil.getStringPreference(HomeActivity.this, Constants.TOKEN_ID);

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM).addFormDataPart("",token + "-" + dbFile.getName(),RequestBody.create(mediaType, dbFile)                                    )
                            .build();

                    Request request = new Request.Builder()
                            .url("http://185.19.219.90/ReceiveUpload.aspx")
                            .post(requestBody)
                            .build();

                    OkHttpClient client =new OkHttpClient();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            String responseString = response.body().toString();

                            if (responseString != null) {

                            }
                        }
                    });
                }
            }

            return null;
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"Starting timer");
        startRepeatingTimer(null);

        sendLastCrashLog();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent != null){

                    boolean shouldPlaylistChange = intent.getBooleanExtra(Constants.ALARM_PLAYLIST_CHANGED,false);

                    if (shouldPlaylistChange){
                        // Stop the current playlist and start new playlist.
                        switchPlaylist();
                    } else {

                        int playlistStatus = intent.getIntExtra(Constants.ALARM_ACTION,-12);

                        if (playlistStatus == 0 || playlistStatus == 1){
                            onPlaylistTimeChanged(playlistStatus);
                        }
                    }
                }

            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

        networkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                int status = NetworkUtil.getConnectivityStatusString(context);

                boolean isConnected = haveNetworkConnection();

                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {

                    if (isConnected){

                        if (AlenkaMedia.getInstance().lastConnectedState == isConnected){
                            return;
                        }

                        Utilities.showToast(context,"Network Connected");

                        try{

                            if (!AlenkaMedia.getInstance().isDownloadServiceRunning && !isCountDownTimerRunning){
                                HomeActivity.this.stopService(new Intent(HomeActivity.this, DownloadService.class));
                                HomeActivity.this.startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                                doBindService();
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    } else {

                        if (AlenkaMedia.getInstance().lastConnectedState == isConnected){
                            return;
                        }

                        Utilities.showToast(HomeActivity.this,"Network Disconnected");

                        HomeActivity.this.stopService(new Intent(HomeActivity.this, DownloadService.class));
                    }

                    AlenkaMedia.getInstance().lastConnectedState = isConnected;
                }

            }
        };
        registerReceiver(networkChangeReceiver, intentConnectivity);

//        checkForPlaylistStatus.postDelayed(handlePlaylistStatus,delay);
        updatePlayerLoginStatus();

    }


    private boolean haveNetworkConnection() {

            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /*
    *TODO: On back press button code if user press back button the application will not destroy or stop
    * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (Integer.parseInt(String.valueOf(Build.VERSION.SDK_INT)) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void shouldUpdateTimeOnServer() {

        updatePlayerSongsStatus();
        updateDownloadedSongsStatusOnServer();
        checkForUpdateData();

    }

    @Override
    public void checkForPendingDownloads() {
//        checkForUnfinishedDownloads();
    }

    @Override
    public void playAdvertisement() {

        if (arrAdvertisements == null || arrAdvertisements.size() == 0){

            PlaylistWatcher.ADVERTISEMENT_TIME_COUNTER = 0;
            shouldPlaySoftStopAd = false;
            PlaylistWatcher.PLAY_AD_AFTER_SONGS_COUNTER = 0;
            return;
        }

        String adPlayType = SharedPreferenceUtil.getStringPreference(HomeActivity.this,AlenkaMediaPreferences.playing_Type);

        if (adPlayType.equals("Hard Stop")){

            // Stop current song and play song

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (arrAdvertisements.size() > 0){

                        if (videoView.isPlaying()){
                            videoView.stopPlayback();
                            setVisibilityAndPlayAdvertisement();
                        }
                    }
                }
            });

        } else if(adPlayType.equals("Soft Stop")){

            shouldPlaySoftStopAd = true;
        }

    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            if (videoView.isPlaying()){
                videoView.stopPlayback();
            }
            updateLogoutStatus();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Utilities.showToast(HomeActivity.this,"Please click BACK again to exit");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

    }

    Runnable handlePlaylistStatus = new Runnable() {
        @Override
        public void run() {

            if (currentPlaylistStatus != AlenkaMedia.playlistStatus){
                currentPlaylistStatus = AlenkaMedia.playlistStatus;
                onPlaylistTimeChanged(currentPlaylistStatus);
            }

//            checkForPlaylistStatus.postDelayed(this, delay);
        }
    };

    @Override
    protected void onStop() {

        Log.e(TAG,"Stopping timer");
        cancelRepeatingTimer(null);
        videoView.stopPlayback();
        stopService(new Intent(HomeActivity.this, DownloadService.class));
//        checkForPlaylistStatus.removeCallbacks(handlePlaylistStatus);
        super.onStop();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(networkChangeReceiver);
        super.onPause();

    }

    /*************************PlaylistWatcher Methods Starts****************************/

    public void startRepeatingTimer(View view) {
        Context context = HomeActivity.this;
        if(alarm != null){
            alarm.setWatcher();
        }else{
//            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelRepeatingTimer(View view){
        Context context = this.getApplicationContext();
        if(alarm != null){
            alarm.cancelWatcher();
        }else{
//            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void onPlaylistTimeChanged(int playlistCode) {

        switch (playlistCode){
            case PlaylistWatcher.NO_PLAYLIST:{

                if (videoView.isPlaying()){
                    videoView.stopPlayback();
                    videoView.setVisibility(View.GONE);
                }
            }break;

            case PlaylistWatcher.PLAYLIST_PRESENT:{
                getPlaylistsForCurrentTime();
            }break;
        }
    }
    public void switchPlaylist() {

        if (videoView.isPlaying()){
            videoView.stopPlayback();
            getPlaylistsForCurrentTime();
        }
    }


    /*This method inserts the status of song as played in database.*/

    public void insertsongStatus(final int index){

        try {
            String artist_id = arrSongs.get(index).getArtist_ID();
            String title_id  = arrSongs.get(index).getTitle_Id();
           // String timeid=arrSongs.get(index).getT_Time();
            String spl_plalist_id = arrSongs.get(index).getSpl_PlaylistId();

            PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
            playerStatusManager.artist_id = artist_id;
            playerStatusManager.title_id = title_id;
            playerStatusManager.spl_plalist_id = spl_plalist_id;
            playerStatusManager.insertSongPlayedStatus();

            playerStatusManager = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void insertAdvertisementStatus(final int index){

        String currentDate = Utilities.currentDate();
        String currenttime = Utilities.currentTime();

        PlayerStatus playerStatus = new PlayerStatus();
        playerStatus.setAdvPlayedDate(currentDate);
        playerStatus.setAdvPlayedTime(currenttime);
        playerStatus.setAdvIdStatus(arrAdvertisements.get(index).getAdvtID());
        playerStatus.setPlayerStatusAll("adv");
        PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
        playerStatusManager.insertAdvPlayerStatus(playerStatus);

    }


    private void updatePlayerLoginStatus(){

        PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
        playerStatusManager.updateLoginStatus();
        playerStatusManager.updateHeartBeatStatus();
        playerStatusManager.updateDataOnServer();

    }

    private void updatePlayerSongsStatus(){

        PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
        playerStatusManager.sendPlayedSongsStatusOnServer();
    }

    private void updateLogoutStatus(){

        PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
        playerStatusManager.updateLogoutStatus();

    }

    @Override
    public void onResponse(String response, int tag) {
        if (response == null || response.equals("") || response.length() < 1){

            Utilities.showToast(HomeActivity.this, "Empty response for player statuses");
            return;
        }
    }

    @Override
    public void onError(Exception e, int tag) {

    }

    public static void saveLogcatToFile(Context context) {

        String fileName = "logcat_"+System.currentTimeMillis()+".txt";
        File outputFile = new File(Environment.getExternalStorageDirectory(),fileName);
        try {
            @SuppressWarnings("unused")
            Process process = Runtime.getRuntime().exec("logcat -e "+outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlaylistStatusChanged(int status) {

        switch (status){
            case PlaylistWatcher.NO_PLAYLIST:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (videoView.isPlaying()){
                            videoView.stopPlayback();
                            rlNoPlaylist.setVisibility(View.VISIBLE);
//                            videoView.setVisibility(View.GONE);
                        }
                    }
                });



            }break;

            case PlaylistWatcher.PLAYLIST_PRESENT:{

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rlNoPlaylist.setVisibility(View.GONE);
                        getPlaylistsForCurrentTime();
                    }
                });


            }break;

            case PlaylistWatcher.PLAYLIST_CHANGE:{

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (videoView.isPlaying()){
                            videoView.stopPlayback();
//                            videoView.setVisibility(View.GONE);
                            rlNoPlaylist.setVisibility(View.GONE);
                        }

                        // Check if the songs of next playlist have been downloaded, if not, restart the player.

                        ArrayList<Songs> downloadedSongs = new PlaylistManager(HomeActivity.this,null).
                                                                getDownloadedSongsForPlaylist(PlaylistWatcher.currentPlaylistID);

                        if (downloadedSongs == null || downloadedSongs.size() == 0) {

                            if (Utilities.isMyServiceRunning(DownloadService.class, HomeActivity.this)){

                                stopService(new Intent(HomeActivity.this, DownloadService.class));
                                restartPlayer();
                            }

                            return;
                        }

                        getPlaylistsForCurrentTime();
                    }
                });


            }break;
        }

    }

    private void sort(ArrayList<Songs> songsArrayList){

        try {

            Collections.sort(songsArrayList, new Comparator<Songs>() {
                @Override
                public int compare(Songs songs, Songs t1) {

                    long song1 = songs.getSerialNo();
                    long song2 = t1.getSerialNo();

                    return Long.compare(song1, song2);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    /*************************PlaylistWatcher Methods Ends*/

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanForFiles(){

        String path = HomeActivity.this.getApplicationInfo().dataDir;

        File file = new File(path);

     /*   Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
        File sdCard = externalLocations.get(ExternalStorage.SD_CARD);

        if (sdCard != null){
            sdCardLocation = sdCard.getAbsolutePath();
        }
        File externalSdCard = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);
*/
       /* HashSet<String> externalLocations = ExternalStorage.getExternalMounts();

        if (externalLocations.size() > 0){

            Object[] aa = externalLocations.toArray();
            sdCardLocation = (String) aa[0];
        }*/

        List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();

       File[] files = HomeActivity.this.getExternalMediaDirs();

       String[] sdsd = files[1].getAbsolutePath().split("/");

        if (sdsd.length > 3){
            String zeroComponent = sdsd[0];

            if (zeroComponent.equals(" ") || zeroComponent.equals("")){
                zeroComponent = "/";
            }
            String firstComponent = sdsd[1];
            String secondComponent = sdsd[2];

            String finalPath = zeroComponent + firstComponent + File.separator + secondComponent;

            sdCardLocation = finalPath;
        }

       /* if (files.length > 1){

            String storage = files[1].getAbsolutePath();
            sdCardLocation = storage;
        }*/

        if (arrVideoFiles.size() > 0){

            targetFileName = arrVideoFiles.get(13);

//            targetFileName = firstVideoFileLocation.substring(firstVideoFileLocation.lastIndexOf("/")+1);

            try {

                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 42);

               /* File target = new File(sdCardLocation.concat(File.separator + targetFileName));
                File source = new File(firstVideoFileLocation);
                copyDirectory(source,target);*/

            }catch (Exception e){
                Log.e("File Copy", "Failed");
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_CODE_STORAGE_FOLDER_SELECTOR){

            Uri treeUri = resultData.getData();

            String path = FileUtil.getFullPathFromTreeUri(treeUri,HomeActivity.this);

            if (path != null){

                File sourceDirectory = new File(path);

                if (sourceDirectory != null){

                    DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);

                    AlenkaMedia.globalDocumentFile = sourceDirectory;

                    String pickedD = pickedDir.toString();

                    if (!AlenkaMedia.getInstance().isDownloadServiceRunning){

                        startService(new Intent(this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,treeUri.toString()));
                        doBindService();
                    }
                }
            }


        }
    }

    void listRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                listRecursive(child);

        Log.e("List Files: ", fileOrDirectory.getName());

        if (fileOrDirectory.getName().endsWith(".mp4")) {

            arrVideoFiles.add(fileOrDirectory.getAbsolutePath());
        }
    }

    private void updateDownloadedSongsStatusOnServer(){

        //ArrayList<Songs> totalSongsThatAreDownloaded = new PlaylistManager(HomeActivity.this,null).getAllDownloadedSongs();

        int totalSongs = new PlaylistManager(HomeActivity.this,null).getTotalDownloadedSongs();

        if (totalSongs >= 0){

            PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
            playerStatusManager.songsDownloaded = "" + totalSongs;
            playerStatusManager.updateDownloadedSongsCountOnServer();
            //txtDownload.setText("Songs Downloaded= "+totalSongs);

        }

        /*if (countOfTotalSongsToBeDownloaded > 0){

            if (getSongsToBeDownloaded() != null){

                int songsThatHaveBeenDownloaded =  countOfTotalSongsToBeDownloaded - getSongsToBeDownloaded().size();

                if (songsThatHaveBeenDownloaded > 0){

                    PlayerStatusManager playerStatusManager = new PlayerStatusManager(HomeActivity.this);
                    playerStatusManager.songsDownloaded = "" + songsThatHaveBeenDownloaded;
                    playerStatusManager.updateDownloadedSongsCountOnServer();
                }
            }
        }*/
    }

    private void checkForUnfinishedDownloads(){

        Utilities.showToast(HomeActivity.this,"Checking for unfinished downloads.");

        Log.e(TAG, "Checking for unfinished downloads.");
        if (ConnectivityReceiver.isConnected()){

            Log.e(TAG, "Internet is connected.");
            if (!AlenkaMedia.getInstance().isDownloadServiceRunning){

                ArrayList<Songs> songs = getSongsToBeDownloaded();
                ArrayList<Advertisements> ads = getAdvertisementsToBeDownloaded();

                if (songs != null && songs.size() > 0 ||
                        ads != null && ads.size() > 0) {
                    Log.e(TAG, "Starting download.");
                    Utilities.showToast(HomeActivity.this,"Starting download for unfinished songs.");
                    startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                    doBindService();
                }
            }
        }
    }

    private void checkForUpdateData(){

        boolean shouldUpdateData = AlenkaMedia.getInstance().isUpdateInProgress;

        if (!shouldUpdateData)
        new PlaylistManager(HomeActivity.this, playlistLoaderListener).checkUpdatedPlaylistData();
    }

    PlaylistLoaderListener playlistLoaderListener = new PlaylistLoaderListener() {
        @Override
        public void startedGettingPlaylist() {

            Log.e(TAG,"Started getting playlist");
        }

        @Override
        public void finishedGettingPlaylist() {

            Log.e(TAG,"Finished getting playlist");

            ArrayList<Songs> songs = getSongsToBeDownloaded();
            ArrayList<Advertisements> ads = getAdvertisementsToBeDownloaded();

            /*
            If new songs are present. Start downloading them, else restart the player for playlist time changes sync.
             */
            if (songs != null && songs.size() > 0 ||
                    ads != null && ads.size() > 0){

                if (!AlenkaMedia.getInstance().isDownloadServiceRunning){
                    startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                    doBindService();
                }



            } else {

                PlaylistManager playlistManager = new PlaylistManager(HomeActivity.this,playlistLoaderListener);
                playlistManager.publishTokenForUpdatedData();
            }
        }

        @Override
        public void errorInGettingPlaylist(final Exception e) {
            Utilities.showToast(HomeActivity.this, e.getLocalizedMessage());
        }

        @Override
        public void recordSaved(boolean isSaved) {

        }

        @Override
        public void tokenUpdatedOnServer() {

            AlenkaMedia.getInstance().isUpdateInProgress = false;
            restartPlayer();
        }
    };

    private void restartPlayer(){

        AlenkaMedia.playlistStatus = -12;
        AlenkaMedia.currentPlaylistId = "";
        startActivity(new Intent(HomeActivity.this, Splash_Activity.class));
        HomeActivity.this.finish();
    }


    private void showAlertDialogForStorageSelection(){

        dialogBuilder = new AlertDialog.Builder(HomeActivity.this, R.style.AppTheme_MaterialDialogTheme);

        dialogBuilder.setTitle("Select source");
        dialogBuilder.setMessage("Please select source of songs.");
        dialogBuilder.setNegativeButton("Download from Internet",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mCountDownTimer != null){
                            mCountDownTimer.cancel();
                        }

                        alarm = new PlaylistWatcher();
                        alarm.setContext(HomeActivity.this);
                        alarm.setPlaylistStatusListener(HomeActivity.this);
                        alarm.setWatcher();

                        startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,true));
                        doBindService();
                        dialog.dismiss();
                    }
                }
        ).setPositiveButton("External Storage", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (mCountDownTimer != null){
                    mCountDownTimer.cancel();
                }

                if (Utilities.isVersionLowerThanLollipop()){

                    List<StorageUtils.StorageInfo> list = StorageUtils.getStorageList();

                    if (list.size() > 1){

                        File root = new File(list.get(1).path, "AlenkaMedia");

                        if (root.exists()){

                            AlenkaMedia.globalDocumentFile = root;

                            startService(new Intent(HomeActivity.this, DownloadService.class).putExtra(Constants.TAG_START_DOWNLOAD_SERVICE,root.toString()));
                            doBindService();
                            alarm = new PlaylistWatcher();
                            alarm.setContext(HomeActivity.this);
                            alarm.setPlaylistStatusListener(HomeActivity.this);
                            alarm.setWatcher();

                        }else {

                            Utilities.showToast(HomeActivity.this,"AlenkaMedia folder does not exist in external storage.");
                            }

                    } else {
                        Utilities.showToast(HomeActivity.this,"No external storage found.");
                        }

                } else {
                    startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_STORAGE_FOLDER_SELECTOR);
                }

//                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_STORAGE_FOLDER_SELECTOR);
                dialogInterface.dismiss();
            }
        })
        .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (mCountDownTimer != null){
                    mCountDownTimer.cancel();
                }

                SharedPreferenceUtil.setBooleanPreference(HomeActivity.this,Constants.STORAGE_ALERT_SHOWN_ONCE,false);
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }
        });
        dialogBuilder.setCancelable(false);

        dialog = dialogBuilder.create();
        final Window dialogWindow = dialog.getWindow();
        final WindowManager.LayoutParams dialogWindowAttributes = dialogWindow.getAttributes();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
// Set fixed width (280dp) and WRAP_CONTENT height
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogWindowAttributes);
        lp.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, getResources().getDisplayMetrics());
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);

// Set to TYPE_SYSTEM_ALERT so that the Service can display it
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_TOAST);
//            dialogWindowAttributes.windowAnimations = R.style.Dialo;
        dialog.show();
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {

       // showDialogToPlayAnyPlaylist(position);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.playPause:{
                if (videoView.isPlaying()){

                    videoView.pause();
                   // btnplay.setVisibility(View.VISIBLE);
                   // btnPlayPause.setVisibility(View.INVISIBLE);

                    if (fadingMediaPlayer != null && fadingMediaPlayer.isFadingMediaPlaying()){
                        fadingMediaPlayer.pause();
                    }

                    btnPlayPause.setSelected(true); //Shows play button

                    if (alarm != null){
                        alarm.pausePlaylistWatcher();
                    }
                } else {
                    videoView.start();

                    if (fadingMediaPlayer != null && !fadingMediaPlayer.isFadingMediaPlaying()){
                        fadingMediaPlayer.resume();
                    }

                    btnPlayPause.setSelected(false);


                    if (alarm != null){
                        alarm.resumePlaylistWatcher();
                    }
                }

            }break;

            case R.id.volume:{

                if (btnVolume.isSelected()){

                    videoView.setVolume(1);
                    btnVolume.setSelected(false);

                } else {

                    videoView.setVolume(0);
                    btnVolume.setSelected(true);
                }
            }break;

            case R.id.next:{

                    playSong();

            }break;


            case R.id.stop:{
                videoView.release();
                videoView.stopPlayback();
                lvSongs.setVisibility(View.GONE);
                txtSongTitle.setVisibility(View.GONE);
                txtSongArtist.setVisibility(View.GONE);
                NextxtSongTitle.setVisibility(View.GONE);
                NexttxtSongArtist.setVisibility(View.GONE);
                txtCurrentDuration.setText("00:00");
                txtTotalDuration.setText("00:00");
                btnPlayPause.setEnabled(false);

            }break;

            case R.id.helpimg:{
                help();
               // pickerDialog.dismiss();

            }break;


            case R.id.btnPlayNow:{

                ImageButton button = (ImageButton) view;

                Integer tag = (Integer) button.getTag();
                if(lvSongs.getVisibility()==View.VISIBLE) {
                    playSelectedSong(tag);
                }
                else {
                    playSelectedSongfromSearch(tag);
                      }

                pickerDialog.dismiss();

            }break;

            case R.id.btnPlayNow1:{

                ImageButton button = (ImageButton) view;

                Integer tag = (Integer) button.getTag();

                playSelectedSongfromFragment(tag);
                pickerDialog.dismiss();
                pickerDialog1.dismiss();
                drawerLayout.closeDrawers();



            }break;


            case R.id.btnPlayNext:{

                ImageButton button = (ImageButton) view;

                Integer tag = (Integer) button.getTag();

                addSelectedSongToTheQueueToBePlayedNext(tag);


                pickerDialog.dismiss();

            }break;

            case R.id.btnPlayNext1:{

                ImageButton button = (ImageButton) view;

                Integer tag = (Integer) button.getTag();

                addSelectedSongToTheQueueToBePlayedNext(tag);
                pickerDialog.dismiss();
                pickerDialog1.dismiss();
                drawerLayout.closeDrawers();



            }break;


            case R.id.btnYes:{

                Button button = (Button) view;

                Integer tag = (Integer) button.getTag();
                if(lvSongs.getVisibility()==View.VISIBLE) {
                    deleteSelectedSong(tag);
                }
                else {
                    deleteSelectedSongSearch(tag);
                }

                pickerDialog.dismiss();

            }break;

            case R.id.btnYes1:{

                Button button = (Button) view;

                Integer tag = (Integer) button.getTag();

                deleteSelectedSongfromFragment(tag);

                pickerDialog.dismiss();


            }break;


            case R.id.btnOk:{
                ImageButton button = (ImageButton) view;

                Integer tag = (Integer) button.getTag();

                playSelectedPlaylist(tag);

                pickerDialog.dismiss();

                drawerLayout.closeDrawers();

            }break;
        }
    }

    private void playSelectedSong(int index){

        videoView.setTag(VIDEO_VIEW_TAG);

        currentlyPlayingSongAtIndex = index;

        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txtSongTitle.setText(arrSongs.get(currentlyPlayingSongAtIndex).getTitle());
                txtSongArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex).getAr_Name());
                if(arrSongs.size()-1>currentlyPlayingSongAtIndex) {
                    NextxtSongTitle.setText(arrSongs.get(currentlyPlayingSongAtIndex + 1).getTitle());
                    NexttxtSongArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex + 1).getAr_Name());
                }
                else{
                    NextxtSongTitle.setText(arrSongs.get(0).getTitle());
                    NexttxtSongArtist.setText(arrSongs.get(0).getAr_Name());
                }
                setVideoPath(arrSongs.get(currentlyPlayingSongAtIndex).getSongPath());

            }
        });

    }
    private void playSelectedSongfromSearch(int index){

        videoView.setTag(VIDEO_VIEW_TAG);

        currentlyPlayingSongAtIndex = index;

        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txtSongTitle.setText(arrSearch.get(currentlyPlayingSongAtIndex).getTitle());
                txtSongArtist.setText(arrSearch.get(currentlyPlayingSongAtIndex).getAr_Name());
                setVideoPath(arrSearch.get(currentlyPlayingSongAtIndex).getSongPath());
            }
        });

    }
    private void playSelectedSongfromFragment(int index){

        videoView.setTag(VIDEO_VIEW_TAG);

        currentlyPlayingSongAtIndex = index;

        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
              txtSongTitle.setText(arrDialog.get(currentlyPlayingSongAtIndex).getTitle());
              txtSongArtist.setText(arrDialog.get(currentlyPlayingSongAtIndex).getAr_Name());
                setVideoPath(arrDialog.get(currentlyPlayingSongAtIndex).getSongPath());
            }
        });

    }

    private void deleteSelectedSong(int index){

        videoView.setTag(VIDEO_VIEW_TAG);

        deletePlayingSongAtIndex = index;
        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                SharedPreferenceUtil.getStringPreference(HomeActivity.this,"TitleidDelete");
                SharedPreferences prefs = getSharedPreferences("listviewdelete", MODE_PRIVATE);
               String titleiddel= prefs.getString("TitleidDelete","Default");
                arrSongs.remove(deletePlayingSongAtIndex);
                songAdapter.notifyDataSetChanged();
                MySQLiteHelper sd=new MySQLiteHelper(HomeActivity.this);
                boolean y=sd.InsertData(titleiddel);

            }
        });

    }



    private void deleteSelectedSongSearch(int index){

        videoView.setTag(VIDEO_VIEW_TAG);

        deletePlayingSongAtIndex = index;
        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                SharedPreferenceUtil.getStringPreference(HomeActivity.this,"TitleidDelete");
                SharedPreferences prefs = getSharedPreferences("listviewdelete", MODE_PRIVATE);
                String titleiddel= prefs.getString("TitleidDelete","Default");
                arrSearch.remove(deletePlayingSongAtIndex);
                searchAdapter.notifyDataSetChanged();
                MySQLiteHelper sd=new MySQLiteHelper(HomeActivity.this);
                boolean y=sd.InsertData(titleiddel);

            }
        });

    }

    private void deleteSelectedSongfromFragment(int index){

        videoView.setTag(VIDEO_VIEW_TAG);

        deletePlayingSongAtIndex = index;
        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                SharedPreferenceUtil.getStringPreference(HomeActivity.this,"TitleidDelete");
                SharedPreferences prefs = getSharedPreferences("listviewdelete", MODE_PRIVATE);
                String titleiddel= prefs.getString("TitleidDelete","Default");
                arrDialog.remove(deletePlayingSongAtIndex);
                dialogAdapter.notifyDataSetChanged();
                MySQLiteHelper sd=new MySQLiteHelper(HomeActivity.this);
                boolean y=sd.InsertData(titleiddel);
                getsongsafterdelete(arrPlaylists.get(0));


            }
        });

    }

    private void setControlStatesForAdvertisement(boolean shouldEnable){

        btnPlayPause.setEnabled(shouldEnable);
        btnVolume.setEnabled(shouldEnable);
        lvPlaylist.setEnabled(shouldEnable);
        lvSongs.setEnabled(shouldEnable);

        if (drawerFragment != null){
            drawerFragment.setPlaylistEnabled(shouldEnable);
        }
    }

    private void addSelectedSongToTheQueueToBePlayedNext(int index){

        playNextSongIndex = index;
        Utilities.showToast(this,"Song added to queue." );

    }

    private void playSelectedPlaylist(int index){

        Playlist selectedPlaylist = drawerFragment.playlistArrayList.get(index);

        currentPlaylist = selectedPlaylist;

        ArrayList<Songs> arrSongsForSelectedPlaylist = new PlaylistManager(HomeActivity.this,null).
                getSongsForPlaylist(selectedPlaylist.getsplPlaylist_Id());

        if (arrSongsForSelectedPlaylist != null && arrSongsForSelectedPlaylist.size() > 0){

            arrSongs.clear();
            arrSongs.addAll(arrSongsForSelectedPlaylist);

            if (selectedPlaylist.getIsSeparatinActive() == 0){
                sort(arrSongs);
            }

            Handler mHandler = new Handler(getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    videoView.setTag(VIDEO_VIEW_TAG);

                    currentlyPlayingSongAtIndex = 0;
                   songAdapter.notifyDataSetChanged();
                    playlistAdapter.notifyDataSetChanged();
                    updateSideMenuPlaylist();
                    txtSongTitle.setText(arrSongs.get(currentlyPlayingSongAtIndex).getTitle());
                    txtSongArtist.setText(arrSongs.get(currentlyPlayingSongAtIndex).getAr_Name());
                    setVideoPath(arrSongs.get(currentlyPlayingSongAtIndex).getSongPath());
                }
            });

        }

    }

    public void deletesongslistview(int index)

    {
        pickerDialog = new Dialog(HomeActivity.this);

        pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        pickerDialog.setContentView(R.layout.custom_alert_dialog_deletesongs);

        pickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        pickerDialog.setTitle(R.string.alert_dialog_OnSongClick_title);

        Button btnyes = (Button)pickerDialog.findViewById(R.id.btnYes);

        Button btnno = (Button)pickerDialog.findViewById(R.id.btnNo);

        ImageView btnCancel = (ImageView) pickerDialog.findViewById(R.id.recycleCancel);


        btnyes.setTag(index);
        btnyes.setOnClickListener(HomeActivity.this);
        btnno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });

        pickerDialog.show();
    }

    public void deletesongsfromDialogFragment(int index)

    {
        pickerDialog = new Dialog(HomeActivity.this);

        pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        pickerDialog.setContentView(R.layout.custom_alert_dialog_deletedialogsong);

        pickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        pickerDialog.setTitle(R.string.alert_dialog_OnSongClick_title);

        Button btnyes = (Button)pickerDialog.findViewById(R.id.btnYes1);

        Button btnno = (Button)pickerDialog.findViewById(R.id.btnNo1);

        ImageView btnCancel = (ImageView) pickerDialog.findViewById(R.id.recycleCancel);

        btnyes.setTag(index);
        btnyes.setOnClickListener(HomeActivity.this);
        btnno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });
        pickerDialog.show();
    }

    public void help()
    {
        pickerDialog = new Dialog(HomeActivity.this);

        pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        pickerDialog.setContentView(R.layout.custom_alert_dialog_help);

        pickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        pickerDialog.setTitle(R.string.alert_dialog_OnSongClick_title);

        ImageView btnCancel = (ImageView) pickerDialog.findViewById(R.id.helpCancel);

        TextView txtidhelp  = (TextView) pickerDialog.findViewById(R.id.helptext);

        txtidhelp.setText("The player id is "+SharedPreferenceUtil.getStringPreference(HomeActivity.this, Constants.TOKEN_ID));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });

        pickerDialog.show();
    }


    public void searchNotFound()
    {
        pickerDialog = new Dialog(HomeActivity.this);

        pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        pickerDialog.setContentView(R.layout.sustom_alet_dialog_searchnotfound);

        pickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        pickerDialog.setTitle(R.string.alert_dialog_OnSongClick_title);

        ImageView btnCancel = (ImageView) pickerDialog.findViewById(R.id.helpCancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });

        pickerDialog.show();
    }


    public void showDialogToPlayAnySong(int index)
    {
        pickerDialog = new Dialog(HomeActivity.this);

        pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        pickerDialog.setContentView(R.layout.custom_alert_dialog_toplaysong);

        pickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        pickerDialog.setTitle(R.string.alert_dialog_OnSongClick_title);

        ImageButton btnPlayNow = (ImageButton)pickerDialog.findViewById(R.id.btnPlayNow);

        ImageButton btnPlayNext = (ImageButton)pickerDialog.findViewById(R.id.btnPlayNext);

        ImageButton btnCancel = (ImageButton)pickerDialog.findViewById(R.id.btnCancel);

        TextView txtVwNewSong  = (TextView) pickerDialog.findViewById(R.id.play_new_song);

        TextView txtVwCurrentSong  = (TextView) pickerDialog.findViewById(R.id.play_current_song);

        txtVwNewSong.setTypeface(fontBold);
        txtVwCurrentSong.setTypeface(fontBold);

        btnPlayNow.setOnClickListener(HomeActivity.this);
        btnPlayNow.setTag(index);

        btnPlayNext.setOnClickListener(HomeActivity.this);
        btnPlayNext.setTag(index);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });

        pickerDialog.show();
    }

    public void showDialogToPlayAnySongfromDialog(int index)
    {
        pickerDialog = new Dialog(HomeActivity.this);

        pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        pickerDialog.setContentView(R.layout.custom_alert_dialog_tplaysong_dialog);

        pickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        pickerDialog.setTitle(R.string.alert_dialog_OnSongClick_title);

        ImageButton btnPlayNow = (ImageButton)pickerDialog.findViewById(R.id.btnPlayNow1);

        ImageButton btnPlayNext = (ImageButton)pickerDialog.findViewById(R.id.btnPlayNext1);

        ImageButton btnCancel = (ImageButton)pickerDialog.findViewById(R.id.btnCancel);

        TextView txtVwNewSong  = (TextView) pickerDialog.findViewById(R.id.play_new_song);

        TextView txtVwCurrentSong  = (TextView) pickerDialog.findViewById(R.id.play_current_song);

        txtVwNewSong.setTypeface(fontBold);
        txtVwCurrentSong.setTypeface(fontBold);

        btnPlayNow.setOnClickListener(HomeActivity.this);
        btnPlayNow.setTag(index);

        btnPlayNext.setOnClickListener(HomeActivity.this);
        btnPlayNext.setTag(index);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });

        pickerDialog.show();
    }


    public void showDialogToPlayAnyPlaylist(int index){
        // custom dialog
        pickerDialog = new Dialog(HomeActivity.this);
        pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pickerDialog.setContentView(R.layout.custom_alert_dialog);
        pickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //dialog.getWindow().setLayout(400,300);

        pickerDialog.setTitle(R.string.alert_dialog_OnplaylistClick_title);

        ImageButton btnok = (ImageButton)pickerDialog.findViewById(R.id.btnOk);
        ImageButton btnContinue = (ImageButton)pickerDialog.findViewById(R.id.btnContinue);
        ImageButton btnCancel = (ImageButton)pickerDialog.findViewById(R.id.btnCancel);

        btnok.setTag(index);
        btnContinue.setTag(index);

       // TextView txtVwNewPlaylist  = (TextView) pickerDialog.findViewById(R.id.play_new_playlist);
        //TextView txtVwCurrentPlaylist  = (TextView) pickerDialog.findViewById(R.id.play_current_playlist);


       // txtVwNewPlaylist.setTypeface(fontBold);
       // txtVwCurrentPlaylist.setTypeface(fontBold);
       // btnContinue.setTypeface(fontNormal);
       // btnok.setTypeface(fontNormal);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });
        btnok.setOnClickListener(this);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });

        pickerDialog.show();
    }


    AdapterView.OnItemClickListener playlistClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
           //showDialogToPlayAnyPlaylist(i);
        }
    };


    AdapterView.OnItemClickListener songClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
           //showDialogToPlayAnySong(i);

}
    };

}
