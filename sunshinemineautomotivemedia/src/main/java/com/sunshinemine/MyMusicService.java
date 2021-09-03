package com.sunshinemine;

import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;

import androidx.media.MediaBrowserServiceCompat;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyMusicService extends MediaBrowserServiceCompat {

    private MediaSessionCompat mSession;
    private List<MediaMetadataCompat> mMusic;
    private MediaPlayer mMediaPlayer;
    private MediaMetadataCompat mMediaMetadataCompat;
    private MediaMetadataCompat mCurrentTrack;

    @Override
    public void onCreate() {
        super.onCreate();

        mMusic = new ArrayList<>();
        mMusic.add(new MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,"https://www.youtube.com/watch?v=mS60nG6bJwo")
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE,"Music 1")
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,"Artist 1")
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,30000)
        .build());

        mMusic.add(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,"https://www.youtube.com/watch?v=KpMUF3FgdyQ")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,"Music 2")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,"Artist 2")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,30000)
                .build());

        mMediaPlayer = new MediaPlayer();



        mSession = new MediaSessionCompat(this, "MyMusicService");
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);
        setSessionToken(mSession.getSessionToken());

    }

    @Override
    public void onDestroy() {
        mSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid,
                                 Bundle rootHints) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> list = new ArrayList<>();
        for(MediaMetadataCompat m : mMusic){
            list.add(
                    new MediaBrowserCompat.MediaItem(m.getDescription(),MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
            );
        }
        result.sendResult(list);
    }

    private PlaybackStateCompat buildState(int state){
        return new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .setState(state,mMediaPlayer.getCurrentPosition(),1, SystemClock.elapsedRealtime())
                .build();
    }

    private void handlePlay(){

        mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PLAYING));
        mSession.setMetadata(mCurrentTrack);
        try{
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(MyMusicService.this, Uri.parse(mCurrentTrack.getDescription().getMediaId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
               mediaPlayer.start();
            }
        });
        mMediaPlayer.prepareAsync();
    }
    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            if(mCurrentTrack == null){
                mCurrentTrack = mMusic.get(0);
                handlePlay();
            }
            else{
                mMediaPlayer.start();
                mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PLAYING));
            }
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
        }

        @Override
        public void onSeekTo(long position) {
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            for (MediaMetadataCompat item:mMusic){
                if(item.getDescription().getMediaId().equals(mediaId)){
                    mCurrentTrack = item;
                    break;
                }
            }
        }

        @Override
        public void onPause() {
            mMediaPlayer.pause();
            mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PAUSED));
        }

        @Override
        public void onStop() {
        }

        @Override
        public void onSkipToNext() {
        }

        @Override
        public void onSkipToPrevious() {
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
        }

        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
        }
    }
}