package com.example.ryandao.musicplayer;

import android.media.AudioFormat;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.TrackInfo;

import com.ritolaaudio.simplewavio.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by ryandao on 12/6/14.
 */
public class AudioProcessor {
  private File file;

  public AudioProcessor(File file) {
    this.file = file;
  }

  public byte[] process() throws Exception {
    ArrayList<Double> result = new ArrayList<Double>();
    MediaFormat mediaFormat = new MediaFormat();

    TrackInfo[] trackInfos = new MediaPlayer().getTrackInfo();
    for (TrackInfo trackInfo : trackInfos) {
      if (trackInfo.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
        mediaFormat = trackInfo.getFormat();
      }
    }

    int sampleRate = mediaFormat.getInteger("KEY_SAMPLE_RATE");

    double [][] inputAudio = Utils.WAVToFloats(this.file);
    System.out.println("inputAudio.length = " + inputAudio.length);

    int BLOCK = (int) Math.round(sampleRate * 0.1);

    for(int start = 0; start < inputAudio.length; start += BLOCK) {
      double sum = 0;
      for(int i = 0; i < BLOCK && start + i < inputAudio.length; ++i) {
        sum += Math.abs(inputAudio[start + i][0]);
      }
      result.add(Math.exp(sum));
    }
    double minValue = Collections.min(result);
    double maxValue = Collections.max(result);

    byte[] data = new byte[result.size() + 1];

    for(int i = 0; i < result.size(); ++i) {
      int cur = (int) Math.round((result.get(i) - minValue) * 5.0 / (maxValue - minValue));
      data[i] = (byte) ((1<<(cur + 1)) - 1);
    }

    data[data.length-1] = 0x40;
    System.out.println(Arrays.toString(data));
    return data;
  }
}
