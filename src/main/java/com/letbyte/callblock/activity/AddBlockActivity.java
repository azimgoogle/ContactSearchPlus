package com.letbyte.callblock.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.letbyte.callblock.R;
import com.letbyte.callblock.adapter.Adapter;
import com.letbyte.callblock.callback.PermissionCallback;
import com.letbyte.callblock.control.Constant;
import com.letbyte.callblock.control.Util;
import com.letbyte.callblock.data.model.Block;
import com.letbyte.callblock.data.model.Model;
import com.letbyte.callblock.data.provider.DataProvider;
import com.letbyte.callblock.databinding.ActivityAddBlockBinding;
import com.letbyte.callblock.widget.slide.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

public class AddBlockActivity extends AppCompatActivity {

    private Logic logic;
    private static final int LAUNCH_DELAY = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logic = new Logic(this);
        logic.applyLogic(savedInstanceState);

/*        new Thread(new Runnable() {

            @Override
            public void run() {
                Util.sleep(4);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logic.refresh();
                    }
                });
            }
        }).start();*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                overridePendingTransition(R.anim.animation_left_in, R.anim.animation_right_out);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(R.anim.animation_left_in, R.anim.animation_right_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constant.REQUEST_CODE_PERMISSION_READ_CALL_LOG:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    logic.refresh();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private static final class Logic {

        private Activity activity;
        private ActivityAddBlockBinding binding;

        private Logic(Activity activity) {
            this.activity = activity;
        }

        private void applyLogic(Bundle savedInstanceState) {
            initView(savedInstanceState);
            initData();
        }


        private SlidingTabFragment fragment;

        private void initView(Bundle savedInstanceState) {
            binding = DataBindingUtil.setContentView(activity, R.layout.activity_add_block);
            ((AddBlockActivity) activity).setSupportActionBar(binding.toolbar);
            ((AddBlockActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

           /* binding.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isSaved = save();

                    if (isSaved) {
                        binding.fab.setImageResource(R.drawable.ic_done_all_24dp);
                    }
                }
            });*/

            if (savedInstanceState == null) {
                FragmentTransaction transaction = ((AddBlockActivity) activity).getSupportFragmentManager().beginTransaction();
                fragment = new SlidingTabFragment();
                transaction.replace(R.id.contentFrame, fragment);
                transaction.commitAllowingStateLoss();
            }
        }

        private void refresh() {
            if (fragment != null) {
                fragment.refresh();
            }
        }

        private void initData() {

        }

        private boolean save() {

/*
            getUrlEditView().setError(null);

            String number = getUrlEditView().getText().toString().trim();
            if (TextUtils.isEmpty(number)) {
                getUrlEditView().setError("Enter Valid Number");
                return false;
            }

            number = number.replaceAll("[^0-9]+", Constant.EMPTY);

            if (TextUtils.isEmpty(number)) {
                getUrlEditView().setError("Enter Valid Number");
                return false;
            }

            Block block = new Block();
            block.setNumber(number);

            boolean success = DataProvider.onProvider(activity).iu(block);
            if (success) {
                getUrlEditView().setText(Constant.EMPTY);
            }
*/

            return true;
        }

/*        private AutoCompleteTextView getUrlEditView() {
            return (AutoCompleteTextView) activity.findViewById(R.id.urlEdit);
        }*/


        /*fragment implementation*/
        public static final class SlidingTabFragment extends Fragment {
            private SlidingTabLayout mSlidingTabLayout;
            private ViewPager mViewPager;


            private void refresh() {
                SlidingPagerAdapter adapter = (SlidingPagerAdapter) mViewPager.getAdapter();

                Util.log("Current Item >>> " + mViewPager.getCurrentItem());

                adapter.refresh(mViewPager.getChildAt(mViewPager.getCurrentItem()), mViewPager.getCurrentItem());
            }

            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState) {
                return inflater.inflate(R.layout.content_fragment_add_block, container, false);
            }

            @Override
            public void onViewCreated(View view, Bundle savedInstanceState) {

                mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
                mViewPager.setAdapter(new SlidingPagerAdapter());


                mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.slidingTabLayout);
                mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

                    @Override
                    public int getIndicatorColor(int position) {
                        return Util.getColor(getResources(), android.R.color.holo_red_dark);
                    }

                    @Override
                    public int getDividerColor(int position) {
                        return Util.getColor(getResources(), android.R.color.transparent);
                    }
                });


                mSlidingTabLayout.setViewPager(mViewPager);
            }


            private class SlidingPagerAdapter extends PagerAdapter {

                private void refresh(View view, int position) {
                    syncAdapterData(view, position);
                }

                @Override
                public int getCount() {
                    return 3;
                }

                @Override
                public boolean isViewFromObject(View view, Object o) {
                    return o == view;
                }

                @Override
                public CharSequence getPageTitle(int position) {

                    String title = null;

                    switch (position) {
                        case 0:
                            title = "Call Logs";
                            break;
                        case 1:
                            title = "Contacts";
                            break;
                        case 2:
                            title = "Add Manual";
                            break;

                    }

                    return title;
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {

                    View view = getActivity().getLayoutInflater().inflate(R.layout.content_recycler,
                            container, false);

                    container.addView(view);

                    getRecyclerView(view).setHasFixedSize(true);
                    getRecyclerView(view).setLayoutManager(new LinearLayoutManager(getContext()));
                    getRecyclerView(view).setAdapter(new Adapter(new ArrayList<Model>()));

                    syncAdapterData(view, position);

                    return view;
                }

                private RecyclerView getRecyclerView(View view) {
                    return (RecyclerView) view.findViewById(R.id.recycler);
                }

                private void syncAdapterData(final View view, final int position) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final List<Model<?>> models = new ArrayList<>();

                            List<Block> blocks = null;

                            switch (position) {
                                case 0:
                                    blocks = DataProvider.onProvider(getActivity()).getCallLogs(getActivity(), Constant.REQUEST_CODE_PERMISSION_READ_CALL_LOG);
                                    break;
                                case 1:
                                    blocks = DataProvider.onProvider(getActivity()).getContacts();
                                    break;
                                case 2:
                                    break;
                            }


                            switch (position) {
                                case 0:
                                case 1:
                                    for (Block block : blocks) {
                                        Model<Block> model = new Model<>(block, Model.Type.BLOCK);
                                        models.add(model);
                                    }
                                    break;
                                case 2:
                                    break;
                            }

                            view.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getAdapter(view).addItems(models);
                                }
                            }, LAUNCH_DELAY);
                        }
                    }).start();

                }

                private Adapter getAdapter(View view) {
                    return (Adapter) getRecyclerView(view).getAdapter();
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView((View) object);
                }
            }
        }


    }//Ending logic section

}
