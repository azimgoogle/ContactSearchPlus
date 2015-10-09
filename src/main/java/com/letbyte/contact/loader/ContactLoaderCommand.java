package com.letbyte.contact.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.letbyte.contact.adapter.ContactAdapter;
import com.letbyte.contact.control.Constant;
import com.letbyte.contact.data.model.Contact;

//Use separate thread for search and Query to the native-phonebook
public class ContactLoaderCommand implements Command {

    private ContentProviderClient contactClient;
    private ProgressBar mProgressBar;
    private RecyclerView.Adapter mAdapter;
    private List<Contact> mContactModelList;
    private boolean mIsToFilterHasPhoneNumber;

    public ContactLoaderCommand(Context context, ProgressBar progressBar, RecyclerView.Adapter adapter,
                                List<Contact> contactModelList, boolean isToFilterHasPhoneNumber) {
        mProgressBar = progressBar;
        mAdapter = adapter;
        this.mContactModelList = contactModelList;
        contactClient = context.getContentResolver().acquireContentProviderClient(Contacts.CONTENT_URI);
        this.mIsToFilterHasPhoneNumber = isToFilterHasPhoneNumber;
    }


    @Override
    public void execute() {
        new ContactLoaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class ContactLoaderTask extends AsyncTask<Void, Void, ArrayList<Contact>> {

        @Override
        protected ArrayList<Contact> doInBackground(Void... params) {
            if (contactClient != null) {
                Cursor contactCursor = null;
                try {
                    String filterString = (mIsToFilterHasPhoneNumber ? Contacts.HAS_PHONE_NUMBER + " = ? AND " : Constant.EMPTY_STRING) +
                            Contacts.DISPLAY_NAME_PRIMARY + " NOT NULL";
                    String[] arguments = mIsToFilterHasPhoneNumber ? new String[]{"1"} : null;
                    contactCursor = contactClient.query(Contacts.CONTENT_URI, new String[]{Contacts.DISPLAY_NAME_PRIMARY, Contacts.DISPLAY_NAME,
                                    Contacts.DISPLAY_NAME_ALTERNATIVE, Contacts._ID, Contacts.PHOTO_THUMBNAIL_URI},
                            filterString, arguments,
                            Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC");
                    if (contactCursor != null && contactCursor.moveToFirst()) {
                        final int displayNamePrimaryIndex = contactCursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                        final int displayNameIndex = contactCursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                        final int displayNameAlternativeIndex = contactCursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                        final int contactIDIndex = contactCursor.getColumnIndex(Contacts._ID);
                        final int photoThumbNailIndex = contactCursor.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI);
                        ArrayList<Contact> contactModelList = new ArrayList<Contact>();
                        ArrayList<String> displayNameList;
                        String displayNamePrimary, displayNamePrimaryOrig, displayName, displayNameAlternative, imageUrl;
                        long cID;
                        int index = 0;
                        for (; !contactCursor.isAfterLast(); contactCursor.moveToNext()) {
                            displayNameList = new ArrayList<>(2);
                            displayNamePrimaryOrig = displayNamePrimary = contactCursor.getString(displayNamePrimaryIndex);
                            displayName = contactCursor.getString(displayNameIndex);
                            displayNameAlternative = contactCursor.getString(displayNameAlternativeIndex);

                            displayNamePrimaryOrig = displayNamePrimary;//To display only
                            displayNamePrimary = displayNamePrimary.toLowerCase();
                            displayNameList.add(displayNamePrimary);//Keeping lower case entry for BG comparison

                            if (displayName != null) {
                                displayName = displayName.toLowerCase();
                                if (!displayNamePrimary.equals(displayName))
                                    displayNameList.add(displayName);
                            }

                            if (displayNameAlternative != null) {
                                displayNameAlternative = displayNameAlternative.toLowerCase();
                                if (!displayNamePrimary.equals(displayNameAlternative) &&
                                        !(displayName != null && displayName.equals(displayNameAlternative)))
                                    displayNameList.add(displayNameAlternative);
                            }

                            cID = contactCursor.getLong(contactIDIndex);
                            imageUrl = contactCursor.getString(photoThumbNailIndex);
                            Constant.cIDArrayListIndexMap.put(cID, index++);
                            //contactModelList.add(ContactModel.putInContactModel(cID, displayNamePrimaryOrig, displayNameList, imageUrl));//Add number list at same time
                            contactModelList.add(new Contact(cID, displayNamePrimaryOrig, imageUrl, displayNameList));
                            System.out.println("[Azim-contact]::" + displayNamePrimary);
                        }
                        return contactModelList;
                    }
                } catch (RemoteException e) {
                    System.out.println("[Azim-contact]::" + e.toString());
                    e.printStackTrace();
                } finally {
                    if (contactCursor != null)
                        contactCursor.close();
                    if (contactClient != null)
                        contactClient.release();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(ArrayList<Contact> result) {
            ContactClient.getInstance().finishCommand(ContactLoaderCommand.this);
            if (mProgressBar != null)
                mProgressBar.setVisibility(View.INVISIBLE);
            if (result != null && mContactModelList != null && mAdapter != null) {
                mContactModelList.clear();
                mContactModelList.addAll(result);
                ((ContactAdapter) mAdapter).adds(mContactModelList);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}