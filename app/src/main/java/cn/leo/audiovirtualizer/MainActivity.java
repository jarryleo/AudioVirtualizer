package cn.leo.audiovirtualizer;

import android.Manifest;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;

import cn.leo.permission.PermissionRequest;

public class MainActivity extends AppCompatActivity implements Visualizer.OnDataCaptureListener {

    private MediaPlayer mMediaPlayer;
    private TextView mTvTest;
    private AudioVirtualizer mAudioVirtualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvTest = findViewById(R.id.tvTest);
        mAudioVirtualizer = findViewById(R.id.av);
        mTvTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic();
            }
        });
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //visualizer();
                mAudioVirtualizer.bindMediaPlayer(mp);
                mp.start();
            }
        });
    }

    @PermissionRequest({Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE})
    private void playMusic() {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        if (cursor == null) {
            return;
        }
        if (!cursor.moveToNext()) {
            return;
        }
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        cursor.close();
        Uri parse = Uri.parse("file://" + path);
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(this, parse);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void visualizer() {
        int audioSessionId = mMediaPlayer.getAudioSessionId();
        final Visualizer visualizer = new Visualizer(audioSessionId);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
        visualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2,
                false, true);
        visualizer.setEnabled(true);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                visualizer.setEnabled(false);
            }
        });
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        byte[] model = new byte[fft.length / 2 + 1];
        model[0] = (byte) Math.abs(fft[1]);
        int j = 1;

        for (int i = 2; i < 18; ) {
            model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
            i += 2;
            j++;
        }
        mTvTest.setText(Arrays.toString(model));
    }
}
