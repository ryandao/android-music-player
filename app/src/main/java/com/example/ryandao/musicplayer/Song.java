package com.example.ryandao.musicplayer;

import android.net.Uri;

/**
 * Created by ryandao on 12/6/14.
 */
public class Song {
  private long id;
  private String title;
  private String artist;
  private Uri fileUri;

  public Song(long songId, String songTitle, String songArtist, Uri songFileUri) {
    id = songId;
    title = songTitle;
    artist = songArtist;
    fileUri = songFileUri;
  }

  public long getID(){return id;}
  public String getTitle(){return title;}
  public String getArtist(){return artist;}
  public Uri getFileUri() { return fileUri; }
}

