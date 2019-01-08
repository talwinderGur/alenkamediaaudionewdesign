package com.alenkasmartaudioplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.alenkasmartaudioplayer.models.Songs;
import com.alenkasmartaudioplayer.utils.Constants;

import java.io.File;
import java.nio.DoubleBuffer;
import java.util.ArrayList;

/**
 * Created by ParasMobile on 6/21/2016.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "alenkaplayer.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_PLAYLIST = "playlist";
    public static final String TABLE_SONGS = "songs";
    public static final String TABLE_PRAYER = "prayer";
    public static final String TABLE_DeleteSongs = "songsdeletelist";
    public static final String TABLE_ADVERTISEMENT = "advertisement";
    public static final String TABLE_PLAYER_STATUS = "table_player_status";


    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCHID = "sch_id";
    public static final String COLUMN_SP_PLAYLIST_ID = "sp_playlist_id";
    public static final String COLUMN_TOKEN_ID = "token_id";

    /**
     * //TODO:Playlist column tables
     */
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_SP_NAME = "sp_name";
    public static final String COLUMN_START_TIME_IN_MILI="startTimeInMilli";
    public static final String COLUMN_END_TIME_IN_MILI="endTimeInMilli";
    public static final String COLUMN_IS_SEPARATION_ACTIVE = "isseprationactive";
    public static final String COLUMN_IS_FADING_ACTIVE = "isfadingactive";
    //public static final String COLUMN_PLAY_LIST_ID = "playlist_id";

    /**
     * //TODO:Songs column tables
     */
    public static final String COLUMN_TITLE_ID = "title_id";
    public static final String COLUMN_IS_DOWNLOADED = "is_downloaded";
    public static final String COLUMN_TITLE = "titles";
    public static final String COLUMN_ALBUM_ID = "album_id";
    public static final String COLUMN_ARTIST_ID = "artist_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_ARTIST_NAME = "artist_name";
    public static final String COLUMN_ALBUM_NAME = "album_name";
    public static final String COLUMN_SONG_PATH = "song_path";
    public static final String COLUMN_TITLE_URL = "song_url";
    public static final String COLUMN_SERIAL_NO = "serial_no";

   //TODO: Colums for songsdeletelist
    public static final String COLUMN_TITLE_IDdelete = "title_iddel";


    //TODO: Colums for Prayer Data

    public static final String COLUMN_START_TIME_FOR_PRAYER = "start_time_prayer";
    public static final String COLUMN_END_TIME_FOR_PRAYER = "end_time_prayer";

    public static final String COLUMN_START_DATE_FOR_PRAYER="start_date_prayer";
    public static final String COLUMN_END_DATE_FOR_PRAYER="end_date_prayer";

    public static final String COLUMN_START_TIME_IN_MILI_PRAYER="startTimeInMilli";
    public static final String COLUMN_END_TIME_IN_MILI_PRAYER="endTimeInMilli";

    //TODO:Columns for Advertisement Data

    public static final String COLUMN_ADV_FILE_URL = "adv_file_url";
    public static final String COLUMN_ADV_ID = "adv_id";
    public static final String COLUMN_ADV_NAME = "adv_name";
    public static final String COLUMN_ADV_IS_MIN = "adv_minute";
    public static final String COLUMN_ADV_IS_SONG = "adv_song";
    public static final String COLUMN_ADV_IS_TIME = "adv_time";
    public static final String COLUMN_ADV_PLY_TYPE = "adv_play_type";
    public static final String COLUMN_ADV_SERIAL_NO = "adv_serial_no";
    public static final String COLUMN_ADV_TOTAL_MIN = "adv_total_min";
    public static final String COLUMN_ADV_TOTAL_SONGS = "adv_total_song";
    public static final String COLUMN_ADV_E_DATE = "adv_end_date";
    public static final String COLUMN_ADV_S_DATE = "adv_start_date";
    public static final String COLUMN_ADV_S_TIME = "adv_start_time";
    public static final String COLUMN_ADV_PATH = "adv_path";
    public static final String COLUMN_SET_DOWNLOAD_STATUS = "download_status";
    public static final String COLUMN_START_TIME_IN_MILLIS_ADV = "start_time_in_millis_adv";




    //TODO: Database creation sql statement
    private static final String DATABASE_CREATE_PLAYLIST = "create table "
            + TABLE_PLAYLIST + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_SCHID
            + " text not null, " + COLUMN_SP_PLAYLIST_ID
            + " text not null, " + COLUMN_START_TIME
            + " text not null, " + COLUMN_END_TIME
            + " text not null, " + COLUMN_SP_NAME
            + " text not null, " + COLUMN_START_TIME_IN_MILI
            + " numeric not null, " + COLUMN_END_TIME_IN_MILI
            + " numeric not null, " + COLUMN_IS_SEPARATION_ACTIVE
            + " numeric not null, " + COLUMN_IS_FADING_ACTIVE
            + " numeric not null);";

    //TODO: Database creation sql statement
    private static final String DATABASE_CREATE_SONGS = "create table "
            + TABLE_SONGS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_SCHID
            + " text not null, " + COLUMN_TITLE_ID
            + " text not null, " + COLUMN_IS_DOWNLOADED
            + " text not null, " + COLUMN_TITLE
            + " text not null, " + COLUMN_ALBUM_ID
            + " text not null, " + COLUMN_ARTIST_ID
            + " text not null, " + COLUMN_TIME
            + " text not null, " + COLUMN_ARTIST_NAME
            + " text not null, " + COLUMN_ALBUM_NAME
            + " text not null, " + COLUMN_SP_PLAYLIST_ID
            + " text not null, " + COLUMN_SONG_PATH
            + " text not null, " + COLUMN_TITLE_URL
            + " numeric not null, " + COLUMN_SERIAL_NO
            + " text not null);";

    //TODO: Table Creation For Songsdeletelist

    private static final String DATABASE_Create_DeleteSongsList= "create table "
            + TABLE_DeleteSongs + "("  + COLUMN_TITLE_IDdelete
            + " text not null);";

    //TODO: Table Creation For Prayer

    private static final String DATABASE_CREATE_PRAYER = "create table "
            + TABLE_PRAYER + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_START_TIME_FOR_PRAYER
            + " text not null, " + COLUMN_END_TIME_FOR_PRAYER
            + " text not null, " + COLUMN_START_DATE_FOR_PRAYER
            + " text not null, " + COLUMN_END_DATE_FOR_PRAYER
            + " text not null, " + COLUMN_START_TIME_IN_MILI_PRAYER
            + " numeric not null, " + COLUMN_END_TIME_IN_MILI_PRAYER
            + " numeric not null);";

    //TODO:Table Creation For Advertisements

    private static final String DATABASE_CREATE_ADVERTISEMENT = "create table "
            + TABLE_ADVERTISEMENT + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_ADV_FILE_URL
            + " text not null, " + COLUMN_ADV_ID
            + " text not null, " + COLUMN_ADV_NAME
            + " text not null, " + COLUMN_ADV_IS_MIN
            + " text not null, " + COLUMN_ADV_IS_SONG
            + " text not null, " + COLUMN_ADV_IS_TIME
            + " text not null, " + COLUMN_ADV_PLY_TYPE
            + " text not null, " + COLUMN_ADV_SERIAL_NO
            + " text not null, " + COLUMN_ADV_TOTAL_MIN
            + " text not null, " + COLUMN_ADV_TOTAL_SONGS
            + " text not null, " + COLUMN_ADV_E_DATE
            + " text not null, " + COLUMN_ADV_S_DATE
            + " text not null, " + COLUMN_ADV_S_TIME
            + " text not null, " + COLUMN_ADV_PATH
            + " text , " + COLUMN_SET_DOWNLOAD_STATUS
            + " text not null, " + COLUMN_START_TIME_IN_MILLIS_ADV
            + " numeric not null);";


    //TODO: Columns for Player Status
    public static final String COLUMN_LOGIN_DATE = "login__date";
    public static final String COLUMN_LOGIN_TIME = "login_time";
    //TODO: Logout status
    public static final String COLUMN_LOGOUT_DATE = "logout_date";
    public static final String COLUMN_LOGOUT_TIME = "logout_time";
    //TODO: PlayedSongStatus
    public static final String COLUMN_ARTIST_ID_SONG = "column_artist_id_song";
    public static final String COLUMN_PLAYED_DATE_TIME_SONG = "played_date_time_song";
    public static final String COLUMN_TITLE_ID_SONG = "title_id_song";
    public static final String COLUMN_SPL_PLAYLIST_ID_SONG = "spl_playlist_id_song";
    //TODO: HeartBeat status column
    public static final String COLUMN_HEARTBEAT_DATETIME = "heatbeat_datetime";
    //TODO: Played Adv..Status
    public static final String COLUMN_ADVERTISEMENT_ID_STATUS = "advertisement_id_status";
    public static final String COLUMN_ADVERTISEMENT_PLAYED_DATE = "advertisement_played_date";
    public static final String COLUMN_ADVERTISEMENT_PLAYED_TIME = "advertisement_played_time";
    //TODO: Prayer status
    public static final String COLUMN_PRAYER_PLAYED_DATE = "prayer_played_date";
    public static final String COLUMN_PRAYER_PLAYED_TIME = "prayer_played_time";

    public static final String COLUMN_IS_PLAYER_STATUS_TYPE = "is_player_status_type";

    //TODO Database create for Player Status

    private static final String DATABASE_CREATE_PLAYER_STATUS = "create table "
            + TABLE_PLAYER_STATUS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_LOGIN_DATE
            + " text, " + COLUMN_LOGIN_TIME
            + " text, " + COLUMN_LOGOUT_DATE
            + " text, " + COLUMN_LOGOUT_TIME
            + " text, " + COLUMN_ARTIST_ID_SONG
            + " text, " + COLUMN_PLAYED_DATE_TIME_SONG
            + " text, " + COLUMN_TITLE_ID_SONG
            + " text, " + COLUMN_SPL_PLAYLIST_ID_SONG
            + " text, " + COLUMN_HEARTBEAT_DATETIME
            + " text, " + COLUMN_ADVERTISEMENT_ID_STATUS
            + " text, " + COLUMN_ADVERTISEMENT_PLAYED_DATE
            + " text, " + COLUMN_ADVERTISEMENT_PLAYED_TIME
            + " text, " + COLUMN_PRAYER_PLAYED_DATE
            + " text, " + COLUMN_PRAYER_PLAYED_TIME
            + " text , " + COLUMN_IS_PLAYER_STATUS_TYPE
            + " text);";

    public static MySQLiteHelper instance = null;

    public static MySQLiteHelper getInstance(Context context){

        if (instance == null){
            instance = new MySQLiteHelper(context);
        }

        return instance;
    }

    public MySQLiteHelper(Context context) {

        super(context, context.getApplicationInfo().dataDir
                + File.separator + Constants.ROOT_FOLDER
                + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_PLAYLIST);
        database.execSQL(DATABASE_CREATE_SONGS);
        database.execSQL(DATABASE_CREATE_PRAYER);
        database.execSQL(DATABASE_Create_DeleteSongsList);
        database.execSQL(DATABASE_CREATE_ADVERTISEMENT);
        database.execSQL(DATABASE_CREATE_PLAYER_STATUS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
//        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_PRAYER);
//        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_ADVERTISEMENT);
//        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_PLAYER_STATUS);
//        onCreate(db);

        if (oldVersion == 1 && newVersion == 2)
        db.execSQL(ADD_FADING_COLUMN_TO_PLAYLIST_TABLE);


    }

    private String ADD_FADING_COLUMN_TO_PLAYLIST_TABLE = "ALTER TABLE " +TABLE_PLAYLIST+
            " ADD "+ COLUMN_IS_FADING_ACTIVE +" NUMERIC NOT NULL DEFAULT(0)";


    public boolean InsertData(String titleid)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MySQLiteHelper.COLUMN_TITLE_IDdelete, titleid);
        long result= db.insert(TABLE_DeleteSongs,null,contentValues);
        if(result==-1) {

            return false;
        }
        else
        {
            return true;
        }
    }
    public ArrayList<Songs> searchDictionaryWords(String searchWord){
        SQLiteDatabase db=this.getWritableDatabase();
        ArrayList<Songs> mItems = new ArrayList<Songs>();
        String query ="Select * from " +MySQLiteHelper.TABLE_SONGS + " where " +MySQLiteHelper.COLUMN_TITLE +" like " + "'%" + searchWord + "%'" +
                " OR "+MySQLiteHelper.COLUMN_ALBUM_NAME +" like " + "'%" + searchWord + "%'"+
                " OR "+MySQLiteHelper.COLUMN_ARTIST_NAME +" like " + "'%" + searchWord + "%'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            //Ranjeet static fn cursorToSongs
            Songs songs = SongsDataSource.cursorToSongs(cursor);
            mItems.add(songs);
            cursor.moveToNext();
        }
        cursor.close();
        return mItems;
    }



}
/* back up link
http://stackoverflow.com/questions/5282936/android-backup-restore-how-to-backup-an-internal-database*/
