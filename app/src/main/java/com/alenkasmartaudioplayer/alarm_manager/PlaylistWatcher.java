package com.alenkasmartaudioplayer.alarm_manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;

import com.alenkasmartaudioplayer.activities.Splash_Activity;
import com.alenkasmartaudioplayer.application.AlenkaMedia;
import com.alenkasmartaudioplayer.mediamanager.AdvertisementsManager;
import com.alenkasmartaudioplayer.mediamanager.PlaylistManager;
import com.alenkasmartaudioplayer.models.Advertisements;
import com.alenkasmartaudioplayer.models.Playlist;
import com.alenkasmartaudioplayer.utils.AlenkaMediaPreferences;
import com.alenkasmartaudioplayer.utils.SharedPreferenceUtil;
import com.alenkasmartaudioplayer.utils.Utilities;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by love on 3/6/17.
 */
public class PlaylistWatcher {


    /***********************************************************
     *
     * Constant for when no playlist is present at current time.
     *
     ***********************************************************/
    public static final int NO_PLAYLIST = 0;


    /***********************************************************
     *
     * Constant for when a playlist is present at current time.
     *
     ***********************************************************/
    public static final int PLAYLIST_PRESENT = 1;


    /************************PLAYLIST_CHANGE********************
     *
     * Constant for when a playlist is present at current time and
     * the next playlist is present without any gap.
     * For example end time of Playlist A is 4:00 pm and the start
     * time of Playlist B is also 4:00 pm. In this case we stop
     * current playback and start the new one.
     *
     ***********************************************************/
    public static final int PLAYLIST_CHANGE = 2;


    /*****************currentDayOfTheWeek************************
     * This variable indicates the current day of week as an integer.
     * For ex 1 for Monday 2 for Tuesday and so on.
     ***********************************************************/
    private static int currentDayOfTheWeek = -1;


    /******************currentPlaylistID***********************
     * ID of the playlist currently playling.
     **********************************************************/
    public static String currentPlaylistID = "";

    /**********************************************************
     * Handler that checks the playlists time. This runs every
     * 10 second
     ***********************************************************/
    private Handler mHandler = null;

    /***********************************************************
     * This handler is used to run the mHandler in background.
     ************************************************************/
    private HandlerThread mHandlerThread = null;

    Context context;

    private PlaylistStatusListener playlistStatusListener;

    private static int UPDATE_PLAYER_STATUS_TIME = 600 * 1000; //15 minutes 900

    private static long UPDATE_PLAYER_STATUS_TIMER = 0;

    private static int CHECK_PENDING_DOWNLOADS_TIME = 600 * 1000; //15 minutes 900

    private static long CHECK_PENDING_DOWNLOADS_TIMER = 0;

    private static String ADVERTISEMENT_TYPE = ""; // 1 for isMinute, 2 for isSong, 3 is for isTime

    private static int TIME_AFTER_ADVERTISEMENT_SHOULD_PLAY = -1;


    /*******************ADVERTISEMENT_TIME_COUNTER*******************
     * ADVERTISEMENT_TIME_COUNTER is used to check the time for an ad
     * to play. Initial value is 0 and after every 10 seconds the value
     * is increased to 10 seconds(in milliseconds). When the value becomes
     * equal to TIME_AFTER_ADVERTISEMENT_SHOULD_PLAY we play the advertisement.
     ****************************************************************/
    public static long ADVERTISEMENT_TIME_COUNTER = 0;

    /*************************ADVERTISEMENT_TIME********************
     * ADVERTISEMENT_TIME indicates the number of minutes(in milliseconds)
     * after which the ad will play. Default value is 1 and actual value we
     * get from server.
    ****************************************************************/
    private static int ADVERTISEMENT_TIME = 1;

    /******************PLAY_AD_AFTER_SONGS****************************
     * This variable is used when ADVERTISEMENT_TYPE is of type 2
     * that is when advertisement is to be played after a number
     * songs have played.
     *****************************************************************/
    public static int PLAY_AD_AFTER_SONGS = -1;

    /****************PLAY_AD_AFTER_SONGS_COUNTER************************
    * This variable is used to keep track the number of songs that have been
     * played. After value of this variable reaches PLAY_AD_AFTER_SONGS we play
     * an ad and this value is reset to 0.
     *******************************************************************/
    public static long PLAY_AD_AFTER_SONGS_COUNTER = 0;

    /****************PLAYLIST_TIMER_CHECK_TIMER************************
     * This is the value in seconds in which the handler runs every mentioned
     * seconds.
     *******************************************************************/
    public static long PLAYLIST_TIMER_CHECK_TIMER = 1;

    /****************ADVERTISEMENT_PLAY_TIME************************
     * When advertisement is of type isTime. This variable will keep
     * track of the time on which the next song advertisement is to be
     * played.
     ******************************************************************/
    public static String ADVERTISEMENT_PLAY_TIME = "";

    ArrayList<Advertisements> advertisements;

    private static int currentlyPlayingAdAtIndex = 0;

    private boolean isPaused = false;

    public interface PlaylistStatusListener {
        void onPlaylistStatusChanged(int status);
        void shouldUpdateTimeOnServer();
        void playAdvertisement();
        void checkForPendingDownloads();
        void refreshPlayerControls();
    }

    public void setContext(Context context){

        this.context = context;
        setAdvertisements();
    }

    public void setPlaylistStatusListener(PlaylistStatusListener playlistStatusListener) {
        this.playlistStatusListener = playlistStatusListener;
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (PlaylistWatcher.this.playlistStatusListener != null){
                PlaylistWatcher.this.playlistStatusListener.refreshPlayerControls();
            }

            if (ADVERTISEMENT_TYPE.equals("1")){ //isMinuteAdvertisement

                ADVERTISEMENT_TIME_COUNTER = ADVERTISEMENT_TIME_COUNTER + (500 * PLAYLIST_TIMER_CHECK_TIMER);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(ADVERTISEMENT_TIME_COUNTER);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(ADVERTISEMENT_TIME_COUNTER);

                long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(ADVERTISEMENT_TIME);

                Logger.e("Total time = " + totalMinutes);
                Logger.e("Current time in seconds = " + seconds);
                Logger.e("Current time in minutes = " + minutes);

                if (ADVERTISEMENT_TIME_COUNTER == ADVERTISEMENT_TIME){
                    // Play advertisement;
//                    Toast.makeText(PlaylistWatcher.this.context, "Should play advertisement", Toast.LENGTH_SHORT).show();

                    if (PlaylistWatcher.this.playlistStatusListener != null){
                        PlaylistWatcher.this.playlistStatusListener.playAdvertisement();
                    }
                }
            }

            if (ADVERTISEMENT_TYPE.equals("3")){

                    String timeStamp = new SimpleDateFormat("h:mm aa", Locale.US).format(Calendar.getInstance().getTime());

                    if (timeStamp.equals(ADVERTISEMENT_PLAY_TIME)){
//                        Toast.makeText(PlaylistWatcher.this.context, "Play ad for isTime", Toast.LENGTH_SHORT).show();

                        if (PlaylistWatcher.this.playlistStatusListener != null){
                            PlaylistWatcher.this.playlistStatusListener.playAdvertisement();
                        }

                        currentlyPlayingAdAtIndex++;

                        if (advertisements.size() - 1 >= currentlyPlayingAdAtIndex){

                            String playAdAtTime = advertisements.get(currentlyPlayingAdAtIndex).getsTime();

                            if (playAdAtTime != null){
                                ADVERTISEMENT_PLAY_TIME = playAdAtTime;
                            }
                        } else {
                            ADVERTISEMENT_PLAY_TIME = "";
                        }
                    }
            }

            UPDATE_PLAYER_STATUS_TIMER = UPDATE_PLAYER_STATUS_TIMER + (1000 * PLAYLIST_TIMER_CHECK_TIMER);

//            Log.e("Playlist Watcher", "Timer Value " + UPDATE_PLAYER_STATUS_TIMER);

            if (UPDATE_PLAYER_STATUS_TIMER == UPDATE_PLAYER_STATUS_TIME){

                if (PlaylistWatcher.this.playlistStatusListener != null){
                    PlaylistWatcher.this.playlistStatusListener.shouldUpdateTimeOnServer();
                }

                UPDATE_PLAYER_STATUS_TIMER = 0;
            }

            CHECK_PENDING_DOWNLOADS_TIMER = CHECK_PENDING_DOWNLOADS_TIMER + 1000 * PLAYLIST_TIMER_CHECK_TIMER;

            if (CHECK_PENDING_DOWNLOADS_TIME == CHECK_PENDING_DOWNLOADS_TIMER){

                if (PlaylistWatcher.this.playlistStatusListener != null){
                    PlaylistWatcher.this.playlistStatusListener.checkForPendingDownloads();
                }

                CHECK_PENDING_DOWNLOADS_TIMER = 0;
            }

            if (currentDayOfTheWeek == -1){
                currentDayOfTheWeek = Utilities.getCurrentDayNumber();
            }

            if (currentDayOfTheWeek != Utilities.getCurrentDayNumber()){
                currentDayOfTheWeek = Utilities.getCurrentDayNumber();
                context.startActivity(new Intent(context, Splash_Activity.class));
                Activity activity = (Activity)context;
                activity.finish();
            }

            ArrayList<Playlist> playlists = new PlaylistManager(context,null).getPlaylistForCurrentTimeOnly();

            if (playlists == null){

            }

            int playlistStatus = -1;

            boolean shouldPlaylistChange = false;

            if (playlists != null && playlists.size() == 0){

                playlistStatus = NO_PLAYLIST;
                currentPlaylistID = "";

            } else if (playlists != null && playlists.size() > 0){

            /*Playlist is present but this is next playlist with no time gap.*/

                String playlistId = playlists.get(0).getsplPlaylist_Id();

            /*Override the default value of current playing playlist*/
                if (currentPlaylistID.equals("")){
                    currentPlaylistID = playlistId;
                }

            /*Check if current playing playlist is equal playlist provided by database.
            * If not, change the playlist*/

                if (!currentPlaylistID.equals(playlistId)){

//                    Toast.makeText(context, "Playlist should change", Toast.LENGTH_SHORT).show();

                    shouldPlaylistChange = true;
                    currentPlaylistID = playlistId;

                    if (playlistStatusListener != null){
                        playlistStatusListener.onPlaylistStatusChanged(PLAYLIST_CHANGE);
                    }
                }

                playlistStatus = PLAYLIST_PRESENT;
            }

            if (AlenkaMedia.playlistStatus == -12){
                AlenkaMedia.playlistStatus = playlistStatus;
            }

            if (AlenkaMedia.playlistStatus != playlistStatus){
                AlenkaMedia.playlistStatus = playlistStatus;

                if (playlistStatusListener != null){
                    playlistStatusListener.onPlaylistStatusChanged(playlistStatus);
                }

            }

            if (!isPaused)
            mHandler.postDelayed(runnable,1000 * PLAYLIST_TIMER_CHECK_TIMER);
        }
    };

    public void setWatcher()
    {
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.postDelayed(runnable,1000 * PLAYLIST_TIMER_CHECK_TIMER);
    }

    public void pausePlaylistWatcher(){

        isPaused = true;
    }

    public void resumePlaylistWatcher(){
        isPaused = false;
        mHandler.postDelayed(runnable,1000 * PLAYLIST_TIMER_CHECK_TIMER);
    }

    public void cancelWatcher()
    {

        if (mHandler != null)
        mHandler.removeCallbacks(runnable);
    }

    private void setAdvertisements(){

        String advertisementTypeMinute =  SharedPreferenceUtil.getStringPreference(this.context,
                AlenkaMediaPreferences.is_Minute_Adv);

        String advertisementTypeSong =  SharedPreferenceUtil.getStringPreference(this.context,
                AlenkaMediaPreferences.is_song_Adv);

        String advertisementTypeTime =  SharedPreferenceUtil.getStringPreference(this.context,
                AlenkaMediaPreferences.is_Time_Adv);


        if (advertisementTypeMinute != null && !advertisementTypeMinute.equals("")){
            ADVERTISEMENT_TYPE = "1";

            String timeAfterAdvertisement =  SharedPreferenceUtil.getStringPreference(this.context,
                    AlenkaMediaPreferences.total_minute_after_adv_play);

            if (timeAfterAdvertisement != null && !timeAfterAdvertisement.equals("")){
                TIME_AFTER_ADVERTISEMENT_SHOULD_PLAY = Integer.valueOf(timeAfterAdvertisement);
//                TIME_AFTER_ADVERTISEMENT_SHOULD_PLAY = 1; // TODO Remove this
                ADVERTISEMENT_TIME = TIME_AFTER_ADVERTISEMENT_SHOULD_PLAY * 60000;
            }

        } else if(advertisementTypeSong != null && !advertisementTypeSong.equals("")){
            ADVERTISEMENT_TYPE = "2";

            String playSongsAfterAdvertisement =  SharedPreferenceUtil.getStringPreference(this.context,
                    AlenkaMediaPreferences.total_Songs);

            PLAY_AD_AFTER_SONGS = Integer.valueOf(playSongsAfterAdvertisement);

        } else if(advertisementTypeTime != null && !advertisementTypeTime.equals("")){
            ADVERTISEMENT_TYPE = "3";


            if (ADVERTISEMENT_PLAY_TIME.equals("")){

                if (advertisements == null)
                advertisements = new AdvertisementsManager(PlaylistWatcher.this.context).
                        getAdvertisementsForComingTime();

                if (advertisements.size() > 0){

                    String playAdAtTime = advertisements.get(currentlyPlayingAdAtIndex).getsTime();

                    if (playAdAtTime != null){
                        ADVERTISEMENT_PLAY_TIME = playAdAtTime;
                    }
                }

            }
        }

    }
}
