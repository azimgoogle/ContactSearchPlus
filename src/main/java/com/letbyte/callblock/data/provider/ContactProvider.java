package com.letbyte.callblock.data.provider;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import java.util.LinkedHashMap;

/**
 * Created by Max on 15-Nov-15.
 */
public class ContactProvider extends AsyncTask<Void, Void, LinkedHashMap<Long, String>> {
    private Context mContext;

    public ContactProvider(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected LinkedHashMap<Long, String> doInBackground(Void... params) {
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE NOCASE ASC";
        Cursor cursorContacts = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Contacts._ID},
                ContactsContract.Contacts.HAS_PHONE_NUMBER + " = ?", new String[]{"1"}, sortOrder);
        if (cursorContacts != null) {
            LinkedHashMap<Long, String> map = new LinkedHashMap<>();
            if (cursorContacts.moveToFirst()) {
                final int contactIDIndex = cursorContacts.getColumnIndex(ContactsContract.Contacts._ID);
                final int displayNameIndex = cursorContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
                do {
                    map.put(cursorContacts.getLong(contactIDIndex), cursorContacts.getString(displayNameIndex));
                } while (cursorContacts.moveToNext());
            }
            cursorContacts.close();
            return map;
        }
        return null;
    }

    @Override
    protected void onPostExecute(LinkedHashMap<Long, String> map) {
        super.onPostExecute(map);
    }
}
