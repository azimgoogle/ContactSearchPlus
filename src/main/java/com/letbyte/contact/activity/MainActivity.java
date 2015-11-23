package com.letbyte.contact.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.Tracker;
import com.letbyte.contact.R;
import com.letbyte.contact.adapter.ContactAdapter;
import com.letbyte.contact.application.Application;
import com.letbyte.contact.control.Constant;
import com.letbyte.contact.control.PrefManager;
import com.letbyte.contact.control.Util;
import com.letbyte.contact.data.model.Contact;
import com.letbyte.contact.databinding.ActivityMainBinding;
import com.letbyte.contact.listener.ContactLoadingFinishedListener;
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

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, ContactLoadingFinishedListener {

    private ActivityMainBinding binding;
    private final int[] searchIndexes = new int[]{
            Constant.DISPLAY_NAME,
            Constant.PHONE_NUMBER,
            Constant.MAIL_ADDRESS,
            Constant.ADDRESS,
            Constant.NOTES,
            Constant.ORGANIZATION,
            Constant.RELATION};//Change final and search indexes during Adapter syncing,to reduce search domain
    private boolean isToCallOnSingleTap;
    private SearchView mSearchView;
    private Tracker tracker;
    private ContactAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application application = (Application) getApplication();
        tracker = application.getDefaultTracker();


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(binding.toolbar);
        RecyclerView recyclerView = getRecyclerView();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //getRecyclerView().addItemDecoration(new RecyclerViewDividerItemDecorator(this, null));
        mAdapter = new ContactAdapter(R.layout.contact, Constant.contactModelList);
        recyclerView.setAdapter(mAdapter);
        syncAdapter();
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, getRecyclerView(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(ContactAdapter.IS_SCROLLING_IDLE) {
                    final long contactID = mAdapter.getContactIDbyPosition(position);
                    if (isToCallOnSingleTap)
                        makeCall(contactID);
                    else
                        showContactDetailsView(contactID);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                final long contactID = mAdapter.getContactIDbyPosition(position);
                showContextMenu(contactID, view);
            }
        }));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        ContactAdapter.IS_SCROLLING_IDLE = true;
                        mAdapter.notifyDataSetChanged();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        ContactAdapter.IS_SCROLLING_IDLE = false;
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        ContactAdapter.IS_SCROLLING_IDLE = false;
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });


        boolean bootSynced = PrefManager.on(this.getBaseContext()).isBootSynced();
        boolean synced = PrefManager.on(this.getBaseContext()).isSynced();
        if (!bootSynced) {
            new SyncTask(this.getBaseContext()).execute(new HashMap<String, Boolean>());
        } else if (!synced) {
            new SyncTask(this.getBaseContext()).execute(PrefManager.on(getBaseContext()).getConfig());
        }

    }

    private AdView getAdView() {
        return (AdView) findViewById(R.id.adView);
    }

    private void resolveAdView() {
        new Thread(new Runnable() {
            boolean isOnline;

            @Override
            public void run() {

                isOnline = Util.isOnline();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (isOnline) {
                            getAdView().loadAd(new AdRequest.Builder().build());
                        }

                        getAdView().setVisibility(isOnline ? View.VISIBLE : View.GONE);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Application application = (Application) getApplication();
        application.trackMe(getClass().getName());

        resolveAdView();
    }

    private void showContextMenu(final long contactID, View view) {
        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
//                Toast.makeText(MainActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                switch (item.getItemId()) {
                    case R.id.call:
                        makeCall(contactID);
                        break;
                    case R.id.message:
                        sendMessage(contactID);
                        break;
                    case R.id.details:
                        showContactDetailsView(contactID);
                        break;
                }
                return true;
            }
        });
        popup.setGravity(Gravity.RIGHT);
        popup.show();
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
//        intent.putExtra("sms_body", "The SMS text");
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.expandActionView();
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        mSearchView = searchView;
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
        /*final List<Contact> filteredContacts = filter(Constant.contactModelList, newText);
        apply(filteredContacts);*/
        mAdapter.getFilter().filter(newText);
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


        boolean isToFilterPhoneNumber = PrefManager.on(this).isFilteredByNumber();
        boolean isPrioritizeStrequent = PrefManager.on(this).isStrequentPriority();


        ContactClient.getInstance().setContactLoadingFinishedListener(this);
        ContactClient.getInstance().addCommand(new ContactLoaderCommand(this, progressBar, mAdapter,
                Constant.contactModelList, isToFilterPhoneNumber, isPrioritizeStrequent));

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

        isToload = PrefManager.on(this).isNote();
        if (isToload)
            ContactClient.getInstance().addCommand(new NotesLoaderCommand(this, progressBar, mAdapter, Constant.contactModelList));

        isToload = PrefManager.on(this).isOrganization();
        if (isToload)
            ContactClient.getInstance().addCommand(new OrganizationLoaderCommand(this, progressBar, mAdapter, Constant.contactModelList));

        isToload = PrefManager.on(this).isRelation();
        if (isToload)
            ContactClient.getInstance().addCommand(new RelationLoaderCommand(this, progressBar, mAdapter, Constant.contactModelList));

        isToCallOnSingleTap = PrefManager.on(this).isTocallOnSingleTap();
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
        String subString;
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
                        break;

                    }
                }
                if (isMatched)
                    break;
            }
        }

        return filteredContacts;
    }

    @Override
    public void contactLoadingFinished() {
        if (mSearchView != null) {
            String filterString = mSearchView.getQuery().toString();
            onQueryTextChange(filterString);
        }
    }
}
