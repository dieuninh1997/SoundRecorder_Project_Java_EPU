package com.dieuninh.soundrecorder.soundrecorder.adapters;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import com.dieuninh.soundrecorder.soundrecorder.R;
import com.dieuninh.soundrecorder.soundrecorder.activities.TrimRecordingActivity;
import com.dieuninh.soundrecorder.soundrecorder.data.DBHelper;
import com.dieuninh.soundrecorder.soundrecorder.data.RecordItem;
import com.dieuninh.soundrecorder.soundrecorder.fragments.PlaybackFragment;
import com.dieuninh.soundrecorder.soundrecorder.listeners.OnDatabaseChangedListener;
import com.dieuninh.soundrecorder.soundrecorder.utility.Constant;


public class SavedRecordAdapter extends RecyclerView.Adapter<SavedRecordAdapter.RecordingsViewHolder>
        implements OnDatabaseChangedListener {

    private static final String LOG_TAG = "SavedRecordAdapter";
    private DBHelper mDatabase;
    RecordItem item;
    Context mContext;
    LinearLayoutManager llm;

    public SavedRecordAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();
        mContext = context;
        mDatabase = new DBHelper(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        Log.e("Database","Count="+mDatabase.getCount());
        llm = linearLayoutManager;
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {
        item = getItem(position);
        if (item != null) {
            long itemDuration = item.getLength();
            final long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(itemDuration);
            long seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                    - java.util.concurrent.TimeUnit.MINUTES.toSeconds(minutes);

            holder.vName.setText(item.getName());
            holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
            holder.vDateAdded.setText(
                    DateUtils.formatDateTime(
                            mContext,
                            item.getTime(),
                            DateUtils.FORMAT_SHOW_DATE |
                                    DateUtils.FORMAT_NUMERIC_DATE |
                                    DateUtils.FORMAT_SHOW_TIME |
                                    DateUtils.FORMAT_SHOW_YEAR
                    )
            );
            //define an on click listener to open playbackfragment
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        PlaybackFragment playbackFragment =
                                new PlaybackFragment()
                                        .newInstance(getItem(holder.getPosition()));

                        android.app.FragmentTransaction transaction = ((FragmentActivity) mContext).getFragmentManager().beginTransaction();
                        playbackFragment.show(transaction, "Dialog_Playback");
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "exception", e);
                    }
                }
            });

            //
            holder.btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareFileDialog(holder.getPosition());
                }
            });
            //
            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    renameFileDialog(holder.getPosition());
                }
            });
            //
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFileDialog(holder.getPosition());
                }
            });

        holder.btnTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFilePathAudioName(holder.getPosition());
            }
        });
        }
        else
        {
            Toast.makeText(mContext,"khong getItem dc",Toast.LENGTH_SHORT).show();
        }
    }

    public void sendFilePathAudioName(int position)
    {
        RecordItem data =getItem(position);
        Intent intent = new Intent(mContext, TrimRecordingActivity.class);
        intent.putExtra(Constant.FILE_PATH, data.getFilePath());
          intent.putExtra(Constant.FILE_NAME, data.getName());
        mContext.startActivity(intent);
    }
    private void deleteFileDialog(final int position) {
        AlertDialog.Builder deleteFileBuilder = new AlertDialog.Builder(mContext);
        deleteFileBuilder.setTitle(mContext.getString(R.string.dialog_title_delete));
        deleteFileBuilder.setMessage(mContext.getString(R.string.dialog_text_delete));
        deleteFileBuilder.setCancelable(true);
        deleteFileBuilder.setPositiveButton(mContext.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //remove item from database, recyclerview and storage
                    remove(position);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "exception ", e);
                }
                dialog.cancel();
            }
        });
        deleteFileBuilder.setNegativeButton(mContext.getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = deleteFileBuilder.create();
        alert.show();

    }

    private void renameFileDialog(final int position) {
        //rename file
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_file, null);
        final EditText input = (EditText) view.findViewById(R.id.new_name);
        renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(mContext.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString().trim() + ".mp4";
                rename(position, value);
                dialog.cancel();
            }
        });
        renameFileBuilder.setNegativeButton(mContext.getString(R.string.btn_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();

    }

    private void rename(int position, String name) {
        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + name;
        File f = new File(mFilePath);
        if (f.exists() && !f.isDirectory()) {
            //file name is not unique, cannot rename file
            Toast.makeText(mContext,
                    String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();
        } else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getFilePath());
            oldFilePath.renameTo(f);
            mDatabase.renameItem(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }
    }

    private void shareFileDialog(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
        shareIntent.setType("audio/mp4");
        mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));

    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_view, parent, false);
        mContext = parent.getContext();
        return new RecordingsViewHolder(itemView);
    }

    public void removeOutOfApp(String filePath) {
    }

    public static class RecordingsViewHolder
            extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDateAdded;
        protected ImageButton btnShare, btnTrim, btnDelete, btnEdit;
        protected View cardView;

        public RecordingsViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.file_name_text);
            vLength = (TextView) v.findViewById(R.id.file_length_text);
            vDateAdded = (TextView) v.findViewById(R.id.file_date_added_text);
            btnShare = (ImageButton) v.findViewById(R.id.btnShare);
            btnTrim = (ImageButton) v.findViewById(R.id.btnTrim);
            btnDelete = (ImageButton) v.findViewById(R.id.btnDelete);
            btnEdit = (ImageButton) v.findViewById(R.id.btnEdit);
            cardView = v.findViewById(R.id.card_view);
        }
    }

    public RecordItem getItem(int position) {
        return mDatabase.getItemAt(position);
    }

    @Override
    public int getItemCount() {

        return mDatabase.getCount();
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        //item added to top of the list
        notifyItemInserted(getItemCount() - 1);
        llm.scrollToPosition(getItemCount() - 1);
    }

    @Override
    public void onDatabaseEntryRenamed() {

    }


    public void remove(int position) {
        //xóa item khỏi database, recylerview and storage
        //xóa file khỏi storage
        File file = new File(getItem(position).getFilePath());
        file.delete();

        Toast.makeText(mContext,
                String.format(mContext.getString(R.string.toast_file_delete), getItem(position).getName()),
                Toast.LENGTH_SHORT).show();
        mDatabase.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);
    }


}

