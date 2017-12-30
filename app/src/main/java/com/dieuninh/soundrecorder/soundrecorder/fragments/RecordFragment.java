package com.dieuninh.soundrecorder.soundrecorder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.dieuninh.soundrecorder.soundrecorder.R;
import com.dieuninh.soundrecorder.soundrecorder.services.RecordingService;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;

import developer.shivam.library.WaveView;

/**
 * Created by DieuLinh on 3/30/2017.
 */

public class RecordFragment extends Fragment{

    private static final String POSITION="position";
    private static final String LOG_TAG=RecordFragment.class.getSimpleName();

    private int position;
    //record controls
    private FloatingActionButton mRecordButton=null;
    private Button mPauseButton=null;
    private TextView mRecordingPrompt;
    private Chronometer mChronometer=null;
    private WaveView mWaveView;

    private int mRecordPromptCount=0;
    private boolean mStartRecording=true;
    private boolean mPauseRecording=true;
    private long timeWhenPaused=0;


    public static RecordFragment newInstance(int position)
    {
        RecordFragment f=new RecordFragment();
        Bundle b=new Bundle();
        b.putInt(POSITION,position);
        f.setArguments(b);
        return f;
    }
    public RecordFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(POSITION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View recordView=inflater.inflate(R.layout.fragment_record,container,false);

        mChronometer= (Chronometer) recordView.findViewById(R.id.chronometer);
        mWaveView= (WaveView) recordView.findViewById(R.id.sample_wave_view);

        //update recording prompt text
        mRecordingPrompt= (TextView) recordView.findViewById(R.id.recording_status_text);
        mRecordButton= (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(getResources().getColor(R.color.colorAccent));
        mRecordButton.setColorPressed(getResources().getColor(R.color.colorPrimary));
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
//                showNotification();

                mStartRecording=!mStartRecording;
            }
        });
        mPauseButton= (Button) recordView.findViewById(R.id.btnPause);
        mPauseButton.setVisibility(View.GONE);//ẩn đi
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPauseRecord(mPauseRecording);
                mPauseRecording=!mPauseRecording;
            }
        });
        return recordView;
    }



    private void onRecord(boolean start) {
        Intent intent=new Intent(getActivity(),RecordingService.class);
        if(start)
        {
            mWaveView.setSpeed(1);
            mRecordButton.setImageResource(R.drawable.ic_stop_white_24px);
            Toast.makeText(getActivity(),getResources().getString(R.string.toast_recording_start),Toast.LENGTH_SHORT).show();

            File folder=new File(Environment.getExternalStorageDirectory()+
                    "/SoundRecorder");
            if(!folder.exists())
            {
                //nếu folder /SoundRecorder_dn chưa có, thì tạo mới
                folder.mkdir();
            }


            //start chronometer
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if(mRecordPromptCount==0)
                    {
                        mRecordingPrompt.setText(getResources().getString(R.string.record_in_progress)+
                                "."  );

                    }else if(mRecordPromptCount==1)
                    {
                        mRecordingPrompt.setText(getResources().getString(R.string.record_in_progress)+"..");
                    }
                    else if (mRecordPromptCount==2)
                    {
                        mRecordingPrompt.setText(getResources().getString(R.string.record_in_progress)+"...");
                        mRecordPromptCount=-1;
                    }
                    mRecordPromptCount++;
                }
            });

            //start recordingservice
            getActivity().startService(intent);
            //keep screen on while recording
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            mRecordingPrompt.setText(getResources().getString(R.string.record_in_progress)+".");
            mRecordPromptCount++;

        }else
        {
            //stop recording
            mWaveView.setSpeed(0);
            mRecordButton.setImageResource(R.drawable.ic_mic_white_24px);
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused=0;
            mRecordingPrompt.setText(getResources().getString(R.string.record_prompt));
            getActivity().stopService(intent);
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    //TODO: implement pause recording
    private void onPauseRecord(boolean pause) {
        if (pause) {
            //pause recording
            mWaveView.setSpeed(0);
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_play_arrow_white_24px ,0 ,0 ,0);
            mRecordingPrompt.setText((String)getString(R.string.resume_recording_button).toUpperCase());
            timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
        } else {
            //resume recording
            mWaveView.setSpeed(1);
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_pause_white_24px ,0 ,0 ,0);
            mRecordingPrompt.setText((String)getString(R.string.pause_recording_button).toUpperCase());
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            mChronometer.start();
        }
    }


}
