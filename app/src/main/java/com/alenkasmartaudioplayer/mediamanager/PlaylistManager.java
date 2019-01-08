package com.alenkasmartaudioplayer.mediamanager;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.alenkasmartaudioplayer.R;
import com.alenkasmartaudioplayer.activities.LoginActivity;
import com.alenkasmartaudioplayer.activities.Splash_Activity;
import com.alenkasmartaudioplayer.api_manager.OkHttpUtil;
import com.alenkasmartaudioplayer.application.AlenkaMedia;
import com.alenkasmartaudioplayer.database.AdvertisementDataSource;
import com.alenkasmartaudioplayer.database.PlaylistDataSource;
import com.alenkasmartaudioplayer.database.SongsDataSource;
import com.alenkasmartaudioplayer.interfaces.PlaylistLoaderListener;
import com.alenkasmartaudioplayer.models.Advertisements;
import com.alenkasmartaudioplayer.models.Playlist;
import com.alenkasmartaudioplayer.models.Songs;
import com.alenkasmartaudioplayer.utils.AlenkaMediaPreferences;
import com.alenkasmartaudioplayer.utils.Constants;
import com.alenkasmartaudioplayer.utils.ExternalStorage;
import com.alenkasmartaudioplayer.utils.SharedPreferenceUtil;
import com.alenkasmartaudioplayer.utils.Utilities;
import com.bugfender.sdk.Bugfender;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.sql.Array;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by love on 29/5/17.
 */
public class PlaylistManager implements OkHttpUtil.OkHttpResponse {

    public static final String TAG = "PlaylistManager";

    Context context;

    PlaylistDataSource playlistDataSource = null;

    PlaylistLoaderListener playlistLoaderListener;

    ArrayList<String> schIdArrayList = new ArrayList<String>();

    SongsDataSource songsDataSource = null;

    int currentlyDownloadingSongsFromPlaylistAtIndex = 0;

    ArrayList<String> titleId = new ArrayList<>();

    String splid;

    private ArrayList<Playlist> playlists = new ArrayList<>();

    private AdvertisementDataSource advertisementDataSource;

    public PlaylistManager(Context context, PlaylistLoaderListener playlistLoaderListener){
        this.context = context;
        playlistDataSource = new PlaylistDataSource(this.context);
        songsDataSource = new SongsDataSource(this.context);
        advertisementDataSource = new AdvertisementDataSource(this.context);
        this.playlistLoaderListener = playlistLoaderListener;
    }

    public void getPlaylistsFromServer(){

        if (this.playlistLoaderListener != null){
            this.playlistLoaderListener.startedGettingPlaylist();
        }

        try{
            JSONObject json = new JSONObject();

            json.put("DfClientId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.DFCLIENT_ID));
            json.put("TokenId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.TOKEN_ID));
            json.put("WeekNo", Utilities.getCurrentDayNumber());
            json.put("CurrentDateTime", Utilities.currentFormattedDate());
/*
            new OkHttpUtil(context, Constants.GetSplPlaylist_VIDEO,json.toString(),
                    PlaylistManager.this,false,
                    Constants.GetSplPlaylist_TAG).
                    execute();
*/

            Utilities.showToast(context, "Getting playlist");
            new OkHttpUtil(context, Constants.GetSplPlaylistDateWise,json.toString(),
                    PlaylistManager.this,false,
                    Constants.GetSplPlaylistDateWise_TAG).
                    callRequest();


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getAdvertisements(){

        try{
            JSONObject json = new JSONObject();

            json.put("Cityid", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.City_ID));
            json.put("CountryId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.Country_ID));
            json.put("CurrentDate", Utilities.currentFormattedDate());
            json.put("DfClientId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.DFCLIENT_ID));
            json.put("StateId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.State_Id));
            json.put("TokenId", SharedPreferenceUtil.getStringPreference(context, AlenkaMediaPreferences.TOKEN_ID));
            json.put("WeekNo", Utilities.getDayNumberForAdv());

            new OkHttpUtil(context, Constants.ADVERTISEMENTS,json.toString(),
                    PlaylistManager.this,false,
                    Constants.ADVERTISEMENTS_TAG).
                    callRequest();


        } catch (Exception e){
            e.printStackTrace();
        }


    }

    public ArrayList<Playlist> getPlaylistForCurrentAndComingTime(){

        try {
            playlistDataSource.open();
            /*Commented method returns the playlist for current time only and not for the future times.*/
//            return playlistDataSource.getAllPlaylists();

            /*This method returns the playlist for current time and the future times.*/
            return playlistDataSource.getPlaylistsForCurrentAndComingTime();
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            playlistDataSource.close();
        }
        return null;
    }

    /*This method returns playlists for current time only and not for future times.*/

    public ArrayList<Playlist> getPlaylistForCurrentTimeOnly(){

        try {
            playlistDataSource.open();

            return playlistDataSource.getAllPlaylists();

        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            playlistDataSource.close();
        }
        return null;
    }

    @Override
    public void onResponse(String response, int tag) {

        if (response == null){
            Utilities.showToast(this.context, "Empty response");

            // If empty response from server and we have songs available in local database, then start the player.
            if (tag == Constants.GET_SPL_PLAY_LIST_TITLES_TAG || tag == Constants.GetSplPlaylistDateWise_TAG){

                ArrayList<Songs> downloadedSongs = getAllDownloadedSongs();

                if (downloadedSongs != null && downloadedSongs.size() > 0) {

                    if (this.playlistLoaderListener != null){
                        this.playlistLoaderListener.finishedGettingPlaylist();
                    }
                }
            }
            return;
        }

        switch(tag){

            case Constants.GetSplPlaylistDateWise_TAG :{
//                Toast.makeText(this.context, "GetSplPlaylist_TAG", Toast.LENGTH_SHORT).show();
                handleGettingPlaylistResponse(response);
            }break;

            case Constants.GET_SPL_PLAY_LIST_TITLES_TAG:{
//                Toast.makeText(this.context, "GET_SPL_PLAY_LIST_TITLES_TAG", Toast.LENGTH_SHORT).show();
                handleGetSongsResponse(response);
            }break;

            case Constants.ADVERTISEMENTS_TAG:{
//                Toast.makeText(this.context, "ADVERTISEMENTS_TAG", Toast.LENGTH_SHORT).show();
                handleResponseForAdvertisements(response);
            }break;

            case Constants.CHECK_TOKEN_PUBLISH_TAG:{
                handleResponseForTokenPublish(response);
            }break;

            case Constants.UPDATE_TOKEN_PUBLISH_TAG:{
                handleResponseForTokenUpdatedOnServer(response);
            }break;
        }

    }

    private void handleResponseForTokenUpdatedOnServer(String response) {

        try {

            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray != null){

                if (jsonArray.length() > 0){

                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    String isPublishUpdate = jsonObject.getString("IsPublishUpdate");

                    if (isPublishUpdate.equals("1")){

                        if (this.playlistLoaderListener != null){
                            this.playlistLoaderListener.tokenUpdatedOnServer();
                        }

                    }
                }
            }

        }catch (Exception e){

            e.printStackTrace();
        }
    }

    private void handleResponseForTokenPublish(String response) {

        try {

            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray != null){

                if (jsonArray.length() > 0){

                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    String isPublishUpdate = jsonObject.getString("IsPublishUpdate");

                    Log.e(TAG,"IsPublishUpdate value: " + isPublishUpdate);

                    if (isPublishUpdate.equals("1")){

                        Log.e(TAG,"Get new data.");
                        AlenkaMedia.getInstance().isUpdateInProgress = true;
                        getPlaylistsFromServer();
                        return;
                    }
                }
            }

            AlenkaMedia.getInstance().isUpdateInProgress = false;

        }catch (Exception e){

            e.printStackTrace();
        }
    }

    @Override
    public void onError(Exception e, int tag) {
        if (this.playlistLoaderListener != null){
            this.playlistLoaderListener.errorInGettingPlaylist(e);
        }
    }

    private void handleGettingPlaylistResponse(String response) {

        try {

            playlistDataSource.open();
            schIdArrayList.clear();
            JSONArray jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Playlist modal = new Playlist();
                String startTime = jsonObject.getString("StartTime");
                String endTime = jsonObject.getString("EndTime");

                //TODO: Here we change the format of date & Time
                String startTime1 = Utilities.changeDateFormat(startTime);
                String endTime1 = Utilities.changeDateFormat(endTime);

                //TODO: Here change the Date & Time in Milliseconds

                long startTimeInMilli = Utilities.getTimeInMilliSec(startTime1);
                long endTimeInMilli = Utilities.getTimeInMilliSec(endTime1);
                modal.setStart_Time_In_Milli(startTimeInMilli);
                modal.setEnd_Time_In_Milli(endTimeInMilli);
                modal.setStart_time(startTime);
                modal.setEnd_time(endTime);

                modal.setFormat_id(jsonObject.getString("FormatId"));
                modal.setdfclient_id(jsonObject.getString("dfclientid"));
                modal.setpSc_id(jsonObject.getString("pScid"));
                modal.setsplPlaylist_Id(jsonObject.getString("splPlaylistId"));
                modal.setsplPlaylist_Name(jsonObject.getString("splPlaylistName"));
                modal.setIsSeparatinActive(jsonObject.getLong("IsSeprationActive"));

                if (jsonObject.has("IsFadingActive")){
                    modal.setIsFadingActive(jsonObject.getLong("IsFadingActive"));
                }

                schIdArrayList.add(modal.getpSc_id());
                playlistDataSource.checkifPlaylistExist(modal);

            }

        } catch (SQLException e) {
            if (this.playlistLoaderListener != null){
                this.playlistLoaderListener.errorInGettingPlaylist(e);
            }
            e.printStackTrace();
        } catch (JSONException e) {
            if (this.playlistLoaderListener != null){
                this.playlistLoaderListener.errorInGettingPlaylist(e);
            }
            e.printStackTrace();
        } finally {
            playlistDataSource.close();

            if (this.playlistLoaderListener != null){
//                this.playlistLoaderListener.finishedGettingPlaylist();
            }
            deletExtraPlaylists();

            Utilities.showToast(PlaylistManager.this.context,"Getting Songs");

            getSongsForAllPlaylists();
        }
    }

    private ArrayList<Songs> getSongsToBeDownloaded(){

        ArrayList<Playlist> playlists = new PlaylistManager(PlaylistManager.this.context, null).getPlaylistFromLocallyToBedDownload();
        ArrayList<Songs> songsToBeDownloaded = null;

        if (playlists.size() > 0) {

            PlaylistManager songsLoader = new PlaylistManager(PlaylistManager.this.context, null);
            songsToBeDownloaded = new ArrayList<>();
            for (Playlist playlist : playlists) {

                ArrayList<Songs> songs = songsLoader.getSongsThatAreNotDownloaded(playlist.getsplPlaylist_Id());

                if (songs != null && songs.size() > 0) {

//                    if (playlist.getIsSeparatinActive() == 0){
//                    }

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

    private void handleGetSongsResponse(String response){

        if (response.equalsIgnoreCase("[]")){

            if (getSongsToBeDownloaded() != null && getSongsToBeDownloaded().size() > 0){

                if (this.playlistLoaderListener != null){
                    this.playlistLoaderListener.finishedGettingPlaylist();
                }

                try {
                    Activity activity = (Activity)this.context;

                    if (activity instanceof Splash_Activity || activity instanceof LoginActivity){
                        activity.finish();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            return;
        }

        try {

            titleId.clear();
            songsDataSource.open();

            //Map<String,String> downloadedSongsFromLocalStorage = getAlreadyDownloadedSongsFromLocalStorage();


            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray != null && jsonArray.length() > 0){

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Songs modal = new Songs();
                    modal.setAlbum_ID(jsonObject.getString("AlbumID"));
                    modal.setArtist_ID(jsonObject.getString("ArtistID"));
                    modal.setTitle(jsonObject.getString("Title"));
                    modal.setTitle_Url(jsonObject.getString("TitleUrl"));
                    modal.setAl_Name(jsonObject.getString("alName"));
                    modal.setAr_Name(jsonObject.getString("arName"));
                    modal.setSpl_PlaylistId(jsonObject.getString("splPlaylistId"));
                    modal.setT_Time(jsonObject.getString("tTime"));
                    modal.setTitle_Id(jsonObject.getString("titleId"));
                    modal.setSerialNo(jsonObject.getLong("srno"));

                    titleId.add(modal.getTitle_Id());
                    splid = modal.getSpl_PlaylistId();

                 /*   String songId = jsonObject.getString("titleId");

                    if (downloadedSongsFromLocalStorage.containsKey(songId)) {

                        modal.setIs_Downloaded(1);
                        modal.setSongPath(downloadedSongsFromLocalStorage.get(songId));

                    } else {
                        modal.setIs_Downloaded(0);
                    }*/
                    modal.setIs_Downloaded(0);

                    // TODO: Check for song if song exist then skip else insert
                    songsDataSource.checkifSongExist(modal, this.context);
//                    modalSongList.add(modal);
                }

            } else {

                Utilities.showToast(this.context, "No songs in playlist.");
            }

        }catch (Exception e){
            e.printStackTrace();

        }finally {

            songsDataSource.close();

            deleteExtraSongs();

            //If there are more playlists whose songs are not retreived get them

            if (currentlyDownloadingSongsFromPlaylistAtIndex < playlists.size() - 1){
                currentlyDownloadingSongsFromPlaylistAtIndex++;
                startDownloadingSongsForPlaylistWithPlaylistID(currentlyDownloadingSongsFromPlaylistAtIndex);
            } else {

                songsDataSource.close();

                getAdvertisements();
            }
        }
    }

    public ArrayList<String> storagePaths() {

        String[] mStrings = new String[3];

        String pathToUsb = "";

        try {

            if (Utilities.isVersionLowerThanLollipop()) {

                File[] pathsss = this.context.getExternalFilesDirs(null);

                Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();

                if (externalLocations.size() > 1) {

                    File usbDrive = pathsss[0];

                    pathToUsb = usbDrive.getAbsolutePath();

                } else {
                    pathToUsb = this.context.getApplicationInfo().dataDir;
                }

            } else {

                File[] pathsss = ContextCompat.getExternalFilesDirs(this.context.getApplicationContext(), null);

                for (int count = 0; count < pathsss.length; count++){

                    File drive = pathsss[count];

                    if (drive != null){

                        String filesDirectory = drive.getAbsolutePath();

                        if (filesDirectory.contains("files")){
                            String newPath = filesDirectory.substring(0, filesDirectory.lastIndexOf(File.separator));

                            if (newPath != null) {
                                mStrings[count] = newPath;
                            }
                        } else {
                            mStrings[count] = filesDirectory;
                        }
                    }

                }

            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> stringArrayList = new ArrayList<>();

        for (int count = 0; count < mStrings.length; count++) {

            String path = mStrings[count];

            if (path != null){

                File originalFilesFolder = new File(path);

                if (originalFilesFolder != null && originalFilesFolder.exists()) {

                    String filePath = originalFilesFolder.getAbsolutePath();
                    stringArrayList.add(filePath);

                }
            }


        }

        return stringArrayList;
    }

    public Map<String, String> getAlreadyDownloadedSongsFromLocalStorage() {

        ArrayList<String> localStorages = storagePaths();

        Map<String, String> songIdsOfDownloadedSongs = new HashMap<>();

        for (String path : localStorages) {

            File directory = new File(path);

            if (directory != null) {

                if (directory.isDirectory()){

                    String songName = directory.getAbsolutePath() + File.separator + "30063.mp3";

                    if (songName != null){

                        File songFile = new File(songName);

                        if (songFile != null && songFile.exists()){
                            Log.e(TAG,"Exists");
                        }
                    }

                    String[] strings = directory.list();

                    File[] downloadedSongs = directory.listFiles();

                    for (File song : downloadedSongs) {

                        String extension = FilenameUtils.getExtension(song.getAbsolutePath());

                        if (Constants.TAG_FILE_EXTENSION_MP3.contains(extension)) {

                            String songId = FilenameUtils.getBaseName(song.getAbsolutePath());

                            songIdsOfDownloadedSongs.put(songId, song.getAbsolutePath());

                        }
                    }
                }
            }

        }
        return songIdsOfDownloadedSongs;
    }

    public void traverse (final File dir,Map<String, String> songIdsOfDownloadedSongs ) {

        try {

            if (dir.exists()) {

                File[] files = dir.listFiles();

                if (files != null) {

                    for (int i = 0; i < files.length; ++i) {

                        File file = files[i];

                        if (file != null && file.isDirectory()) {
                            traverse(file, songIdsOfDownloadedSongs);
                        } else {

                            String extension = FilenameUtils.getExtension(file.getAbsolutePath());

                            if (Constants.TAG_FILE_EXTENSION_MP3.contains(extension)) {

                                String songId = FilenameUtils.getBaseName(file.getAbsolutePath());

                                songIdsOfDownloadedSongs.put(songId, file.getAbsolutePath());

                            }
                            // do something here with the file
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleResponseForAdvertisements(String response){

        try{

            advertisementDataSource.open();

            JSONArray jsonArray = new JSONArray(response);

//            jsonArray.remove(1);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if(jsonObject.getString("Response").equals("1")) {

                    Advertisements modal_Adv = new Advertisements();
                    //String Response = jsonObject.getString("Response");
                    modal_Adv.setAdvFileUrl(jsonObject.getString("AdvtFilePath"));
                    modal_Adv.setAdvtID(jsonObject.getString("AdvtId"));
                    modal_Adv.setAdvtName(jsonObject.getString("AdvtName"));
                    modal_Adv.setIsMinute(jsonObject.getString("IsMinute"));
                    modal_Adv.setIsSong(jsonObject.getString("IsSong"));
                    modal_Adv.setIsTime(jsonObject.getString("IsTime"));
                    modal_Adv.setPlayingType(jsonObject.getString("PlayingType"));
                    modal_Adv.setSRNo(jsonObject.getString("SrNo"));
                    modal_Adv.setTotalMinutes(jsonObject.getString("TotalMinutes"));
                    modal_Adv.setTotalSongs(jsonObject.getString("TotalSongs"));
                    modal_Adv.seteDate(jsonObject.getString("eDate"));
                    modal_Adv.setsDate(jsonObject.getString("sDate"));


                    modal_Adv.setsTime(jsonObject.getString("sTime"));

                    String edate = jsonObject.getString("eDate");
                    String sdate = jsonObject.getString("sDate");
                    String sTime = jsonObject.getString("sTime");

                   /* if (i == 0){
                        sTime = "12:15 AM";
                        modal_Adv.setsTime(sTime);
                    } else {
                        sTime = "12:18 AM";
                        modal_Adv.setsTime(sTime);
                    }*/

                    //TODO: Get sTime in milliseconds
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat mdformat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                    String strDate = mdformat.format(calendar.getTime());

                    String play_Adv_Time = strDate + " " + sTime;
                    //TODO : cahnge format using Prayer Method for Advertisement Type Is_Time
                    String formated_time = Utilities.changeDateFormatForPrayer(play_Adv_Time);
                    //TODO : get Time in milliseconds using Prayer Method for Advertisement Type Is_Time
                    long timeInMilli = Utilities.getTimeInMilliSecForPrayer(formated_time);
                    modal_Adv.setStart_Adv_Time_Millis(timeInMilli);
                    modal_Adv.setStatus_Download(0);

                    if(i == 0){

                        String playing_type = modal_Adv.getPlayingType(); // Hard Stop

                        String is_Minute = modal_Adv.getIsMinute(); //1
                        String totalMinutes = modal_Adv.getTotalMinutes(); // 5
//                        is_Minute = "1"; //TODO Remove this value - for testing only
//                        totalMinutes = "3";

                        String is_Song = modal_Adv.getIsSong(); // 0
                        String totalSongs = modal_Adv.getTotalSongs();
                    //    is_Song = "0";
                     //   totalSongs = "0";

                        String isTime = modal_Adv.getIsTime();
                    //    isTime = "1";

                        if (is_Minute.equals("1")){
                            SharedPreferenceUtil.setStringPreference(this.context,AlenkaMediaPreferences.is_song_Adv, "");
                            SharedPreferenceUtil.setStringPreference(this.context,AlenkaMediaPreferences.is_Minute_Adv, is_Minute);
                            SharedPreferenceUtil.setStringPreference(this.context,AlenkaMediaPreferences.total_minute_after_adv_play, totalMinutes);

                        } else if(is_Song.equals("1")){
                            SharedPreferenceUtil.setStringPreference(this.context,AlenkaMediaPreferences.is_Minute_Adv, "");
                            SharedPreferenceUtil.setStringPreference(this.context,AlenkaMediaPreferences.is_song_Adv, is_Song);
                            SharedPreferenceUtil.setStringPreference(this.context,AlenkaMediaPreferences.total_Songs, totalSongs);

                        } else if(isTime.equals("1")){
                            SharedPreferenceUtil.setStringPreference(this.context,AlenkaMediaPreferences.is_song_Adv, "");
                            SharedPreferenceUtil.setStringPreference(this.context,AlenkaMediaPreferences.is_Minute_Adv, "");
                            SharedPreferenceUtil.setStringPreference(this.context,AlenkaMediaPreferences.is_Time_Adv, isTime);

//                            playing_type = "Soft Stop";
                        }
                        SharedPreferenceUtil.setStringPreference(this.context,AlenkaMediaPreferences.playing_Type, playing_type); //// TODO: 17/6/17 Change this

                    }

                    advertisementDataSource.checkifExistAdv(modal_Adv);
                }else {

                    /*If advertisements are present delete them*/
                    ArrayList<Advertisements> arrayList = advertisementDataSource.getAllAdv();
                    if(arrayList.size()>0){
                        for (int k=0;i<=arrayList.size()-1;k++){
                            String pathAdvertisement = arrayList.get(k).getAdvtFilePath();
                            File file = new File(pathAdvertisement);
                            file.delete();
                        }
                        advertisementDataSource.deleteAdvIfNotInServer();
                    }
                    Log.e("DeleteAdvertisemnetData","Deleted");
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        } finally {

            advertisementDataSource.close();

            if (this.playlistLoaderListener != null){
                this.playlistLoaderListener.finishedGettingPlaylist();
            }

            try {
                Activity activity = (Activity)this.context;

                if (activity instanceof Splash_Activity || activity instanceof LoginActivity){
                    activity.finish();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public ArrayList<Songs> getAllDownloadedSongs(){

        try {
            songsDataSource.open();

            return songsDataSource.getAllDownloadedSongs();

        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getTotalDownloadedSongs(){

        try {
            songsDataSource.open();

            return songsDataSource.getCountForTotalSongsDownloaded();

        }catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalSongs(){

        try {
            songsDataSource.open();

            return songsDataSource.getCountForSongs();

        }catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    private void deleteExtraSongs(){

        /*try {

            songsDataSource.open();

            ArrayList<Songs> arrayList = songsDataSource.getSongListNotAvailableinWebResponse
                    (Arrays.copyOf(titleId.toArray(), titleId.toArray().length, String[].class),splid);
            if (arrayList.size() > 0) {
                for (int k = 0; k < arrayList.size(); k++) {
                    String songpath = arrayList.get(k).getSongPath();
                    File file = new File(songpath);

                    songsDataSource.deleteSongs(arrayList.get(k),false);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            songsDataSource.close();
        }Ranjeet 24 oct 2018*/
    }

    public void getSongsForAllPlaylists(){

        playlists.addAll(getAllPlaylistInPlayingOrder());

        if (playlists.size() > 0){

            startDownloadingSongsForPlaylistWithPlaylistID(currentlyDownloadingSongsFromPlaylistAtIndex);
        } else {

            if (this.playlistLoaderListener != null){
                this.playlistLoaderListener.finishedGettingPlaylist();
            }
            Utilities.showToast(this.context, "No songs for current time.");
        }

    }
    private void startDownloadingSongsForPlaylistWithPlaylistID(int index){

        getSongsForPlaylistId(playlists.get(index).getsplPlaylist_Id());

    }

    private void getSongsForPlaylistId(String playlistId){

        try {

            JSONObject json = new JSONObject();
            json.put("splPlaylistId", playlistId);
            Log.e(TAG, "json" + json);

/*
            new OkHttpUtil(context, Constants.GET_SPL_PLAY_LIST_TITLES_VIDEO,json.toString(),
                    PlaylistManager.this,false,
                    Constants.GET_SPL_PLAY_LIST_TITLES_TAG).
                    execute();
*/


            new OkHttpUtil(context, Constants.GET_SPL_PLAY_LIST_TITLES,json.toString(),
                    PlaylistManager.this,false,
                    Constants.GET_SPL_PLAY_LIST_TITLES_TAG).
                    callRequest();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void deletExtraPlaylists(){
        try{
            playlistDataSource.open();

            ArrayList<Playlist> arrayList = playlistDataSource.
                    getListNotAvailableinWebResponse(
                            Arrays.copyOf(schIdArrayList.toArray(),
                                    schIdArrayList.toArray().length,
                                    String[].class));

            if (arrayList.size() > 0) {

                songsDataSource.open();
                //TODO: check if playlist id not refer in other schid record if not exist then delete all songs else dont
                for (int k = 0; k < arrayList.size(); k++) {
                    if (playlistDataSource.checkifPlaylistExist(arrayList.get(k).getpSc_id())) {
                        //songsDataSource.deleteSongsWithPlaylist(arrayList.get(k).getpSc_id());
                    }
                    playlistDataSource.deletePlaylist(arrayList.get(k));
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            playlistDataSource.close();
            songsDataSource.close();
        }
    }

    private ArrayList<Playlist> getPlaylistFromLocally() {
        ArrayList<Playlist> arrayList = null;
        ArrayList<Playlist> remaingArrayist = null;
        try {
            playlistDataSource.open();
            arrayList = playlistDataSource.getAllPlaylists();
            remaingArrayist = playlistDataSource.getRemainingAllPlaylists();

            if (arrayList.size() > 0) {

//                 TODO Add the playlists whose time to play is gone.
            }
            arrayList.addAll(remaingArrayist);
            playlistDataSource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void songDownloaded(Songs songs, PlaylistLoaderListener songsLoaderListener){
        try {
            songsDataSource.open();
            songsDataSource.updateSongsListWithDownloadstatusandPath(songs);

//            ArrayList<Songs> songsArrayList = songsDataSource.getSongWithSimilarTitleIdsWhichAreNotDownloaded(songs);


        }catch (Exception e){
            e.printStackTrace();
            if (songsLoaderListener != null){
                songsLoaderListener.recordSaved(false);
            }
        }finally {
            songsDataSource.close();

            if (songsLoaderListener != null){
                songsLoaderListener.recordSaved(true);
            }
        }
    }

    public ArrayList<Songs> getSongsThatAreNotDownloaded(String playlistId){
        try {
            songsDataSource.open();
            return songsDataSource.getSongsThoseAreNotDownloaded(playlistId);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
        return null;
    }

    public ArrayList<Songs> getSongsForPlaylist(String playlistId){
        try {
            songsDataSource.open();
            return songsDataSource.getAllSongss(playlistId);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
        return null;
    }


    public void getPlaylistWiseSongDetails(){
        try {
            songsDataSource.open();
            songsDataSource.getPlaylistWiseDownloadedSongs(this.context);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
    }

    public ArrayList<Songs> getDownloadedSongsForPlaylist(String playlistId){
        try {
            songsDataSource.open();
            return songsDataSource.getSongsThoseAreDownloaded(playlistId);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
        return null;
    }

    public ArrayList<Songs> getNotDownloadedSongsForPlaylist(String playlistId){
        try {
            songsDataSource.open();
            return songsDataSource.getSongsThoseAreNotDownloaded(playlistId);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            songsDataSource.close();
        }
        return null;
    }

    /*This method provides the playlists which will play after current time.*/
    public ArrayList<Playlist> getPlaylistFromLocallyToBedDownload() {

        ArrayList<Playlist> arrayList = null;
        ArrayList<Playlist> remaingArrayist = null;

        try {
            playlistDataSource.open();

            arrayList = playlistDataSource.getPlaylistsForCurrentAndComingTime();


            remaingArrayist = playlistDataSource.getPlaylistGoneTime();

            ArrayList<Playlist> arrayListGoneTime = new ArrayList<>();

            if (arrayList.size() > 0) {
                if (arrayListGoneTime.size() > 0) {
                    remaingArrayist.addAll(arrayListGoneTime);
                }
            }

            arrayList.addAll(remaingArrayist);
            playlistDataSource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }



    public ArrayList<Playlist> getAllPlaylistInPlayingOrder() {

        ArrayList<Playlist> arrayList = null;

        try {
            playlistDataSource.open();

            arrayList = playlistDataSource.getAllPlaylistsInPlayingOrder();

            playlistDataSource.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void checkUpdatedPlaylistData(){

        Log.e(TAG,"Checking for new data");

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Tokenid",SharedPreferenceUtil.
                    getStringPreference(PlaylistManager.this.context,Constants.TOKEN_ID));

            new OkHttpUtil(context, Constants.CHECK_TOKEN_PUBLISH,jsonObject.toString(),
                    PlaylistManager.this,false,
                    Constants.CHECK_TOKEN_PUBLISH_TAG).
                    callRequest();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void publishTokenForUpdatedData(){

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Tokenid",SharedPreferenceUtil.
                    getStringPreference(PlaylistManager.this.context,Constants.TOKEN_ID));

            new OkHttpUtil(context, Constants.UPDATE_TOKEN_PUBLISH,jsonObject.toString(),
                    PlaylistManager.this,false,
                    Constants.UPDATE_TOKEN_PUBLISH_TAG).
                    callRequest();


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
