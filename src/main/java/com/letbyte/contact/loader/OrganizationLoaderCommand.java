package com.letbyte.contact.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.Data;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.letbyte.contact.control.Constant;
import com.letbyte.contact.data.model.Contact;

//Use separate thread for search and Query to the native-phonebook
public class OrganizationLoaderCommand implements Command {

    private ContentProviderClient organizationClient;
    private ProgressBar mProgressBar;
    private RecyclerView.Adapter mAdapter;
    private List<Contact> mContactList;

    public OrganizationLoaderCommand(Context context, ProgressBar progressBar, RecyclerView.Adapter adapter, List<Contact> ContactList) {
        mProgressBar = progressBar;
        mAdapter = adapter;
        this.mContactList = ContactList;
        organizationClient = context.getContentResolver().acquireContentProviderClient(Data.CONTENT_URI);
    }


    @Override
    public void execute() {
        new NotesLoaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class NotesLoaderTask extends AsyncTask<Void, Void, List<Contact>> {

        @Override
        protected List<Contact> doInBackground(Void... params) {
            if (organizationClient != null) {
                Cursor organizationCursor = null;
                try {
                    organizationCursor = organizationClient.query(Data.CONTENT_URI, new String[]{Organization.CONTACT_ID, Organization.COMPANY,
                                    Organization.DEPARTMENT, Organization.JOB_DESCRIPTION, Organization.OFFICE_LOCATION, Organization.TITLE},
                            Data.MIMETYPE + " = '" + Organization.CONTENT_ITEM_TYPE + "' AND ((" +
                                    Organization.COMPANY + " NOT NULL AND TRIM(" + Organization.COMPANY + ") != '') OR (" +
                                    Organization.DEPARTMENT + " NOT NULL AND TRIM(" + Organization.DEPARTMENT + ") != '') OR (" +
                                    Organization.JOB_DESCRIPTION + " NOT NULL AND TRIM(" + Organization.JOB_DESCRIPTION + ") != '') OR (" +
                                    Organization.OFFICE_LOCATION + " NOT NULL AND TRIM(" + Organization.OFFICE_LOCATION + ") != '') OR (" +
                                    Organization.TITLE + " NOT NULL AND TRIM(" + Organization.TITLE + ") != '') )",
                            null,
                            Data.DISPLAY_NAME + " COLLATE NOCASE ASC");
                    if (organizationCursor != null && organizationCursor.moveToFirst()) {//Check for count
                        final int cIDIndex = organizationCursor.getColumnIndex(Organization.CONTACT_ID);
                        final int companyIndex = organizationCursor.getColumnIndex(Organization.COMPANY);
                        final int departmentIndex = organizationCursor.getColumnIndex(Organization.DEPARTMENT);
                        final int jdIndex = organizationCursor.getColumnIndex(Organization.JOB_DESCRIPTION);
                        final int officeLocIndex = organizationCursor.getColumnIndex(Organization.OFFICE_LOCATION);
                        final int jtIndex = organizationCursor.getColumnIndex(Organization.TITLE);
                        List<Contact> ContactList = mContactList;

                        List<String> organizationsList;
                        long cID;
                        Integer index;
                        String company, department, jd, location, jt;
                        for (; !organizationCursor.isAfterLast(); organizationCursor.moveToNext()) {
                            cID = organizationCursor.getLong(cIDIndex);
                            index = Constant.cIDArrayListIndexMap.get(cID);
                            if (index == null)
                                continue;
                            organizationsList = ContactList.get(index).getDataIndicesByDataIndex(Constant.ORGANIZATION);
                            company = organizationCursor.getString(companyIndex);
                            department = organizationCursor.getString(departmentIndex);
                            jd = organizationCursor.getString(jdIndex);
                            jt = organizationCursor.getString(jtIndex);
                            location = organizationCursor.getString(officeLocIndex);

                            if (company != null)
                                organizationsList.add(company.toLowerCase());
                            if (department != null)
                                organizationsList.add(department.toLowerCase());
                            if (jd != null)
                                organizationsList.add(jd.toLowerCase());
                            if (jt != null)
                                organizationsList.add(jt.toLowerCase());
                            if (location != null)
                                organizationsList.add(location.toLowerCase());
                        }
                        return ContactList;
                    }
                } catch (RemoteException e) {
                    System.out.println("[Azim-contact]::" + e.toString());
                    e.printStackTrace();
                } finally {
                    if (organizationCursor != null)
                        organizationCursor.close();
                    if (organizationClient != null)
                        organizationClient.release();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(List<Contact> result) {
            ContactClient.getInstance().finishCommand(OrganizationLoaderCommand.this);
            if (mProgressBar != null)
                mProgressBar.setVisibility(View.INVISIBLE);
            if (result != null && mContactList != null && mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}