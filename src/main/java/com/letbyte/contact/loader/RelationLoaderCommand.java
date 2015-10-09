package com.letbyte.contact.loader;

import java.util.ArrayList;
import java.util.List;


import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.Data;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.letbyte.contact.control.Constant;
import com.letbyte.contact.data.model.Contact;

//Use separate thread for search and Query to the native-phonebook
public class RelationLoaderCommand implements Command {
	
	private ContentProviderClient relationClient;
	private ProgressBar mProgressBar;
	private RecyclerView.Adapter mAdapter;
	private List<Contact> mContactModelList;
	
	public RelationLoaderCommand(Context context, ProgressBar progressBar, RecyclerView.Adapter adapter, List<Contact> contactModelList) {
		mProgressBar = progressBar;
		mAdapter = adapter;
		this.mContactModelList = contactModelList;
		relationClient = context.getContentResolver().acquireContentProviderClient(Data.CONTENT_URI);
	}


	@Override
	public void execute() {
		new NotesLoaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private class NotesLoaderTask extends AsyncTask<Void, Void, List<Contact>> {

		@Override
		protected List<Contact> doInBackground(Void... params) {
			if(relationClient != null) {
				Cursor relationCursor = null;
				try {
					relationCursor = relationClient.query(Data.CONTENT_URI, new String[]{Relation.CONTACT_ID, Relation.NAME},
							Data.MIMETYPE + " = '"+ Relation.CONTENT_ITEM_TYPE +"' AND (("+
							Relation.NAME+ " NOT NULL AND TRIM("+Relation.NAME+") != ''))"/* OR ("+
							Organization.DEPARTMENT+ " NOT NULL AND TRIM("+Organization.DEPARTMENT+") != '') OR ("+
							Organization.JOB_DESCRIPTION+ " NOT NULL AND TRIM("+Organization.JOB_DESCRIPTION+") != '') OR ("+
							Organization.OFFICE_LOCATION+ " NOT NULL AND TRIM("+Organization.OFFICE_LOCATION+") != '') OR ("+
							Organization.TITLE+ " NOT NULL AND TRIM("+Organization.TITLE+") != '') )"*/,							
							null,
							Data.DISPLAY_NAME + " COLLATE NOCASE ASC");
					if(relationCursor != null && relationCursor.moveToFirst()) {//Check for count
						final int cIDIndex = relationCursor.getColumnIndex(Relation.CONTACT_ID);
						final int nameIndex = relationCursor.getColumnIndex(Relation.NAME);
						/*final int departmentIndex = organizationCursor.getColumnIndex(Organization.DEPARTMENT);
						final int jdIndex = organizationCursor.getColumnIndex(Organization.JOB_DESCRIPTION);
						final int officeLocIndex = organizationCursor.getColumnIndex(Organization.OFFICE_LOCATION);
						final int jtIndex = organizationCursor.getColumnIndex(Organization.TITLE);*/
						List<Contact> contactModelList = mContactModelList;
						List<String> relationList;
						long cID;
						Integer index;
						String name/*, department, jd, location, jt*/;
						for(; !relationCursor.isAfterLast(); relationCursor.moveToNext()) {
							cID = relationCursor.getLong(cIDIndex);
							index = Constant.cIDArrayListIndexMap.get(cID);
							if(index == null)
								continue;
							relationList = contactModelList.get(index).getDataIndicesByDataIndex(Constant.ORGANIZATION);
							name = relationCursor.getString(nameIndex);							
							/*department = relationCursor.getString(departmentIndex);
							jd = relationCursor.getString(jdIndex);
							jt = relationCursor.getString(jtIndex);
							location = relationCursor.getString(officeLocIndex);*/
							
							/*if(company != null)
								relationList.add(company.toLowerCase());
							if(department != null)
								relationList.add(department.toLowerCase());
							if(jd != null)
								relationList.add(jd.toLowerCase());
							if(jt != null)
								relationList.add(jt.toLowerCase());
							if(location != null)*/
							relationList.add(name.toLowerCase());
						}
						return contactModelList;
					}
				} catch (RemoteException e) {
					//System.out.println("[Azim-contact]::"+e.toString());
					e.printStackTrace();
				} finally {
					if(relationCursor != null)
						relationCursor.close();
					if(relationClient != null)
						relationClient.release();
				}
			}
			return null;
		}
		

		@Override
	    protected void onPostExecute(List<Contact> result) {
			ContactClient.getInstance().finishCommand(RelationLoaderCommand.this);
			if(mProgressBar != null)
				mProgressBar.setVisibility(View.INVISIBLE);
			if(result != null && mContactModelList != null && mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
	    }
	}
}