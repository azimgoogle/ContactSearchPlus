package com.letbyte.contact.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.letbyte.contact.R;
import com.letbyte.contact.adapter.ContactAdapter;
import com.letbyte.contact.control.Constant;
import com.letbyte.contact.control.PrefManager;
import com.letbyte.contact.data.model.Contact;
import com.letbyte.contact.databinding.ActivityMainBinding;
import com.letbyte.contact.drawable.RecyclerViewDividerItemDecorator;
import com.letbyte.contact.listener.RecyclerItemClickListener;
import com.letbyte.contact.loader.AddressLoaderCommand;
import com.letbyte.contact.loader.ContactClient;
import com.letbyte.contact.loader.ContactLoaderCommand;
import com.letbyte.contact.loader.EMailLoaderCommand;
import com.letbyte.contact.loader.NotesLoaderCommand;
import com.letbyte.contact.loader.OrganizationLoaderCommand;
import com.letbyte.contact.loader.PhoneNumberLoaderCommand;
import com.letbyte.contact.loader.RelationLoaderCommand;
import com.letbyte.contact.task.SyncTask;
import com.letbyte.contact.utility.ContactUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ActivityMainBinding binding;
    private final int[] searchIndexes = new int[]{
            Constant.DISPLAY_NAME,
            Constant.PHONE_NUMBER,
            Constant.MAIL_ADDRESS,
            Constant.ADDRESS,
            Constant.NOTES,
            Constant.ORGANIZATION,
            Constant.RELATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(binding.toolbar);

        getRecyclerView().setHasFixedSize(true);
        getRecyclerView().setLayoutManager(new LinearLayoutManager(this));
        getRecyclerView().addItemDecoration(new RecyclerViewDividerItemDecorator(this, null));
        final ContactAdapter adapter = new ContactAdapter(R.layout.contact, new ArrayList<Contact>());
        getRecyclerView().setAdapter(adapter);
        getRecyclerView().addOnItemTouchListener(new RecyclerItemClickListener(this, getRecyclerView(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final long contactID = adapter.getContactIDbyPosition(position);
                showContactDetailsView(contactID);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));

        syncAdapter();

        boolean bootSynced = PrefManager.on(this.getBaseContext()).isBootSynced();
        boolean synced = PrefManager.on(this.getBaseContext()).isSynced();
        if (!bootSynced) {
            new SyncTask(this.getBaseContext()).execute(new HashMap<String, Boolean>());
        } else if (!synced) {
            new SyncTask(this.getBaseContext()).execute(PrefManager.on(this.getBaseContext()).getConfig());
        }
    }

    private void showContactDetailsView(long contactID) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));
        intent.setData(uri);
        startActivity(intent);
    }

    private boolean makeCall(long contactID) {
        String number = new ContactUtility(getApplicationContext()).getNumberfromContactID(contactID);
        if (number == null)
            return false;
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        startActivity(intent);
        return true;
    }

    private boolean sendMessage(long contactID) {
        String number = new ContactUtility(getApplicationContext()).getNumberfromContactID(contactID);
        if (number == null)
            return false;
        Uri uri = Uri.parse("smsto:" + number);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", "The SMS text");
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, Constant.REQUESTCODE_SETTINGS);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUESTCODE_SETTINGS:
                if (resultCode == RESULT_OK) {
                    boolean isChanged = data.getBooleanExtra("isChanged", false);
                    if (isChanged) {
                        syncAdapter();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Contact> filteredContacts = filter(Constant.contactModelList, newText);
        apply(filteredContacts);
        return true;
    }


    private RecyclerView getRecyclerView() {
        return (RecyclerView) findViewById(R.id.recycler);
    }

    private void apply(final List<Contact> filteredContacts) {
        ((ContactAdapter) getRecyclerView().getAdapter()).applyTo(filteredContacts);
        getRecyclerView().scrollToPosition(0);
    }

    private ProgressBar getProgressBar() {
        return (ProgressBar) findViewById(R.id.progressBar);
    }

    private void syncAdapter() {
        ProgressBar progressBar = getProgressBar();
        progressBar.setVisibility(View.VISIBLE);
        RecyclerView.Adapter mAdapter = getRecyclerView().getAdapter();


        boolean isToFilterPhoneNumber = PrefManager.on(this).isToFilterByNumber();

        ContactClient.getInstance().addCommand(new ContactLoaderCommand(this, progressBar, mAdapter,
                Constant.contactModelList, isToFilterPhoneNumber));

        boolean isToload = PrefManager.on(this).isNumber();
        if (isToload)
            ContactClient.getInstance().addCommand(new PhoneNumberLoaderCommand(this, progressBar, mAdapter, Constant.contactModelList));

        isToload = PrefManager.on(this).isEmail();
        if (isToload)
            ContactClient.getInstance().addCommand(new EMailLoaderCommand(this, progressBar, mAdapter, Constant.
                    contactModelList, isToFilterPhoneNumber));

        isToload = PrefManager.on(this).isAddress();
        if (isToload)
            ContactClient.getInstance().addCommand(new AddressLoaderCommand(this, progressBar, mAdapter,
                    Constant.contactModelList, isToFilterPhoneNumber));

        isToload = PrefManager.on(this).isNotes();
        if (isToload)
            ContactClient.getInstance().addCommand(new NotesLoaderCommand(this, progressBar, mAdapter, Constant.contactModelList));

        isToload = PrefManager.on(this).isOrg();
        if (isToload)
            ContactClient.getInstance().addCommand(new OrganizationLoaderCommand(this, progressBar, mAdapter, Constant.contactModelList));

        isToload = PrefManager.on(this).isRelation();
        if (isToload)
            ContactClient.getInstance().addCommand(new RelationLoaderCommand(this, progressBar, mAdapter, Constant.contactModelList));
    }

    private List<Contact> filter(List<Contact> contacts, String query) {

        List<Contact> filteredContacts = new ArrayList<>();

        if (TextUtils.isEmpty(query)) {
            for (Contact contact : contacts) {
                contact.setSubTextSpanned(Spannable.Factory.getInstance().newSpannable(Constant.EMPTY_STRING));
                filteredContacts.add(contact);
            }

            return filteredContacts;
        }

        String realQuery = query;

        query = query.toLowerCase();

        List<String> searchList;
        boolean isMatched;
        int indexOfSubString;
        String temp, subString;
        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            isMatched = false;
            for (int index : searchIndexes) {
                searchList = contact.getDataIndicesByDataIndex(index);
                for (String value : searchList) {
                    indexOfSubString = value.indexOf(query);
                    if (indexOfSubString != -1) {
                        isMatched = true;

                        if (index != Constant.DISPLAY_NAME) {//If display name then manipulate display name particularly
                            subString = value.substring(0, indexOfSubString);
                            subString += "<b>" + realQuery + "</b>";
                            subString += value.substring(indexOfSubString + realQuery.length(), value.length());
                            contact.setSubTextSpanned(Html.fromHtml(subString));

                           /* Spannable str = Spannable.Factory.getInstance().newSpannable(value);
                            str.setSpan(new StyleSpan(Typeface.BOLD), 0, value.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            contact.setSubText(String.valueOf(str));*/

                        }
                        filteredContacts.add(contact);

                        Spannable s;
                        String st;

                        break;

                    }
                }
                if (isMatched)
                    break;
            }
        }

        return filteredContacts;
    }
}
