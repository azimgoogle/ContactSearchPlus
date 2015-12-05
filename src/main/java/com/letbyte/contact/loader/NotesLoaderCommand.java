package com.letbyte.contact.loader;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.Data;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.letbyte.contact.control.Constant;
import com.letbyte.contact.data.model.Contact;

import java.util.List;

//Use separate thread for search and Query to the native-phonebook
public class NotesLoaderCommand implements Command {

    private ContentProviderClient notesClient;
    private ProgressBar mProgressBar;
    private RecyclerView.Adapter mAdapter;
    private List<Contact> mContactList;

    public NotesLoaderCommand(Context context, ProgressBar progressBar, RecyclerView.Adapter adapter, List<Contact> ContactList) {
        mProgressBar = progressBar;
        mAdapter = adapter;
        this.mContactList = ContactList;
        notesClient = context.getContentResolver().acquireContentProviderClient(Data.CONTENT_URI);
    }


    @Override
    public void execute() {
        new NotesLoaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class NotesLoaderTask extends AsyncTask<Void, Void, List<Contact>> {

        @Override
        protected List<Contact> doInBackground(Void... params) {
            if (notesClient != null) {
                Cursor notesCursor = null;
                try {
                    notesCursor = notesClient.query(Data.CONTENT_URI, new String[]{Note.CONTACT_ID, Note.NOTE},
                            Data.MIMETYPE + " = '" + Note.CONTENT_ITEM_TYPE + "' AND " + Note.NOTE + " NOT NULL AND TRIM(" + Note.NOTE + ") != ''",
                            null,
                            Data.DISPLAY_NAME + " COLLATE NOCASE ASC");
                    if (notesCursor != null && notesCursor.moveToFirst()) {
                        final int cIDIndex = notesCursor.getColumnIndex(Note.CONTACT_ID);
                        final int noteIndex = notesCursor.getColumnIndex(Note.NOTE);
                        List<Contact> ContactList = mContactList;
                        List<String> notesList;
                        long cID;
                        Integer index;
                        String notes;
                        for (; !notesCursor.isAfterLast(); notesCursor.moveToNext()) {
                            cID = notesCursor.getLong(cIDIndex);
                            index = Constant.cIDArrayListIndexMap.get(cID);
                            if (index == null)
                                continue;
                            notesList = ContactList.get(index).getDataIndicesByDataIndex(Constant.NOTES);
                            notes = notesCursor.getString(noteIndex);
                            notesList.add(notes.toLowerCase());
                        }
                        return ContactList;
                    }
                } catch (RemoteException e) {
//                    System.out.println("[Azim-contact]::" + e.toString());
                    e.printStackTrace();
                } finally {
                    if (notesCursor != null)
                        notesCursor.close();
                    if (notesClient != null)
                        notesClient.release();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(List<Contact> result) {
            ContactClient.getInstance().finishCommand(NotesLoaderCommand.this);
            if (mProgressBar != null)
                mProgressBar.setVisibility(View.INVISIBLE);
            if (result != null && mContactList != null && mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}