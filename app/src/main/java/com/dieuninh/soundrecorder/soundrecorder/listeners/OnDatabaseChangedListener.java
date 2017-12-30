package com.dieuninh.soundrecorder.soundrecorder.listeners;

/**
 * Created by DieuLinh on 3/31/2017.
 */


public interface OnDatabaseChangedListener {
    void onNewDatabaseEntryAdded();
    void onDatabaseEntryRenamed();
}
