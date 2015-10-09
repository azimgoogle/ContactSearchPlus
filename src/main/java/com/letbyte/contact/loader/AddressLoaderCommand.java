package com.letbyte.contact.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.letbyte.contact.control.Constant;
import com.letbyte.contact.data.model.Contact;

//Use separate thread for search and Query to the native-phonebook
public class AddressLoaderCommand implements Command {
	
	private ContentProviderClient structuredPostalClient;
	private ProgressBar mProgressBar;
	private RecyclerView.Adapter mAdapter;
	private List<Contact> mContactList;
	private boolean mIsToFilterHasPhoneNumber;
	
	public AddressLoaderCommand(Context context, ProgressBar progressBar, RecyclerView.Adapter adapter,
								List<Contact> ContactList, boolean isToFilterPhoneNumber) {
		mProgressBar = progressBar;
		mAdapter = adapter;
		this.mContactList = ContactList;
		structuredPostalClient = context.getContentResolver().acquireContentProviderClient(StructuredPostal.CONTENT_URI);
		this.mIsToFilterHasPhoneNumber = isToFilterPhoneNumber;
	}


	@Override
	public void execute() {
		new EMailLoaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private class EMailLoaderTask extends AsyncTask<Void, Void, List<Contact>> {

		@Override
		protected List<Contact> doInBackground(Void... params) {
			if(structuredPostalClient != null) {
				Cursor structuredPostalCursor = null;
				try {
					String filterString = (mIsToFilterHasPhoneNumber ? StructuredPostal.HAS_PHONE_NUMBER + " = ? AND " : Constant.EMPTY_STRING) +
							StructuredPostal.DISPLAY_NAME_PRIMARY + " NOT NULL  AND (("+
							StructuredPostal.CITY + " NOT NULL AND TRIM("+StructuredPostal.CITY+") != '') OR ("+
							StructuredPostal.COUNTRY + " NOT NULL AND TRIM("+StructuredPostal.COUNTRY+") != '') OR ("+
							StructuredPostal.REGION + " NOT NULL AND TRIM("+StructuredPostal.REGION+") != '') OR ("+
							StructuredPostal.STREET + " NOT NULL AND TRIM("+StructuredPostal.STREET+") != '') )";
					String[] arguments = mIsToFilterHasPhoneNumber ? new String[]{"1"} : null;
					structuredPostalCursor = structuredPostalClient.query(StructuredPostal.CONTENT_URI, new String[]{StructuredPostal.CONTACT_ID, StructuredPostal.CITY,
							StructuredPostal.COUNTRY, StructuredPostal.REGION, StructuredPostal.STREET},
							filterString, arguments,
							StructuredPostal.DISPLAY_NAME + " COLLATE NOCASE ASC");
					if(structuredPostalCursor != null && structuredPostalCursor.moveToFirst()) {
						final int cIDIndex = structuredPostalCursor.getColumnIndex(StructuredPostal.CONTACT_ID);
						final int cityIndex = structuredPostalCursor.getColumnIndex(StructuredPostal.CITY);
						final int countryIndex = structuredPostalCursor.getColumnIndex(StructuredPostal.COUNTRY);
						final int regionIndex = structuredPostalCursor.getColumnIndex(StructuredPostal.REGION);
						final int streetIndex = structuredPostalCursor.getColumnIndex(StructuredPostal.STREET);
						List<Contact> ContactList = mContactList;
						List<String> addressList;
						long cID;
						int index;
						String city, country, region, street;
						for(; !structuredPostalCursor.isAfterLast(); structuredPostalCursor.moveToNext()) {
							cID = structuredPostalCursor.getLong(cIDIndex);
							index = Constant.cIDArrayListIndexMap.get(cID);
							addressList = ContactList.get(index).getDataIndicesByDataIndex(Constant.ADDRESS);
							city = structuredPostalCursor.getString(cityIndex);
							country = structuredPostalCursor.getString(countryIndex);
							region = structuredPostalCursor.getString(regionIndex);
							street = structuredPostalCursor.getString(streetIndex);
							if(city != null)
								addressList.add(city.toLowerCase());
							if(country != null)
								addressList.add(country.toLowerCase());
							if(region != null)
								addressList.add(region.toLowerCase());
							if(street != null)
								addressList.add(street.toLowerCase());						
						}
						return ContactList;
					}
				} catch (RemoteException e) {
					System.out.println("[Azim-contact]::"+e.toString());
					e.printStackTrace();
				} finally {
					if(structuredPostalCursor != null)
						structuredPostalCursor.close();
					if(structuredPostalClient != null)
						structuredPostalClient.release();
				}
			}
			return null;
		}
		

		@Override
	    protected void onPostExecute(List<Contact> result) {
			ContactClient.getInstance().finishCommand(AddressLoaderCommand.this);
			if(mProgressBar != null)
				mProgressBar.setVisibility(View.INVISIBLE);
			if(result != null && mContactList != null && mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
	    }
	}
}