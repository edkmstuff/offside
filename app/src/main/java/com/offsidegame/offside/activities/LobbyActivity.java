package com.offsidegame.offside.activities;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.offsidegame.offside.R;
import com.offsidegame.offside.events.ConnectionEvent;
import com.offsidegame.offside.events.FriendInviteReceivedEvent;
import com.offsidegame.offside.events.GroupInviteEvent;
import com.offsidegame.offside.events.JoinGameEvent;
import com.offsidegame.offside.events.NavigationEvent;
import com.offsidegame.offside.events.PrivateGroupEvent;
import com.offsidegame.offside.events.SignalRServiceBoundEvent;
import com.offsidegame.offside.fragments.ChatFragment;
import com.offsidegame.offside.fragments.GroupsFragment;
import com.offsidegame.offside.fragments.PlayerFragment;
import com.offsidegame.offside.fragments.SingleGroupFragment;
import com.offsidegame.offside.helpers.ImageHelper;
import com.offsidegame.offside.models.AvailableGame;
import com.offsidegame.offside.models.GameInfo;
import com.offsidegame.offside.models.OffsideApplication;
import com.offsidegame.offside.models.PlayerAssets;
import com.offsidegame.offside.models.PrivateGroup;

import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;


public class LobbyActivity extends AppCompatActivity implements Serializable {

    //<editor-fold desc="*****************MEMBERS****************">

    //activity
    private final String activityName = "LobbyActivity";
    private final Context context = this;
    private final Activity thisActivity = this;

    private BottomNavigationView bottomNavigationView;
    private String groupId;

    private TabLayout leaguesSelectionTabLayout;
    private ViewPager leaguesPagesViewPager;
    private ImageView settingsButtonImageView;

    // single group view
    private LinearLayout singlePrivateGroupRoot;

    //playerAssets
    private LinearLayout playerInfoRoot;
    private TextView balanceTextView;
    private TextView powerItemsTextView;

    //profile
    private ImageView playerPictureImageView;
    private String playerId;
    private String playerProfilePictureUrl;
    private SharedPreferences settings;

    //using fragments
    private LinearLayout fragmentContainerRoot;
    private GroupsFragment groupsFragment;
    private PlayerFragment playerFragment;
    private SingleGroupFragment singleGroupFragment;
    private ChatFragment chatFragment;


    //</editor-fold>




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_lobby);

            settings = getSharedPreferences(getString(R.string.preference_name), 0);

            FirebaseUser player = FirebaseAuth.getInstance().getCurrentUser();

            playerId = player.getUid();

            getIds();
            setEvents();
            resetVisibility();

        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);
        }


    }

    private void getIds() {


        playerInfoRoot = (LinearLayout) findViewById(R.id.l_player_info_root);
        fragmentContainerRoot = (LinearLayout) findViewById(R.id.l_fragment_container_root);
        //groupsFragment = (GroupsFragment) this.getSupportFragmentManager().findFragmentById(R.id.l_groups_fragment);

        settingsButtonImageView = (ImageView) findViewById(R.id.l_settings_button_image_view);

        playerPictureImageView = (ImageView) findViewById(R.id.l_player_picture_image_view);
        balanceTextView = (TextView) findViewById(R.id.l_balance_text_view);
        powerItemsTextView = (TextView) findViewById(R.id.l_power_items_text_view);

        //createPrivateGroupButtonTextView = (TextView) findViewById(R.id.l_create_private_group_button_text_view);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.l_bottom_navigation_view);

        //single view
        singlePrivateGroupRoot = (LinearLayout) findViewById(R.id.l_single_group_root);

        //leagues
        leaguesPagesViewPager = (ViewPager) findViewById(R.id.l_leagues_pages_view_pager);
        //setup tabLayout
        leaguesSelectionTabLayout = (TabLayout) findViewById(R.id.l_leagues_selection_tab_layout);
        leaguesSelectionTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        leaguesSelectionTabLayout.setupWithViewPager(leaguesPagesViewPager);

    }

    private void setEvents() {

        settingsButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance()
                        .signOut((FragmentActivity) context)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(context, LoginActivity.class));
                                finish();
                            }
                        });
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                try {
                    switch (item.getItemId()) {
                        case R.id.nav_action_groups:

                            groupsFragment = GroupsFragment.newInstance();
                            replaceFragment(groupsFragment);


                            return true;

                        case R.id.nav_action_profile:
                            playerFragment = new PlayerFragment();
                            EventBus.getDefault().register(playerFragment);
                            replaceFragment(playerFragment);
                            OffsideApplication.signalRService.requestUserProfileData(playerId);
                            return true;

                        case R.id.nav_action_shop:
                            return true;

                        case R.id.nav_action_play:
                            chatFragment = ChatFragment.newInstance();
                            replaceFragment(chatFragment);

                            return true;
                    }

                    return true;

                } catch (Exception ex) {
                    ACRA.getErrorReporter().handleSilentException(ex);
                    return false;
                }

            }
        });


    }

    private void replaceFragment(Fragment fragment) {

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.l_fragment_container_root, fragment, fragment.getTag()).commit();

    }

    private void resetVisibility() {

        playerInfoRoot.setVisibility(View.GONE);
        singlePrivateGroupRoot.setVisibility(View.GONE);
        fragmentContainerRoot.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {

        super.onResume();

        EventBus.getDefault().post(new SignalRServiceBoundEvent(context));
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(context);

    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(context);

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSignalRServiceBinding(SignalRServiceBoundEvent signalRServiceBoundEvent) {
        try {
            if (OffsideApplication.signalRService == null)
                return;

            Context eventContext = signalRServiceBoundEvent.getContext();
            if (eventContext == context) {

                if (OffsideApplication.isBoundToSignalRService) {
                    PlayerAssets playerAssets = OffsideApplication.getPlayerAssets();
                    if (playerAssets == null)
                        OffsideApplication.signalRService.requestPlayerAssets(playerId);
                    else
                        onReceivePlayerAssets(playerAssets);

                } else
                    throw new RuntimeException(activityName + " - onSignalRServiceBinding - Error: SignalRIsNotBound");

//                String[] emptyAvailableGames = new String[]{getString(R.string.lbl_no_available_games)};
///;                setAvailableGamesSpinnerAdapter(emptyAvailableGames);

            }

        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceivePlayerAssets(PlayerAssets playerAssets) {

        try {
            if (playerAssets == null)
                return;

            OffsideApplication.setPlayerAssets(playerAssets);

            //update player stuff
            int balance = playerAssets.getBalance();
            int powerItems = playerAssets.getPowerItems();
            playerProfilePictureUrl = playerAssets.getImageUrl();
            balanceTextView.setText(Integer.toString(balance));
            powerItemsTextView.setText(Integer.toString(powerItems));

            ImageHelper.loadImage(thisActivity, playerProfilePictureUrl, playerPictureImageView, activityName);

            playerInfoRoot.setVisibility(View.VISIBLE);

            tryRejoinGameForReturningPlayer();




        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);

        }

    }

    public void tryRejoinGameForReturningPlayer(){

        //check if player is already playing
        SharedPreferences settings = context.getSharedPreferences(getString(R.string.preference_name), 0);
        String lastKnownGameId = settings.getString(getString(R.string.game_id_key), null);
        String lastKnownPrivateGroupId = settings.getString(getString(R.string.private_group_id_key), null);
        String lastKnownPrivateGameId = settings.getString(getString(R.string.private_game_id_key), null);
        String playerId = OffsideApplication.getPlayerId();

        OffsideApplication.setSelectedPrivateGameId(lastKnownPrivateGameId);

        OffsideApplication.signalRService.requestAvailableGame(lastKnownGameId, lastKnownPrivateGameId, playerId);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveAvailableGame(AvailableGame availableGame) {
        try {

            if (availableGame != null && availableGame.getGameId() != null) {
                String groupId = availableGame.getGroupId();
                OffsideApplication.signalRService.requestPrivateGroup(playerId,groupId);
                OffsideApplication.setSelectedAvailableGame(availableGame);
            }

            else {
                EventBus.getDefault().post(new NavigationEvent(R.id.nav_action_groups));

            }


        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceivePrivateGroup(PrivateGroupEvent privateGroupEvent) {
        try {
            if (privateGroupEvent == null)
                return;

            PrivateGroup privateGroup = privateGroupEvent.getPrivateGroup();
            OffsideApplication.setSelectedPrivateGroup(privateGroup);
            EventBus.getDefault().post(new NavigationEvent(R.id.nav_action_play));

        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);

        }

    }





    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSelectedPrivateGroup(PrivateGroup privateGroup) {
        try {
            if (privateGroup == null || privateGroup.getId() == null)
                return;

            singleGroupFragment = SingleGroupFragment.newInstance();
            replaceFragment(singleGroupFragment);

        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);

        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveNavigation(NavigationEvent navigationEvent) {
        try {
            bottomNavigationView.setSelectedItemId(navigationEvent.getNavigationItemId());

        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionEvent(ConnectionEvent connectionEvent) {
        try {
            boolean isConnected = connectionEvent.getConnected();
            if (isConnected)
                Toast.makeText(context, R.string.lbl_you_are_connected, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, R.string.lbl_you_are_disconnected, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerJoinedPrivateGame(JoinGameEvent joinGameEvent) {
        GameInfo gameInfo = joinGameEvent.getGameInfo();
        if (gameInfo != null) {
            return;
        }

        onReceiveNavigation(new NavigationEvent(R.id.nav_action_play));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupInvite(GroupInviteEvent groupInviteEvent){

        String groupId= groupInviteEvent.getGroupId();
        String gameId= groupInviteEvent.getGameId();
        String privateGameId= groupInviteEvent.getPrivateGamaId();
        String playerId = groupInviteEvent.getInviterPlayerId();

        OffsideApplication.signalRService.requestInviteFriend(groupId, gameId, privateGameId, playerId);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendInviteReceived(FriendInviteReceivedEvent friendInviteReceivedEvent){

        String invitationUrl= friendInviteReceivedEvent.getInvitationUrl();
        String inviterName = OffsideApplication.getPlayerAssets().getPlayerName();

        String shareMessage = String.format("%s invited you to join a group in SideKick. Click to join %s",inviterName, invitationUrl);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);


        startActivity(Intent.createChooser(sendIntent,"Invite friendS" ));


    }





    private Boolean exit = false;

    @Override
    public void onBackPressed() {

        resetVisibility();
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, R.string.lbl_press_back_again_to_exit,
                    Toast.LENGTH_LONG).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }


}
