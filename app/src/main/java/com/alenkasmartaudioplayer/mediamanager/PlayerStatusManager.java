package com.alenkasmartaudioplayer.mediamanager;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.alenkasmartaudioplayer.BuildConfig;
import com.alenkasmartaudioplayer.api_manager.OkHttpUtil;
import com.alenkasmartaudioplayer.database.PlayerStatusDataSource;
import com.alenkasmartaudioplayer.models.PlayerStatus;
import com.alenkasmartaudioplayer.models.Playlist;
import com.alenkasmartaudioplayer.models.Songs;
import com.alenkasmartaudioplayer.utils.Constants;
import com.alenkasmartaudioplayer.utils.SharedPreferenceUtil;
import com.alenkasmartaudioplayer.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by love on 4/6/17.
 */
public class PlayerStatusManager implements OkHttpUtil.OkHttpResponse {

    private PlayerStatusDataSource playerStatusDataSource;

    private Context context;

    public String artist_id = "";
    public String title_id = "";
    public String spl_plalist_id = "";
    public String songsDownloaded = "";


    public PlayerStatusManager(Context context){
        this.context = context;
        playerStatusDataSource = new PlayerStatusDataSource(this.context);
    }

    public void insertSongPlayedStatus(){

        try {

            Calendar calendar;
            calendar =Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss aa", Locale.US);

            String played_date_Time = simpleDateFormat.format(calendar.getTime());


            PlayerStatus playerStatus = new PlayerStatus();
            playerStatus.setArtistIdStatusSong(artist_id);
            playerStatus.setTitleIdSong(title_id);
            playerStatus.setSplPlaylistIdSong(spl_plalist_id);
            playerStatus.setPlayerDateTimeSong(played_date_Time);
            playerStatus.setPlayerStatusAll("song");


            playerStatusDataSource.open();
            playerStatusDataSource.createPlayerStatus(playerStatus);
            playerStatusDataSource.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertAdvPlayerStatus(PlayerStatus playerStatus){


        try {

            Calendar calendar;
            calendar =Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss aa",Locale.US);

            String played_date_Time = simpleDateFormat.format(calendar.getTime());
            playerStatus.setPlayerDateTimeSong(played_date_Time);

            playerStatusDataSource.open();
            playerStatusDataSource.createPlayerStatus(playerStatus);
            playerStatusDataSource.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* This method is for login status where we will insert and send data to server that we have saved locally  */

    public void updateLoginStatus(){

        try {

            String currentDate = Utilities.currentDate();
            String currenttime = Utilities.currentTime();

            PlayerStatus playerStatus = new PlayerStatus();
            playerStatus.setLoginDate(currentDate);
            playerStatus.setLoginTime(currenttime);
            playerStatus.setPlayerStatusAll("login");

            playerStatusDataSource.open();
            playerStatusDataSource.createPlayerStatus(playerStatus);
            playerStatusDataSource.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*Update the player logout time in database.*/
    public void updateLogoutStatus(){

        String currentDate = Utilities.currentDate();
        String currenttime = Utilities.currentTime();

        PlayerStatus playerStatus = new PlayerStatus();
        playerStatus.setLogoutDate(currentDate);
        playerStatus.setLogoutTime(currenttime);
        playerStatus.setPlayerStatusAll("logout");

        try {
            playerStatusDataSource.open();
            playerStatusDataSource.createPlayerStatus(playerStatus);
            playerStatusDataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            updateLogoutStatusOnServer();
        }
    }

    private void updateLogoutStatusOnServer(){

        try {
            playerStatusDataSource.open();

            ArrayList<PlayerStatus> arrayList = playerStatusDataSource.getPlayedSongs("logout");

            JSONArray jsonArray = new JSONArray();
            if(arrayList.size()>0) {
                for (int i = 0; i < arrayList.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("LogoutDate", arrayList.get(i).getLogoutDate());
                    jsonObject.put("LogoutTime", arrayList.get(i).getLogoutTime());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonArray.put(jsonObject);
                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }

//            return jsonArray.toString();
//            Toast.makeText(this.context, "Sending login status", Toast.LENGTH_SHORT).show();

            new OkHttpUtil(this.context,Constants.PLAYER_LOGOUT_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYER_LOGOUT_STATUS_STREAM_TAG).
                    callRequest();

        }catch (Exception e){
            e.printStackTrace();
        }
//        return null;
    }

    public void updateHeartBeatStatus(){

        Calendar calendar;
        calendar =Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss aa",Locale.US);
        String played_date_time = simpleDateFormat.format(calendar.getTime());

        PlayerStatus playerStatus = new PlayerStatus();
        playerStatus.setHeartbeatDateTimeStatus(played_date_time);
        playerStatus.setPlayerStatusAll("heartbeat");

        try {

            playerStatusDataSource.open();
            playerStatusDataSource.createPlayerStatus(playerStatus);
            playerStatusDataSource.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRecordsPlayerSatus(String type){
        try {
            playerStatusDataSource.open();
            playerStatusDataSource.deletePlayedStatus(type);
            playerStatusDataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateDataOnServer(){

        try {
            playerStatusDataSource.open();

            ArrayList<PlayerStatus> arrayList = playerStatusDataSource.getPlayedSongs("login");

            JSONArray jsonArray = new JSONArray();
            if(arrayList.size()>0) {
                for (int i = 0; i < arrayList.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("LoginDate", arrayList.get(i).getLoginDate());
                    jsonObject.put("LoginTime", arrayList.get(i).getLoginTime());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonArray.put(jsonObject);
                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }

//            return jsonArray.toString();
//            Toast.makeText(this.context, "Sending login status", Toast.LENGTH_SHORT).show();
/*
            new OkHttpUtil(this.context,Constants.PLAYER_LOGIN_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYER_LOGIN_STATUS_STREAM_TAG).
                    execute();
*/

            new OkHttpUtil(this.context,Constants.PLAYER_LOGIN_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYER_LOGIN_STATUS_STREAM_TAG).
                    callRequest();

        }catch (Exception e){
            e.printStackTrace();
        }
//        return null;
    }

    public void sendPlayedSongsStatusOnServer(){

        try{

            playerStatusDataSource.open();

            ArrayList<PlayerStatus> arrayList = playerStatusDataSource.getPlayedSongsNew("song");


            Collections.sort(arrayList, new Comparator<PlayerStatus>() {
                @Override
                public int compare(PlayerStatus playerStatus, PlayerStatus t1) {

                    Calendar calendar;
                    calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss aa",Locale.US);

                    try {
                        Date played_date_Time1 = simpleDateFormat.parse(playerStatus.getPlayerDateTimeSong());
                        Date played_date_Time2 = simpleDateFormat.parse(t1.getPlayerDateTimeSong());

                        return played_date_Time1.compareTo(played_date_Time2);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    };

                    return 0;
                }
            });

            JSONArray jsonArray = new JSONArray();

            if(arrayList.size()>0) {

                int run = arrayList.size() < 20 ? arrayList.size() : 20;

                for (int i = 0; i < run; i++) {

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("ArtistId", arrayList.get(i).getArtistIdStatusSong());
                    jsonObject.put("PlayedDateTime", arrayList.get(i).getPlayerDateTimeSong());
                    jsonObject.put("splPlaylistId", arrayList.get(i).getSplPlaylistIdSong());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonObject.put("TitleId", arrayList.get(i).getTitleIdSong());
                    jsonArray.put(jsonObject);

                    //TODO: Here delete the song status one by one from database table
                    //TODO: Here delete the song status one by one from database table
                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }
            Utilities.showToast(this.context,"Sending player status");

            new OkHttpUtil(context,Constants.PLAYED_SONG_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_SONG_STATUS_STREAM_TAG).
                    execute();


     /*       new OkHttpUtil(context,Constants.PLAYED_SONG_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_SONG_STATUS_STREAM_TAG).
                    callRequest();*/

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateDownloadedSongsCountOnServer(){

        if (!songsDownloaded.equalsIgnoreCase("")) {

            JSONObject jsonObject = new JSONObject();

            try {

                jsonObject.put("totalSong",songsDownloaded);
                jsonObject.put("TokenId",SharedPreferenceUtil.getStringPreference(PlayerStatusManager.this.context,Constants.TOKEN_ID));
//                jsonObject.put("verNo",Utilities.getAndroidVersion());
                jsonObject.put("verNo", BuildConfig.VERSION_NAME);

            }catch (Exception e){
                e.printStackTrace();
            }

            new OkHttpUtil(this.context,Constants.DOWNLOADINGPROCESS,jsonObject.toString(),
                    PlayerStatusManager.this,false,
                    Constants.DOWNLOADINGPROCESS_TAG).
                    callRequest();
        }

    }

    private void sendPlayedAdsStatusOnServer(){

        try{

            playerStatusDataSource.open();
            ArrayList<PlayerStatus> arrayList = playerStatusDataSource.getPlayedSongs("adv");

            JSONArray jsonArray = new JSONArray();

            if(arrayList.size()>0) {
                for (int i = 0; i < arrayList.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("AdvtId", arrayList.get(i).getAdvIdStatus());
                    jsonObject.put("PlayedDateTime", arrayList.get(i).getPlayerDateTimeSong());
//                    jsonObject.put("PlayedDate", arrayList.get(i).getAdvPlayedDate());
//                    jsonObject.put("PlayedTime", arrayList.get(i).getAdvPlayedTime());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context, Constants.TOKEN_ID));
                    jsonArray.put(jsonObject);

                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }
//            Toast.makeText(this.context, "Sending played ads status", Toast.LENGTH_SHORT).show();

            new OkHttpUtil(context,Constants.PLAYED_ADVERTISEMENT_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_ADVERTISEMENT_TAG).
                    execute();


           /* new OkHttpUtil(context,Constants.PLAYED_ADVERTISEMENT_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_ADVERTISEMENT_TAG).
                    callRequest();*/

        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private void sendHeartBeatStatusOnServer(){
        try{

            playerStatusDataSource.open();

            ArrayList<PlayerStatus> arrayList = playerStatusDataSource.getPlayedSongs("heartbeat");
            JSONArray jsonArray = new JSONArray();
            if(arrayList.size()>0) {
                for (int i = 0; i < arrayList.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("HeartbeatDateTime", arrayList.get(i).getHeartbeatDateTimeStatus());
                    jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context,Constants.TOKEN_ID));
                    jsonArray.put(jsonObject);
                }
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }
            /*new OkHttpUtil(context,Constants.PLAYER_HEARTBEAT_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYER_HEARTBEAT_STATUS_STREAM_TAG).
                    execute();*/

                        new OkHttpUtil(context,Constants.PLAYER_HEARTBEAT_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYER_HEARTBEAT_STATUS_STREAM_TAG).
                    callRequest();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResponse(String response, int tag) {

        if (response == null || response.equals("") || response.length() < 1){
            Utilities.showToast(this.context,"Empty response for player statuses");
            return;
        }

        switch (tag){
            case Constants.PLAYER_LOGIN_STATUS_STREAM_TAG:{
                handleUpdatedDataResponse(response);
            }break;

            case Constants.PLAYED_SONG_STATUS_STREAM_TAG:{
                handleUpdatedSongsResponse(response);
            }break;

            case Constants.PLAYER_HEARTBEAT_STATUS_STREAM_TAG:{
                handleUpdatedHeartBeatStatusResponse(response);
            }break;
            case Constants.PLAYED_ADVERTISEMENT_TAG:{
                handleUpdatedAdvertisementStatusResponse(response);
            }break;
            case Constants.PLAYER_LOGOUT_STATUS_STREAM_TAG:{
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }break;
            case Constants.DOWNLOADINGPROCESS_TAG:{
                Log.e("Updated download status",response);
            }break;
            case Constants.UPDATE_PLAYLIST_DOWNLOADED_SONGS_TAG:{
                handleUpdatedPlaylistWiseDownloadedSongs(response);
            }break;
            case Constants.UPDATE_PLAYLIST_SONGS_DETAILS_TAG:{
                Log.e("Updated download status",response);
            }break;
        }
    }

    @Override
    public void onError(Exception e, int tag) {
        e.printStackTrace();
    }

    private void handleUpdatedDataResponse(String response){
        try {
            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            Log.d("Response for update",Response);
            if (Response.equals("1")){
                deleteRecordsPlayerSatus("login");

            }
            /*Update played songs status*/
//            sendPlayedSongsStatusOnServer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdatedSongsResponse(String response){

        try {
            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            Log.d("Response for update",Response);
            if (Response.equals("1")){
                deleteRecordsPlayerSatus("login");
            }

            JSONArray songsArray = new JSONArray(response).getJSONObject(0).getJSONArray("SongArray");

                    if (songsArray != null && songsArray.length() > 0) {

                        playerStatusDataSource.open();

                        ArrayList<PlayerStatus> playedSongsArrayList = playerStatusDataSource.getPlayedSongsNew("song");

                        for (int count = 0; count < songsArray.length(); count++) {

                            JSONObject song = songsArray.getJSONObject(count);

                            if (song != null){

                                String songTitleId = song.getString("returnTitleId");
                                String songPlayedTime = song.getString("returnPlayedDateTime");
                                String songUpdated = song.getString("Response");

                                if (songTitleId != null && songPlayedTime != null && songUpdated != null){

                                    if (playedSongsArrayList != null && playedSongsArrayList.size() > 0) {

                                        for (PlayerStatus playerStatus : playedSongsArrayList) {

                                            if (songTitleId.equalsIgnoreCase(playerStatus.getTitleIdSong())
                                                    && songPlayedTime.equalsIgnoreCase(playerStatus.getPlayerDateTimeSong())
                                                    && songUpdated.equalsIgnoreCase("1")){

                                                playerStatusDataSource.deletePlayedSongStatusForTime("song", songTitleId,songPlayedTime);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        playerStatusDataSource.close();
                    }

            /*Update heartbeat status*/
//            sendHeartBeatStatusOnServer();

            sendPlayedAdsStatusOnServer();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateDownloadedSongsPlaylistWise(){

        try{

            PlaylistManager playlistManager = new PlaylistManager(this.context,null);

            ArrayList<Playlist> arrayList = playlistManager.getAllPlaylistInPlayingOrder();

            JSONArray jsonArray = new JSONArray();

            if(arrayList.size()>0) {

                for (int i = 0; i < arrayList.size(); i++) {

                    JSONObject jsonObject = new JSONObject();

                    Playlist playlist = arrayList.get(i);

                    ArrayList<Songs> arrSongs = playlistManager.getDownloadedSongsForPlaylist(playlist.getsplPlaylist_Id());

                    if (arrSongs == null) {
                        jsonObject.put("totalSong", "0");
                    }
                    else {
                        jsonObject.put("totalSong", arrSongs.size());
                    }

                    jsonObject.put("splPlaylistId",playlist.getsplPlaylist_Id());
                    jsonObject.put("TokenId",SharedPreferenceUtil.getStringPreference(this.context,Constants.TOKEN_ID));
                    jsonArray.put(jsonObject);
                }

            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }
//            Toast.makeText(this.context, "Sending player status", Toast.LENGTH_SHORT).show();
/*
            new OkHttpUtil(context,Constants.PLAYED_SONG_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_SONG_STATUS_STREAM_TAG).
                    execute();
*/

            new OkHttpUtil(context,Constants.UPDATE_PLAYLIST_DOWNLOADED_SONGS,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.UPDATE_PLAYLIST_DOWNLOADED_SONGS_TAG).
                    execute();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void handleUpdatedPlaylistWiseDownloadedSongs(String response) {


        try {
            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            Log.d("Response for update",Response);
            if (Response.equals("1")){

//                Toast.makeText(this.context, "All status updated", Toast.LENGTH_SHORT).show();
            }

            updateDownloadedSongsPlaylistDetails();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDownloadedSongsPlaylistDetails(){

        try{

            PlaylistManager playlistManager = new PlaylistManager(this.context,null);

            ArrayList<Playlist> arrayListRepeated = playlistManager.getAllPlaylistInPlayingOrder();

            List<Playlist> arrayList = new ArrayList<Playlist>();

            for (Playlist event : arrayListRepeated) {

                boolean isFound = false;
                // check if the event name exists in noRepeat
                for (Playlist e : arrayList) {
                    if (e.getsplPlaylist_Id().equalsIgnoreCase(event.getsplPlaylist_Id()))
                        isFound = true;
                }

                if (!isFound) {
                    arrayList.add(event);
                }
            }

            JSONArray jsonArray = new JSONArray();

            if(arrayList.size()>0) {

                for (int i = 0; i < arrayList.size(); i++) {

                    JSONObject jsonObject = new JSONObject();

                    Playlist playlist = arrayList.get(i);

                    ArrayList<Songs> arrSongs = playlistManager.getDownloadedSongsForPlaylist(playlist.getsplPlaylist_Id());

                    if (arrSongs == null) {
                        jsonObject.put("titleIDArray", new JSONArray());
                    }
                    else {

                        JSONArray titleIDArray = new JSONArray();

                        for (int j = 0; j < arrSongs.size(); j++){

                            Songs songs = arrSongs.get(j);
                            titleIDArray.put(songs.getTitle_Id());

                        }

                        jsonObject.put("titleIDArray", titleIDArray);
                    }

                    jsonObject.put("splPlaylistId",playlist.getsplPlaylist_Id());
                    jsonObject.put("TokenId",SharedPreferenceUtil.getStringPreference(this.context,Constants.TOKEN_ID));

                    jsonArray.put(jsonObject);
                }

            }else {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(jsonObject);
            }
//            Toast.makeText(this.context, "Sending player status", Toast.LENGTH_SHORT).show();
/*
            new OkHttpUtil(context,Constants.PLAYED_SONG_STATUS_STREAM,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.PLAYED_SONG_STATUS_STREAM_TAG).
                    execute();
*/

            new OkHttpUtil(context,Constants.UPDATE_PLAYLIST_SONGS_DETAILS,jsonArray.toString(),
                    PlayerStatusManager.this,false,
                    Constants.UPDATE_PLAYLIST_SONGS_DETAILS_TAG).
                    execute();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void handleUpdatedHeartBeatStatusResponse(String response){

        try {
            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            Log.d("Response for update",Response);
            if (Response.equals("1")){
                deleteRecordsPlayerSatus("heartbeat");
//                Toast.makeText(this.context, "All status updated", Toast.LENGTH_SHORT).show();
            }
            sendPlayedAdsStatusOnServer();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleUpdatedAdvertisementStatusResponse(String response){

        try {
            String Response = new JSONArray(response).getJSONObject(0).getString("Response");

            Log.d("Response for update",Response);

            if (Response.equals("1")){
               // deleteRecordsPlayerSatus("login");
            }

            JSONArray songsArray = new JSONArray(response).getJSONObject(0).getJSONArray("SongArray");

            if (songsArray != null && songsArray.length() > 0) {

                playerStatusDataSource.open();

                ArrayList<PlayerStatus> playedSongsArrayList = playerStatusDataSource.getPlayedSongsNew("adv");

                for (int count = 0; count < songsArray.length(); count++) {

                    JSONObject song = songsArray.getJSONObject(count);

                    if (song != null){

                        String songTitleId = song.getString("returnTitleId");
                        String songPlayedTime = song.getString("returnPlayedDateTime");
                        String songUpdated = song.getString("Response");

                        if (songTitleId != null && songPlayedTime != null && songUpdated != null){

                            if (playedSongsArrayList != null && playedSongsArrayList.size() > 0) {

                                for (PlayerStatus playerStatus : playedSongsArrayList) {

                                    if (songTitleId.equalsIgnoreCase(playerStatus.getAdvIdStatus())
                                            && songPlayedTime.equalsIgnoreCase(playerStatus.getPlayerDateTimeSong())
                                            && songUpdated.equalsIgnoreCase("1")){

                                        playerStatusDataSource.deletePlayedAdvStatus("adv", songTitleId,songPlayedTime);

                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                playerStatusDataSource.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            updateDownloadedSongsPlaylistWise();
        }

    }
}
