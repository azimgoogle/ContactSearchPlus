package com.letbyte.contact.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.letbyte.contact.control.Constant;
import com.letbyte.contact.data.model.Contact;

//Use separate thread for search and Query to the native-phonebook
public class EMailLoaderCommand implements Command {

    private ContentProviderClient emailClient;
    private ProgressBar mProgressBar;
    private RecyclerView.Adapter mAdapter;
    private List<Contact> mContactList;
    private boolean mIsToFilterHasPhoneNumber;

    public EMailLoaderCommand(Context context, ProgressBar progressBar, RecyclerView.Adapter adapter,
                              List<Contact> ContactList, boolean isToFilterPhoneNumber) {
        mProgressBar = progressBar;
        mAdapter = adapter;
        this.mContactList = ContactList;
        emailClient = context.getContentResolver().acquireContentProviderClient(Email.CONTENT_URI);
        this.mIsToFilterHasPhoneNumber = isToFilterPhoneNumber;
    }


    @Override
    public void execute() {
        new EMailLoaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class EMailLoaderTask extends AsyncTask<Void, Void, List<Contact>> {

        @Override
        protected List<Contact> doInBackground(Void... params) {
            if (emailClient != null) {
                Cursor emailCursor = null;
                try {
                    String filterString = (mIsToFilterHasPhoneNumber ? Email.HAS_PHONE_NUMBER + " = ? AND " : Constant.EMPTY_STRING) +
                            Email.DISPLAY_NAME_PRIMARY + " NOT NULL";
                    String[] arguments = mIsToFilterHasPhoneNumber ? new String[]{"1"} : null;
                    emailCursor = emailClient.query(Email.CONTENT_URI, new String[]{Email.CONTACT_ID, Email.ADDRESS},
                            filterString, arguments, null);
                    if (emailCursor != null && emailCursor.moveToFirst()) {
                        final int emailAddressIndex = emailCursor.getColumnIndex(Email.ADDRESS);
                        final int contactIDIndex = emailCursor.getColumnIndex(Email.CONTACT_ID);
                        List<Contact> ContactList = mContactList;
                        List<String> mailAddressList;
                        long cID;
                        int index;
                        String mailAddress;
                        for (; !emailCursor.isAfterLast(); emailCursor.moveToNext()) {
                            cID = emailCursor.getLong(contactIDIndex);
                            index = Constant.cIDArrayListIndexMap.get(cID);
                            mailAddressList = ContactList.get(index).getDataIndicesByDataIndex(Constant.MAIL_ADDRESS);
                            mailAddress = emailCursor.getString(emailAddressIndex);
                            mailAddressList.add(mailAddress.toLowerCase());
                        }
                        return ContactList;
                    }
                } catch (RemoteException e) {
                    System.out.println("[Azim-contact]::" + e.toString());
                    e.printStackTrace();
                } finally {
                    if (emailCursor != null)
                        emailCursor.close();
                    if (emailClient != null)
                        emailClient.release();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(List<Contact> result) {
            ContactClient.getInstance().finishCommand(EMailLoaderCommand.this);
            if (mProgressBar != null)
                mProgressBar.setVisibility(View.INVISIBLE);
            if (result != null && mContactList != null && mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}