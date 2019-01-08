package com.alenkasmartaudioplayer.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.TabHost;

import com.alenkasmartaudioplayer.activities.HomeActivity;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.google.android.exoplayer2.SimpleExoPlayer;

/**
 * Created by love on 28/2/18.
 */

public class MediaPlayerUtils {

    private static final String TAG = MediaPlayerUtils.class.getSimpleName();

  public final static long CROSSFADE_DURATION = 5000;

    public static void fadeIn(final VideoView videoView, final SimpleExoPlayer exoPlayer,final Context context) {

        final float deviceVolume = 1.0f;

        final Handler h = new Handler();

            h.postDelayed(new Runnable() {

                private float time = 0.0f;
                private float volume = 0.0f;

                @Override
                public void run() {

                    boolean isPlayerMuted = false;

                    if (context instanceof HomeActivity){

                        HomeActivity homeActivity = (HomeActivity)context;
                        isPlayerMuted = homeActivity.btnVolume.isSelected();
                    }

                    if (isPlayerMuted){

                        if (videoView != null)
                            videoView.setVolume(0);

                        if (exoPlayer != null)
                            exoPlayer.setVolume(0);


                    }else {

                        time += 50;
                        volume = (deviceVolume * time) / CROSSFADE_DURATION;

                        //Log.e(TAG,"Setting volume for fade in ->" + volume);
                        //Log.e(TAG,"Time value for fade in ->" + time);

                        if (videoView != null)
                            videoView.setVolume(volume);

                        if (exoPlayer != null)
                            exoPlayer.setVolume(volume);

                    }

                    if (time < CROSSFADE_DURATION)
                        h.postDelayed(this, 100);
                }
            }, 100);
    }

    public static void fadeOutForVideoPlayer(final VideoView videoView, final SimpleExoPlayer exoPlayer,final Context context) {
        {

            final float deviceVolume = 1.0f;

            final Handler h = new Handler();

            h.postDelayed(new Runnable() {
                private float time = CROSSFADE_DURATION;
                private float volume = 0.0f;

                @Override
                public void run() {

                    boolean isPlayerMuted = false;

                    if (context instanceof HomeActivity){

                        HomeActivity homeActivity = (HomeActivity)context;
                        isPlayerMuted = homeActivity.btnVolume.isSelected();
                    }

                    if (isPlayerMuted){

                        if (videoView != null)
                            videoView.setVolume(0);

                        if (exoPlayer != null)
                            exoPlayer.setVolume(0);
                    } else {

                        time -= 150;
                        volume = (deviceVolume * time) / CROSSFADE_DURATION;

                        //Log.e(TAG,"Setting volume for fade out ->" + volume);

                        if (videoView != null)
                            videoView.setVolume(volume);

                        if (exoPlayer != null)
                            exoPlayer.setVolume(volume);
                    }

                    if (time > 0)
                        h.postDelayed(this, 100);  // 1 second delay (takes millis)
                }
            }, 100);

        }
    }

    public static void fadeOut(final VideoView videoView, final SimpleExoPlayer exoPlayer,final Context context) {
        {

            final float deviceVolume = getDeviceVolume(context);

            final Handler h = new Handler();

                h.postDelayed(new Runnable() {
                    private float time = CROSSFADE_DURATION;
                    private float volume = 0.0f;

                    @Override
                    public void run() {

                        boolean isPlayerMuted = false;

                        if (context instanceof HomeActivity){

                            HomeActivity homeActivity = (HomeActivity)context;
                            isPlayerMuted = homeActivity.btnVolume.isSelected();
                        }

                        if (isPlayerMuted){

                            if (videoView != null)
                                videoView.setVolume(0);

                            if (exoPlayer != null)
                                exoPlayer.setVolume(0);
                        } else {

                            time -= 100;
                            volume = (deviceVolume * time) / CROSSFADE_DURATION;

                            Log.e(TAG,"Setting volume for fade out ->" + volume);

                            if (videoView != null)
                                videoView.setVolume(volume);

                            if (exoPlayer != null)
                                exoPlayer.setVolume(volume);
                        }

                        if (time > 0)
                            h.postDelayed(this, 100);  // 1 second delay (takes millis)
                    }
                }, 100);

            }
    }

    public static float getDeviceVolume(Context context) {


        if (true){

            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            int volumeLevel = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

            return (float)volumeLevel;
        }

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int volumeLevel = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return (float) volumeLevel / maxVolume;
    }
}
