package com.example.ryandao.musicplayer;

/**
 * Created by ryandao on 12/6/14.
 */
public class Song {
  private long id;
  private String title;
  private String artist;

  public Song(long songId, String songTitle, String songArtist) {
    id = songId;
    title = songTitle;
    artist = songArtist;
  }

  public long getID(){return id;}
  public String getTitle(){return title;}
  public String getArtist(){return artist;}
}

