package com.letbyte.contact.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.letbyte.contact.control.Constant;
import com.letbyte.contact.data.model.Contact;

//Use separate thread for search and Query to the native-phonebook
public class PhoneNumberLoaderCommand implements Command {
	
	private ContentProviderClient phoneClient;
	private ProgressBar mProgressBar;
	private RecyclerView.Adapter mAdapter;
	private List<Contact> mContactList;
	
	public PhoneNumberLoaderCommand(Context context, ProgressBar progressBar, RecyclerView.Adapter adapter,
			List<Contact> ContactList) {
		mProgressBar = progressBar;
		mAdapter = adapter;
		this.mContactList = ContactList;
		phoneClient = context.getContentResolver().acquireContentProviderClient(Phone.CONTENT_URI);
	}


	@Override
	public void execute() {
		new ContactLoaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private class ContactLoaderTask extends AsyncTask<Void, Void, List<Contact>> {

		@Override
		protected List<Contact> doInBackground(Void... params) {
			try {
				if(phoneClient != null) {
					Cursor phoneCursor = null;
					try {
						phoneCursor = phoneClient.query(Phone.CONTENT_URI, new String[]{Phone.CONTACT_ID, Phone.NUMBER},
								Phone.HAS_PHONE_NUMBER + " = ? AND "+Phone.DISPLAY_NAME_PRIMARY + " NOT NULL", new String[]{"1"}, null);
						if(phoneCursor != null && phoneCursor.moveToFirst()) {
							final int phoneContactIDIndex = phoneCursor.getColumnIndex(Phone.CONTACT_ID);
							final int phoneNumberIndex = phoneCursor.getColumnIndex(Phone.NUMBER);
							List<Contact> ContactList = mContactList;//use less
							String phoneNumber;
							long cID;
							int index;
							List<String> numberList;
							for(; !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {
								cID = phoneCursor.getLong(phoneContactIDIndex);
								index = Constant.cIDArrayListIndexMap.get(cID);
								numberList = mContactList.get(index).getDataIndicesByDataIndex(Constant.PHONE_NUMBER);
								phoneNumber = phoneCursor.getString(phoneNumberIndex);
								//must keep plus sign
								phoneNumber = phoneNumber.replaceAll(Constant.PHONE_NUMBER_REG_EX, Constant.EMPTY_STRING);//Can work to retain initial +
								numberList.add(phoneNumber);
							}
							return ContactList;//use less
						}
					} catch (RemoteException e) {
						System.out.println("[Azim-contact]::"+e.toString());
						e.printStackTrace();
					} finally {
						if(phoneCursor != null)
							phoneCursor.close();
						if(phoneClient != null)
							phoneClient.release();
					}
				}	
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}
		

		@Override
	    protected void onPostExecute(List<Contact> result) {
			ContactClient.getInstance().finishCommand(PhoneNumberLoaderCommand.this);
			if(mProgressBar != null)
				mProgressBar.setVisibility(View.INVISIBLE);
			if(result != null && mContactList != null && mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
	    }
	}
}