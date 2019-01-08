package com.alenkasmartaudioplayer.interfaces;

import com.alenkasmartaudioplayer.models.Advertisements;
import com.alenkasmartaudioplayer.models.Songs;

/**
 * Created by love on 31/5/17.
 */
public interface DownloadListener {

    void onUpdate(long value);
    void downloadCompleted(boolean shouldPlay, Songs songs);
    void advertisementDownloaded(Advertisements advertisements);
    void startedCopyingSongs(int currentSong, int totalSongs, boolean isFinished);
    void showOfflineDownloadingAlert();
    void finishedDownloadingSongs(int totalSongs);
}
