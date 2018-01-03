package com.dieuninh.soundrecorder.soundrecorder.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dieuninh.soundrecorder.soundrecorder.R;
import com.dieuninh.soundrecorder.soundrecorder.adapters.SavedRecordAdapter;


public class SavedRecordFragment extends Fragment {
    private static final String POSITION = "position";
    private static final String LOG_TAG = "SavedRecordFragment";
    private int position;
    private SavedRecordAdapter mSavedRecordAdapter;
    FileObserver observer = new FileObserver(Environment.getExternalStorageDirectory().toString()+ "/SoundRecorder") {
        @Override
        public void onEvent(int event, String file) {
            if (event == FileObserver.DELETE) {
                //user delete a recording file out of app
                String filePath = Environment.getExternalStorageDirectory().toString() + "/SoundRecorder/" + file;
                Log.d(LOG_TAG, "file delete [" + Environment.getExternalStorageDirectory().toString() + "/SoundRecorder/" + file);
                //remove file from database and recyclerview
                mSavedRecordAdapter.removeOutOfApp(filePath);
            }
            //set up a file observer to watch this directory on sd card
        }
    };

    public static SavedRecordFragment newInstance(int position) {
        SavedRecordFragment f = new SavedRecordFragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.position = getArguments().getInt(POSITION);
        observer.startWatching();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_saved_record, container, false);
        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        //newest to oldest order (database stores from oldest to newest)
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mSavedRecordAdapter = new SavedRecordAdapter(getActivity(), llm);
        Log.e("SaveRecordFragment","So item="+mSavedRecordAdapter.getItemCount());
        mRecyclerView.setAdapter(mSavedRecordAdapter);
        return v;
    }
}