package com.example.ryandao.musicplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import com.example.ryandao.musicplayer.MusicService.MusicBinder;

public class MainActivity extends Activity {
  private ArrayList<Song> songList;
  private ListView songView;
  private MusicService musicSrv;
  private Intent playIntent;
  private boolean musicBound=false;

  @Override
  protected void onStart() {
    super.onStart();
    if (playIntent==null) {
      playIntent = new Intent(this, MusicService.class);
      bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
      startService(playIntent);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    songView = (ListView)findViewById(R.id.song_list);
    songList = new ArrayList<Song>();
    getSongList();

    Collections.sort(songList, new Comparator<Song>(){
      public int compare(Song a, Song b){
        return a.getTitle().compareTo(b.getTitle());
      }
    });

    SongAdapter songAdt = new SongAdapter(this, songList);
    songView.setAdapter(songAdt);
  }

  //connect to the service
  private ServiceConnection musicConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      MusicBinder binder = (MusicBinder)service;
      //get service
      musicSrv = binder.getService();
      //pass list
      musicSrv.setList(songList);
      musicBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      musicBound = false;
    }
  };

  public void getSongList() {
    ContentResolver musicResolver = getContentResolver();
    Uri musicUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
    Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

    if (musicCursor!=null && musicCursor.moveToFirst()) {
      int titleColumn = musicCursor.getColumnIndex
              (android.provider.MediaStore.Audio.Media.TITLE);
      int idColumn = musicCursor.getColumnIndex
              (android.provider.MediaStore.Audio.Media._ID);
      int artistColumn = musicCursor.getColumnIndex
              (android.provider.MediaStore.Audio.Media.ARTIST);
      //add songs to list
      do {
        long thisId = musicCursor.getLong(idColumn);
        String thisTitle = musicCursor.getString(titleColumn);
        String thisArtist = musicCursor.getString(artistColumn);
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                thisId);
        songList.add(new Song(thisId, thisTitle, thisArtist, trackUri));
      }
      while (musicCursor.moveToNext());
    }
  }

  public void songPicked(View view) {
    int songId = Integer.parseInt(view.getTag().toString());
    new ServerSendTask(songId).execute("");
  }

  public void playSong(int songId) {
    musicSrv.setSong(songId);
    musicSrv.playSong();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_shuffle:
        //shuffle
        break;
      case R.id.action_end:
        stopService(playIntent);
        musicSrv=null;
        System.exit(0);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    stopService(playIntent);
    musicSrv = null;
    super.onDestroy();
  }

  private class ServerSendTask extends AsyncTask<String, Void, Void> {
    private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
    private int songId;
    private Socket socket;

    public ServerSendTask(int songId) {
      this.songId = songId;
    }

    @Override
    protected void onPreExecute() {
      this.dialog.setMessage("Processing...");
      this.dialog.show();
    }

    @Override
    protected void onPostExecute(Void result) {
      if (this.dialog.isShowing()) {
        this.dialog.dismiss();
      }
      playSong(songId);
    }

    @Override
    protected Void doInBackground(String... params) {
      // TODO: Generate data from the song file and send to Spark core.
      File file = new File(songList.get(songId).getFileUri().getPath());
      try {
        byte[] data = new AudioProcessor(file).process();
          System.out.println("data.length = " + data.length);
        sendBytes(data);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    public void sendBytes(byte[] myByteArray) throws IOException {
      String host = "192.168.43.141";
      int port = 23;
      socket = new Socket(host, port);
      sendBytes(myByteArray, 0, myByteArray.length);
    }

    public void sendBytes(byte[] myByteArray, int start, int len) throws IOException {
      if (len < 0)
        throw new IllegalArgumentException("Negative length not allowed");
      if (start < 0 || start >= myByteArray.length)
        throw new IndexOutOfBoundsException("Out of bounds: " + start);
      // Other checks if needed.

      // May be better to save the streams in the support class;
      // just like the socket variable.
      OutputStream out = socket.getOutputStream();
      DataOutputStream dos = new DataOutputStream(out);

      dos.writeInt(len);
      if (len > 0) {
        dos.write(myByteArray, start, len);
      }
    }
  }
}
