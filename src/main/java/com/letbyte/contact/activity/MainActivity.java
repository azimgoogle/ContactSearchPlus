package com.letbyte.contact.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.kobakei.ratethisapp.RateThisApp;
import com.letbyte.contact.R;
import com.letbyte.contact.adapter.ContactAdapter;
import com.letbyte.contact.application.Application;
import com.letbyte.contact.control.Constant;
import com.letbyte.contact.control.PrefManager;
import com.letbyte.contact.control.Util;
import com.letbyte.contact.data.provider.DataProvider;
import com.letbyte.contact.drawable.RecyclerViewDividerItemDecorator;
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
import com.letbyte.contact.utility.ContactUtility;

public class MainActivity extends AppCompatActivity {

    private Logic logic;
    private static final int KEYBOARD_OPEN_DELAY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logic = new Logic(this);
        logic.applyLogic();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        searchView.postDelayed(new Runnable() {
            @Override
            public void run() {
                searchMenuItem.expandActionView();
            }
        }, KEYBOARD_OPEN_DELAY);
        
        logic.navFragment.mSearchView = searchView;
        searchView.setOnQueryTextListener(logic.navFragment.onQueryTextListener);
        searchView.setSuggestionsAdapter(logic.navFragment.mCursorAdapter);
        searchView.setOnSuggestionListener(logic.navFragment.onSuggestionListener);

        AutoCompleteTextView searchAutoCompleteTextView = (AutoCompleteTextView)
                searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoCompleteTextView.setThreshold(1);

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
                        logic.navFragment.loadView();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (logic.getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
            logic.getDrawerLayout().closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Monitor launch times and interval from installation
        RateThisApp.onStart(this);
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        ((Application) getApplication()).trackMe(getClass().getName());
        resolveAdView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyAdView();
    }

    private void destroyAdView() {
        getAdView().destroy();
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

                if (isOnline) {
                    Util.sleep(2);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getAdView().loadAd(new AdRequest.Builder().build());
                            getAdView().setVisibility(View.VISIBLE);
                        }
                    });


                }

               /* if (isOnline)
                    Util.sleep(2);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getAdView().setVisibility(isOnline ? View.VISIBLE : View.GONE);
                    }
                });*/
            }
        }).start();
    }


    private static final class Logic {

        private static final int DRAWER_LAUNCH_DELAY = 250;

        private final AppCompatActivity activity;

        private final NavigationView.OnNavigationItemSelectedListener navSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                selectNavItem(item.getItemId());
                closeDrawer();
                return true;
            }
        };

        private Logic(AppCompatActivity activity) {
            this.activity = activity;
        }

        private void applyLogic() {
            initView();
            selectNavItem(R.id.nav_search);
        }

        private void initView() {
            DataBindingUtil.setContentView(activity, R.layout.activity_main);

            activity.setSupportActionBar(getToolbar());
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            ActionBarDrawerToggle toggle =
                    new ActionBarDrawerToggle(
                            activity, getDrawerLayout(), getToolbar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);

            getDrawerLayout().setDrawerListener(toggle);
            toggle.syncState();

            getNavigationView().setNavigationItemSelectedListener(navSelectedListener);
        }

        private Toolbar getToolbar() {
            return (Toolbar) activity.findViewById(R.id.toolbar);
        }

        private DrawerLayout getDrawerLayout() {
            return (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        }

        private NavigationView getNavigationView() {
            return (NavigationView) activity.findViewById(R.id.nav_view);
        }

        private void closeDrawer() {
            getDrawerLayout().closeDrawer(GravityCompat.START);
        }

        private NavFragment navFragment;

        private void selectNavItem(int navItemId) {

            int navItem = NavFragment.navNone;

            switch (navItemId) {
                case R.id.nav_search:
                    navItem = NavFragment.navSearch;

                    activity.setTitle(activity.getString(R.string.nav_search));

                    if (navFragment == null) {
                        navFragment = new NavFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt(NavFragment.navItem, navItem);
                        navFragment.setArguments(bundle);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.
                                        getSupportFragmentManager().
                                        beginTransaction().
                                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).
                                        replace(R.id.frameLayout, navFragment).commitAllowingStateLoss();
                            }
                        }, DRAWER_LAUNCH_DELAY);
                    }

                    break;
                case R.id.nav_settings:

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(activity, SettingsActivity.class);
                            activity.startActivityForResult(intent, Constant.REQUESTCODE_SETTINGS);
                        }
                    }, DRAWER_LAUNCH_DELAY);


                    return;
                case R.id.nav_rate:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            RateThisApp.showRateDialog(activity);
                        }
                    }, DRAWER_LAUNCH_DELAY);

                    return;
            }


        }

        /*fragment implementation*/
        public static final class NavFragment extends Fragment {

            private static final String navItem = "navItem";
            private static final int navNone = 0;
            private static final int navSearch = 1;
            boolean isToSuggest;


            private final int[] searchIndexes = new int[]{
                    Constant.DISPLAY_NAME,
                    Constant.PHONE_NUMBER,
                    Constant.MAIL_ADDRESS,
                    Constant.ADDRESS,
                    Constant.NOTES,
                    Constant.ORGANIZATION,
                    Constant.RELATION};

            private ContactAdapter mAdapter;
            private SimpleCursorAdapter mCursorAdapter;
            private SearchView mSearchView;
            private boolean isToCallOnSingleTap;


            private final SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    mAdapter.getFilter().filter(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(newText == null || newText.length() < 1)
                        return false;
                    if(isToSuggest) {
                        populateSuggestionData(newText);
                    }
                    return onQueryTextSubmit(newText);
                }
            };

            private final SearchView.OnSuggestionListener onSuggestionListener = new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int position) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int position) {
                    Cursor cursor = mCursorAdapter.getCursor();
                    cursor.moveToPosition(position);
                    String filterString = cursor.getString(cursor.getColumnIndex(DataProvider.Entry.KEYWORD));
                    System.out.println("[Azim-select-word]::" + filterString);
                    mSearchView.setQuery(filterString, false);//false as we call submit automatically
//        onQueryTextChange(filterString);
                    return true;
                }
            };

            private boolean sendMessage(long contactID) {
                String number = new ContactUtility(getActivity().getApplicationContext()).getNumberfromContactID(contactID);
                if (number == null)
                    return false;
                Uri uri = Uri.parse("smsto:" + number);
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
//        intent.putExtra("sms_body", "The SMS text");
                startActivity(intent);
                return true;
            }

            private final ContactLoadingFinishedListener finishedListener = new ContactLoadingFinishedListener() {
                @Override
                public void contactLoadingFinished() {
                    if (mSearchView != null) {
                        String filterString = mSearchView.getQuery().toString();
                        onQueryTextListener.onQueryTextChange(filterString);
                    }
                }
            };

            String[] suggesionList = null;
            private boolean populateSuggestionData() {
                Cursor cursor = DataProvider.onProvider(getActivity().getApplicationContext()).getSuggesationHint();
                if (cursor == null)
                    return false;
                if(cursor.moveToFirst()) {
                    suggesionList = new String[cursor.getCount()];
                    final int indexKeyword = cursor.getColumnIndex(DataProvider.Entry.KEYWORD);
                    int I = 0;
                    do {
                        System.out.println("[Azim-suggestion-initial]::"+cursor.getString(indexKeyword));
                        suggesionList[I++] = cursor.getString(indexKeyword);
                    } while(cursor.moveToNext());
                }
                return true;
            }


            final int[] to = new int[] {R.id.text1};
            final String[] from = new String[]{DataProvider.Entry.KEYWORD};

            private boolean populateSuggestionData(String query) {
                if(suggesionList == null)
                    return false;

                final MatrixCursor matrixCursor = new MatrixCursor(new String[] { BaseColumns._ID, DataProvider.Entry.KEYWORD});
                int I = 0;
                query = query.toLowerCase();
                final int length = suggesionList.length;
                do
                {
                    if(suggesionList[I].contains(query))
                        matrixCursor.addRow(new Object[]{I, suggesionList[I]});
                    I++;
                } while(I < length);
                mCursorAdapter = new SimpleCursorAdapter(getActivity(),
                        R.layout.query_suggestion,
                        matrixCursor, from,
                        to,
                        CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
                mSearchView.setSuggestionsAdapter(mCursorAdapter);
                return true;
            }

            private RecyclerView getRecyclerView() {
                return (RecyclerView) getActivity().findViewById(R.id.recycler);
            }

/*            private Adapter getAdapter() {
                return (Adapter) getRecyclerView().getAdapter();
            }*/


            private final int getNavItem() {
                return getArguments().getInt(navItem, navNone);
            }

            private final int getLayoutId() {
                int navItem = getNavItem();
                switch (navItem) {
                    case navSearch:
                        return R.layout.content_recycler;
                    default:
                        return navNone;
                }
            }

            private void showContactDetailsView(long contactID) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));
                intent.setData(uri);
                startActivity(intent);
            }

            private void showContextMenu(final long contactID, View view) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(getActivity(), view);
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
                                DataProvider.onProvider(getActivity().getApplicationContext()).importDatabase();
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

            private boolean makeCall(long contactID) {
                String number = new ContactUtility(getActivity().getApplicationContext()).getNumberfromContactID(contactID);
                if (number == null)
                    return false;
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + number));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
                startActivity(intent);
                return true;
            }

            private synchronized void triggerSearchSuggestionHint() {
                if(mSearchView != null) {
                    CharSequence searchString  = mSearchView.getQuery();
                    if(searchString.length() > 0) {
                        DataProvider.onProvider(getActivity().getApplicationContext()).updateOrInsertSearchHints(searchString);
                    }
                }
            }

            private final void buildView() {
                int navItem = getNavItem();
                switch (navItem) {
                    case navSearch:
                        getRecyclerView().setHasFixedSize(true);
                        getRecyclerView().setLayoutManager(new LinearLayoutManager(getActivity()));
                        getRecyclerView().addItemDecoration(new RecyclerViewDividerItemDecorator(getActivity()));
                        mAdapter = new ContactAdapter(R.layout.contact, Constant.contactModelList);
                        getRecyclerView().setAdapter(mAdapter);
                        getRecyclerView().addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), getRecyclerView(), new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                if (ContactAdapter.IS_SCROLLING_IDLE) {
                                    final long contactID = mAdapter.getContactIDbyPosition(position);
                                    if (isToCallOnSingleTap)
                                        makeCall(contactID);
                                    else
                                        showContactDetailsView(contactID);
                                    triggerSearchSuggestionHint();
                                }
                            }

                            @Override
                            public void onItemLongClick(View view, int position) {
                                final long contactID = mAdapter.getContactIDbyPosition(position);
                                showContextMenu(contactID, view);
                                triggerSearchSuggestionHint();
                            }
                        }));
                        getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
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


                        boolean bootSynced = PrefManager.on(getActivity().getBaseContext()).isBootSynced();
                        boolean synced = PrefManager.on(getActivity().getBaseContext()).isSynced();
                        if (!bootSynced) {
                            //new SyncTask(this.getBaseContext()).execute(new HashMap<String, Boolean>());
                        } else if (!synced) {
                            //new SyncTask(this.getBaseContext()).execute(PrefManager.on(getBaseContext()).getConfig());
                        }
                        break;
                }
            }

            private ProgressBar getProgressBar() {
                return (ProgressBar) getActivity().findViewById(R.id.progressBar);
            }

            private void loadView() {

                int navItem = getNavItem();
                switch (navItem) {
                    case navSearch:

                        ProgressBar progressBar = getProgressBar();
                        progressBar.setVisibility(View.VISIBLE);
                        RecyclerView.Adapter mAdapter = getRecyclerView().getAdapter();

                        boolean isToFilterPhoneNumber = PrefManager.on(getActivity()).isFilteredByNumber();
                        boolean isPrioritizeStrequent = PrefManager.on(getActivity()).isStrequentPriority();


                        ContactClient.getInstance().setContactLoadingFinishedListener(finishedListener);
                        ContactClient.getInstance().addCommand(new ContactLoaderCommand(getActivity(), progressBar, mAdapter,
                                Constant.contactModelList, isToFilterPhoneNumber, isPrioritizeStrequent));

                        isToSuggest = PrefManager.on(getActivity()).isToSuggest();
                        if(isToSuggest) {
                            populateSuggestionData();
                        } else {
                            mSearchView.setSuggestionsAdapter(null);
                        }

                        boolean isToload = PrefManager.on(getActivity()).isNumber();
                        if (isToload)
                            ContactClient.getInstance().addCommand(new PhoneNumberLoaderCommand(getActivity(), progressBar, mAdapter, Constant.contactModelList));

                        isToload = PrefManager.on(getActivity()).isEmail();
                        if (isToload)
                            ContactClient.getInstance().addCommand(new EMailLoaderCommand(getActivity(), progressBar, mAdapter, Constant.
                                    contactModelList, isToFilterPhoneNumber));

                        isToload = PrefManager.on(getActivity()).isAddress();
                        if (isToload)
                            ContactClient.getInstance().addCommand(new AddressLoaderCommand(getActivity(), progressBar, mAdapter,
                                    Constant.contactModelList, isToFilterPhoneNumber));

                        isToload = PrefManager.on(getActivity()).isNote();
                        if (isToload)
                            ContactClient.getInstance().addCommand(new NotesLoaderCommand(getActivity(), progressBar, mAdapter, Constant.contactModelList));

                        isToload = PrefManager.on(getActivity()).isOrganization();
                        if (isToload)
                            ContactClient.getInstance().addCommand(new OrganizationLoaderCommand(getActivity(), progressBar, mAdapter, Constant.contactModelList));

                        isToload = PrefManager.on(getActivity()).isRelation();
                        if (isToload)
                            ContactClient.getInstance().addCommand(new RelationLoaderCommand(getActivity(), progressBar, mAdapter, Constant.contactModelList));

                        isToCallOnSingleTap = PrefManager.on(getActivity()).isTocallOnSingleTap();



                        break;
                    default:
                        break;
                }


            }

            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                super.onCreateView(inflater, container, savedInstanceState);
                int layoutId = getLayoutId();

                View rootView = inflater.inflate(layoutId, container, false);

                return rootView;
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);

                buildView();
                loadView();
            }
        }
    }
}
