package com.offsidegame.offside.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.offsidegame.offside.R;
import com.offsidegame.offside.adapters.ViewPagerAdapter;
import com.offsidegame.offside.models.OffsideApplication;
import com.offsidegame.offside.models.PrivateGroup;
import com.offsidegame.offside.models.PrivateGroupsInfo;

import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by user on 8/22/2017.
 */

public class GroupsFragment extends Fragment {
    private LinearLayout groupsRoot;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private String playerId;
    private PrivateGroup selectedPrivateGroup;


    public static GroupsFragment newInstance() {
        GroupsFragment groupsFragment = new GroupsFragment();

        groupsFragment.selectedPrivateGroup = OffsideApplication.getSelectedPrivateGroup();

        return groupsFragment;
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            EventBus.getDefault().register(this);
            playerId = OffsideApplication.getPlayerId();
            OffsideApplication.networkingService.requestPrivateGroupsInfo(playerId);

        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);

        }

    }

    @Override
    public void onPause() {
        try {
            super.onPause();
            EventBus.getDefault().unregister(this);
        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);

        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_groups, container, false);
            getIDs(view);
            setEvents();

            return view;


        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);
            return null;
        }

    }

    private void getIDs(View view) {

        groupsRoot = view.findViewById(R.id.fg_groups_root);

        viewPager = view.findViewById(R.id.fg_tabs_container_view_pager);
        tabLayout = view.findViewById(R.id.fg_groups_tab_layout);

        viewPagerAdapter = new ViewPagerAdapter(getFragmentManager(), getActivity(), viewPager, tabLayout);
        viewPager.setAdapter(viewPagerAdapter);

    }

    private void setEvents() {

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                viewPager.setCurrentItem(tab.getPosition());
                int selectedTabPosition;
                selectedTabPosition = viewPager.getCurrentItem();
                OffsideApplication.selectedGroupTabPosition = selectedTabPosition;
                //viewPagerAdapter.refreshTabLayout(selectedTabPosition);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);


            }
        });
    }


    public void addPage(String groupType) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.key_group_type), groupType);
            PrivateGroupsFragment privateGroupsFragment = new PrivateGroupsFragment();
            privateGroupsFragment.setArguments(bundle);
            viewPagerAdapter.addFragment(privateGroupsFragment, getPageTitle(groupType));
            viewPagerAdapter.notifyDataSetChanged();
            if (viewPagerAdapter.getCount() > 0)
                tabLayout.setupWithViewPager(viewPager);


            setupTabLayout();


        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);

        }

    }

    private String getPageTitle(String groupType) {
        String title = "Unknown";
        if (groupType.equals(getString(R.string.key_private_group_name)))
            title = getString(R.string.lbl_my_private_groups);
        else if (groupType.equals(getString(R.string.key_public_group_name)))
            title = getString(R.string.lbl_public_groups);
        return title;

    }

    public void setupTabLayout() {
        try {
           // selectedTabPosition = viewPager.getCurrentItem();
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                tabLayout.getTabAt(i).setCustomView(viewPagerAdapter.getTabView(i));
            }


        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);

        }

    }

    public boolean isTabsCreated() {
        return viewPagerAdapter != null && viewPagerAdapter.getCount() > 0;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceivePrivateGroupsInfo(PrivateGroupsInfo privateGroupsInfo) {
        try {

            if (privateGroupsInfo != null && privateGroupsInfo.getPrivateGroups() != null) {
                OffsideApplication.setPrivateGroupsInfo(privateGroupsInfo);

                addPagesToGroupsFragment();


                viewPager.setCurrentItem(OffsideApplication.selectedGroupTabPosition);


                groupsRoot.setVisibility(View.VISIBLE);

            }

        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);

        }

    }

    public void addPagesToGroupsFragment() {

        try {
            if (!isTabsCreated()) {
                addPage(getString(R.string.key_private_group_name));
                addPage(getString(R.string.key_public_group_name));
            }


        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);

        }

    }


}
