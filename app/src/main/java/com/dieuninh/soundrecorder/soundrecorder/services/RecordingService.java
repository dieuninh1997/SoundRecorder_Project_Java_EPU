package com.dieuninh.soundrecorder.soundrecorder.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
//import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.dieuninh.soundrecorder.soundrecorder.R;
import com.dieuninh.soundrecorder.soundrecorder.activities.MainActivity;
import com.dieuninh.soundrecorder.soundrecorder.data.DBHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DieuLinh on 4/1/2017.
 */

public class RecordingService extends Service {
    private static final String LOG_TAG =RecordingService.class.getSimpleName();

    private String mFileName = null;
    private String mFilePath = null;

    private MediaRecorder mRecorder = null;
    private DBHelper mDatabase;

    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;
    private RecordingService.OnTimerChangedListener onTimerChangedListener = null;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }
    public void setFileNameAndPath() {
        int count = 0;
        File f;
        do {
            count++;
            mFileName = getString(R.string.default_file_name)
                    + " #" + (mDatabase.getCount() + count) + ".mp3";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath =mFilePath+ "/SoundRecorder/" + mFileName;

            f = new File(mFilePath);
        } while (f.exists() && !f.isDirectory());
    }
    public void stopRecording() {
        mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();


        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        mRecorder = null;

        try {
            mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis);
            Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(LOG_TAG, "exception: can not insert file ", e);
        }
    }

    public void startRecording() {

        setFileNameAndPath();
//cai dat quality:
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioEncodingBitRate(128000);//ok
        mRecorder.setAudioSamplingRate(44100);
        /*
        * loai format MPEG_4 và encoder ACC  sẽ cho file size 85kb/15sec-> very poor quality
        *
        * nếu cài đặt format AMR_NB và encodr AMR_NB sẽ cho file size 25kb/15 sec -> better quality  nhưng ko chay trên iphone đc
         *
         *
         * tăng bitrate value if want change quality
         * bit rate :bits per second, not kb per second
         *
        * */
        mRecorder.setAudioChannels(1);

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

//            startTimer();
//            startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onTimerChangedListener != null)
                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
               // mgr.notify(1, createNotification());
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }
    //TODO:
//    private Notification createNotification() {
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(getApplicationContext())
//                        .setSmallIcon(R.drawable.ic_mic_white_24px)
//                        .setContentTitle(getString(R.string.notification_recording))
//                        .setContentText(mTimerFormat.format(mElapsedSeconds * 1000))
//                        .setOngoing(true);
//
//        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
//                new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, 0));
//
//        return mBuilder.build();
//    }
}
