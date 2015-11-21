package com.letbyte.contact.loader;

import android.Manifest;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.letbyte.contact.adapter.ContactAdapter;
import com.letbyte.contact.control.Constant;
import com.letbyte.contact.data.model.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Use separate thread for search and Query to the native-phonebook
public class ContactLoaderCommand implements Command {

    private ContentProviderClient contactClient;
    private ProgressBar mProgressBar;
    private RecyclerView.Adapter mAdapter;
    private List<Contact> mContactModelList;
    private boolean mIsToFilterHasPhoneNumber, mIsNoStrequent;
    private Context mContext;

    //TODO we will set flags bitwise integer rather continuous booleans
    public ContactLoaderCommand(Context context, ProgressBar progressBar, RecyclerView.Adapter adapter,
                                List<Contact> contactModelList, boolean isToFilterHasPhoneNumber, boolean isToPrioritizeStrequent) {
        mProgressBar = progressBar;
        mAdapter = adapter;
        this.mContactModelList = contactModelList;
        this.mIsNoStrequent = !isToPrioritizeStrequent;
        contactClient = context.getContentResolver().acquireContentProviderClient(Contacts.CONTENT_URI);
        this.mIsToFilterHasPhoneNumber = isToFilterHasPhoneNumber;
        mContext = context;
    }


    @Override
    public void execute() {
        new ContactLoaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class ContactLoaderTask extends AsyncTask<Void, Void, ArrayList<Contact>> {

        @Override
          protected ArrayList<Contact> doInBackground(Void... params) {
            long t1 = System.currentTimeMillis();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    mContext.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                    return null;
//            if(mIsNoStrequent)//Maintain HashMap<ContactID, Contact(contact model)> So that after adding strequent's
            //not same normal contact would add in the list.
            //Moreover rather customarraylist Hashmap can be a faster choice - Need investigation
            if (contactClient != null) {
                Cursor contactCursor = null, strequentCursor = null;
                ArrayList<Contact> contactModelList = new ArrayList<>();
                String displayNamePrimary, displayNamePrimaryOrig, displayName, displayNameAlternative, imageUrl;
                Long cID;
                int index = 0;
                try {
                    ArrayList<Long> streuqentList = new ArrayList<>();
                    if(!mIsNoStrequent) {//Should show some special marker for Prioritized contacts
                        strequentCursor = contactClient.query(Contacts.CONTENT_STREQUENT_URI, null, null, null,
                                Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC");
                        if(strequentCursor != null) {
                            if(strequentCursor.moveToFirst()) {
                                final int contactIDIndex = strequentCursor.getColumnIndex(Contacts._ID);
                                final int displayNamePrimaryIndex = strequentCursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                                final int displayNameIndex = strequentCursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                                final int displayNameAlternativeIndex = strequentCursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                                final int photoThumbNailIndex = strequentCursor.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI);
                                ArrayList<String> displayNameList;
                                //TODO transfer disName related task in separate method
                                //Check either binary search(724.4ms) or hashmap avg (740.2ms)
                                do {
                                    cID = strequentCursor.getLong(contactIDIndex);
                                    streuqentList.add(cID);
                                    imageUrl = strequentCursor.getString(photoThumbNailIndex);
                                    displayNameList = new ArrayList<>(2);
                                    displayNamePrimary = strequentCursor.getString(displayNamePrimaryIndex);
                                    displayName = strequentCursor.getString(displayNameIndex);
                                    displayNameAlternative = strequentCursor.getString(displayNameAlternativeIndex);

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
                                    Constant.cIDArrayListIndexMap.put(cID, index++);
                                    contactModelList.add(new Contact(cID, displayNamePrimaryOrig, imageUrl, displayNameList, true));
                                } while(strequentCursor.moveToNext());
                                Collections.sort(streuqentList);
                            }
                            strequentCursor.close();
                        }
                    }
                    String filterString = (mIsToFilterHasPhoneNumber ? Contacts.HAS_PHONE_NUMBER + " = ? AND " : Constant.EMPTY_STRING) +
                            Contacts.DISPLAY_NAME_PRIMARY + " NOT NULL";
                    String[] arguments = mIsToFilterHasPhoneNumber ? new String[]{"1"} : null;
                    contactCursor = contactClient.query(Contacts.CONTENT_URI, new String[]{Contacts.DISPLAY_NAME_PRIMARY, Contacts.DISPLAY_NAME,
                                    Contacts.DISPLAY_NAME_ALTERNATIVE, Contacts._ID, Contacts.PHOTO_THUMBNAIL_URI},
                            filterString, arguments, /*Contacts.STARRED + " DESC, " +*/
                            Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC");
                    if (contactCursor != null && contactCursor.moveToFirst()) {
                        final int contactIDIndex = contactCursor.getColumnIndex(Contacts._ID);
                        final int displayNamePrimaryIndex = contactCursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                        final int displayNameIndex = contactCursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                        final int displayNameAlternativeIndex = contactCursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                        final int photoThumbNailIndex = contactCursor.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI);
                        ArrayList<String> displayNameList;
                        for (; !contactCursor.isAfterLast(); contactCursor.moveToNext()) {
                            cID = contactCursor.getLong(contactIDIndex);
                            if(mIsNoStrequent || Collections.binarySearch(streuqentList, cID) < 0) {//Ensuring, this contact is not in fav list
                                displayNameList = new ArrayList<>(2);
                            /*displayNamePrimaryOrig = */displayNamePrimary = contactCursor.getString(displayNamePrimaryIndex);
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

                                imageUrl = contactCursor.getString(photoThumbNailIndex);
                                Constant.cIDArrayListIndexMap.put(cID, index++);
                                //contactModelList.add(ContactModel.putInContactModel(cID, displayNamePrimaryOrig, displayNameList, imageUrl));//Add number list at same time
                                contactModelList.add(new Contact(cID, displayNamePrimaryOrig, imageUrl, displayNameList));
                                System.out.println("[Azim-contact]::" + displayNamePrimary);
                            } //HashMap might automatically reject new entry check which is best
                        }
                        System.out.println("[Azim-time]::"+(System.currentTimeMillis() - t1) + " ms");
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