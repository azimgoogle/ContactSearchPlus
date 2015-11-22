package com.letbyte.callblock.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.letbyte.callblock.R;
import com.letbyte.callblock.adapter.Adapter;
import com.letbyte.callblock.application.Application;
import com.letbyte.callblock.control.Util;
import com.letbyte.callblock.core.ServiceCodeGenerator;
import com.letbyte.callblock.data.model.Block;
import com.letbyte.callblock.data.model.Model;
import com.letbyte.callblock.data.provider.ContactProvider;
import com.letbyte.callblock.data.provider.DataProvider;
import com.letbyte.callblock.data.provider.PhoneCallLogProvider;
import com.letbyte.callblock.listener.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Logic logic;
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    private final int PERMISSION_CHECK_REQUEST_CODE_CALL_LOG = 10;
    private final int PERMISSION_CHECK_REQUEST_CODE_CONTACTS = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.log("[Device-Details]::MANUFACTURER::" + Build.MANUFACTURER + "::MODEL::" + Build.MODEL + "::PRODUCT::" + Build.PRODUCT + "::BRAND::" + Build.BRAND + "::DEVICE::" + Build.DEVICE);

        if (permissionCheck(Manifest.permission.WRITE_CALL_LOG, PERMISSION_CHECK_REQUEST_CODE_CALL_LOG)) {
            new PhoneCallLogProvider(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        /*if(permissionCheck(Manifest.permission.READ_CONTACTS, PERMISSION_CHECK_REQUEST_CODE_CONTACTS)) {
            new ContactProvider(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }*/
        logic = new Logic(this);
        logic.applyLogic();
        logic.selectNavItem(R.id.nav_block);
        ServiceCodeGenerator.getInstance(MainActivity.this.getApplicationContext()).generate();

    }

    private boolean permissionCheck(String permissionFor, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(this, permissionFor);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionFor)) {
                System.out.println("Sould showreqpermratnle");
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permissionFor}, requestCode);
            }
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_CHECK_REQUEST_CODE_CALL_LOG:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new PhoneCallLogProvider(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } else {
                }
                break;
            case PERMISSION_CHECK_REQUEST_CODE_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new ContactProvider(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } else {
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
    protected void onResume() {
        super.onResume();

        Application application = (Application) getApplication();
        //application.trackMe(getClass().getName());
        int successServiceCode = ServiceCodeGenerator.getInstance(MainActivity.this.getApplicationContext()).getSuccessServiceCode();
        application.trackMeWithServiceCode(getClass().getName(), successServiceCode);

        logic.resolveAdView();
    }

    /*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        logic.selectNavItem(item.getItemId());
        logic.closeDrawer();
        return true;
    }

    private static final class Logic {
        private Activity activity;

        private Logic(Activity activity) {
            this.activity = activity;
        }

        private void applyLogic() {
            initView();
            //initData();
        }

        private void initView() {
            DataBindingUtil.setContentView(activity, R.layout.activity_main);

            ((MainActivity) activity).setSupportActionBar(getToolbar());
            ((MainActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.startActivity(new Intent(activity, AddBlockActivity.class));
                    activity.finish();
                    activity.overridePendingTransition(R.anim.animation_right_in, R.anim.animation_left_out);
                }
            });

            ActionBarDrawerToggle toggle =
                    new ActionBarDrawerToggle(
                            activity, getDrawerLayout(), getToolbar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

/*                        @Override
                        public void onDrawerOpened(View drawerView) {
                            super.onDrawerOpened(drawerView);
                        }

                        @Override
                        public void onDrawerClosed(View drawerView) {
                            super.onDrawerClosed(drawerView);

                        }

                        @Override
                        public void onDrawerSlide(View drawerView, float slideOffset) {
                            super.onDrawerSlide(drawerView, slideOffset);

                        }*/

                        @Override
                        public void onDrawerStateChanged(int newState) {
                            if (newState == DrawerLayout.STATE_DRAGGING && getDrawerLayout().isDrawerOpen(GravityCompat.START) == false) {
                                if (navFragment.actionMode != null)
                                    navFragment.actionMode.finish();
                            }
                        }
                    };


            getDrawerLayout().setDrawerListener(toggle);
            toggle.syncState();

            getNavigationView().setNavigationItemSelectedListener(((MainActivity) activity));
        }

        private void resolveAdView() {
            new Thread(new Runnable() {
                boolean isOnline;

                @Override
                public void run() {

                    isOnline = Util.isOnline();

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getAdView().setVisibility(Util.isOnline() ? View.VISIBLE : View.GONE);

                            if (getAdView().getVisibility() == View.VISIBLE) {
                                getAdView().loadAd(new AdRequest.Builder().build());
                            }
                        }
                    });
                }
            }).start();
           /* new IsInternetAvailableChecker(getAdView()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/
        }

        private AdView getAdView() {
            return (AdView) activity.findViewById(R.id.adView);
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

            int navItem = 0;

            switch (navItemId) {
                case R.id.nav_block:
                    navItem = NavFragment.navBlock;
                    activity.setTitle(activity.getString(R.string.action_block));
                    break;
            }

            Util.log("Created......................");

            navFragment = new NavFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(NavFragment.navItem, navItem);
            navFragment.setArguments(bundle);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity) activity).
                            getSupportFragmentManager().
                            beginTransaction().
                            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).
                            replace(R.id.contentFrame, navFragment).commitAllowingStateLoss();
                }
            }, NAVDRAWER_LAUNCH_DELAY);

        }

        private void syncBlocks() {
            //if (navFragment != null) navFragment.syncAdapterData();
        }


        /*fragment implementation*/
        public static final class NavFragment extends Fragment {

            private static final String navItem = "nav_item";
            private static final int navBlock = 1;

            private ActionMode actionMode;

            private ActionMode.Callback callback = new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.contextual_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.item_delete:
                            List<Integer> selectedItemPositions = getAdapter().getSelectedItems();
                            for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                                int currentPosition = selectedItemPositions.get(i);
                                Model<?> model = getAdapter().removeItem(currentPosition);

                                if (model.type.ordinal() == Model.Type.BLOCK.ordinal()) {
                                    DataProvider.onProvider(getContext()).deleteBlock((Block) model.t);
                                }
                            }
                            actionMode.finish();
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    actionMode = null;
                    getAdapter().clearSelections();
                }
            };

            private RecyclerView getRecyclerView() {
                return (RecyclerView) getActivity().findViewById(R.id.recycler);
            }

            private Adapter getAdapter() {
                return (Adapter) getRecyclerView().getAdapter();
            }

            private void syncAdapterData() {

                int nav = getArguments().getInt(navItem);

                List<Model<?>> models = new ArrayList<>();

                switch (nav) {
                    case navBlock:

                        List<Block> blocks = DataProvider.onProvider(getContext()).getBlocks();

                        for (Block block : blocks) {
                            Model<Block> model = new Model<>(block, Model.Type.BLOCK);
                            models.add(model);
                        }

                        break;
                }


                getAdapter().addItems(models);
            }

            @Override
            public void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
            }

            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                super.onCreateView(inflater, container, savedInstanceState);
                int layoutId = R.layout.content_recycler;

                View rootView = inflater.inflate(layoutId, container, false);

                return rootView;
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);

                getRecyclerView().setHasFixedSize(true);
                getRecyclerView().setLayoutManager(new LinearLayoutManager(getContext()));
                getRecyclerView().addOnItemTouchListener(new RecyclerItemClickListener(getContext(), getRecyclerView(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        Util.log("LongClick");

                        if (actionMode != null) {
                            return;
                        }
                        // Start the CAB using the ActionMode.Callback defined above
                        actionMode = ((MainActivity) getActivity()).startSupportActionMode(callback);
                        int childPosition = getRecyclerView().getChildAdapterPosition(view);
                        getAdapter().toggleSelection(childPosition);
                    }
                }));

                getRecyclerView().setAdapter(new Adapter(new ArrayList<Model>()));

                syncAdapterData();
            }

          /*  private void myToggleSelection(int idx) {
                getAdapter().toggleSelection(idx);
                String title = String.valueOf(getAdapter().getSelectedItemCount());
                actionMode.setTitle(title);
            }*/
        }

        private class IsInternetAvailableChecker extends AsyncTask<Void, Void, Boolean> {
            private View mView;

            IsInternetAvailableChecker(View view) {
                mView = view;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mView.setVisibility(View.INVISIBLE);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                return Util.isOnline();
            }

            @Override
            protected void onPostExecute(Boolean isConnected) {
                super.onPostExecute(isConnected);
                mView.setVisibility(isConnected ? View.VISIBLE : View.GONE);
            }
        }
    }
}
