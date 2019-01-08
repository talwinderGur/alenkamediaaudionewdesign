package com.alenkasmartaudioplayer.interfaces;

/**
 * Created by love on 30/5/17.
 */
public interface PlaylistLoaderListener {

    void startedGettingPlaylist();

    void finishedGettingPlaylist();

    void errorInGettingPlaylist(Exception exception);

    void recordSaved(boolean isSaved);

    void tokenUpdatedOnServer();

}

