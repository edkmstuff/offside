package com.offsidegame.offside.fragments;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.offsidegame.offside.R;

import com.offsidegame.offside.activities.LoginActivity;
import com.offsidegame.offside.events.JoinGameWithCodeEvent;
import com.offsidegame.offside.events.NavigationEvent;
import com.offsidegame.offside.models.OffsideApplication;

import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;


public class SettingsFragment extends Fragment {

    private LinearLayout signOutRoot;
    private LinearLayout backRoot;
    private LinearLayout joinGameWithCodeRoot;
    private LinearLayout privacyPolicyRoot;





    public static SettingsFragment newInstance() {
        SettingsFragment settingsFragment = new SettingsFragment();
        return settingsFragment;
    }

    public SettingsFragment(){}

    @Override
    public void onResume(){
        try
        {

            super.onResume();
        } catch (Exception ex) {
                    ACRA.getErrorReporter().handleSilentException(ex);
                    return ;
        }


    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try
        {
            // Inflate the layout for this fragment
            View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

            joinGameWithCodeRoot = rootView.findViewById(R.id.sf_join_game_with_code_root);
            signOutRoot = rootView.findViewById(R.id.sf_sign_out_root);
            backRoot = rootView.findViewById(R.id.sf_back_root);
            privacyPolicyRoot = rootView.findViewById(R.id.sf_privacy_policy_root);
            final Context context = getContext();

            joinGameWithCodeRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new JoinGameWithCodeEvent(1));

                }
            });


            signOutRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AuthUI.getInstance()
                            .signOut((FragmentActivity)context)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    // user is now signed out
                                    OffsideApplication.cleanUserPreferences();
                                    OffsideApplication.networkingService.stopListening();

                                    //set up required to exit in back button pressed
                                    Intent intent = new Intent(context, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(intent);

                                }
                            });
                }
            });

            backRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    backToGroups();

                }
            });

            privacyPolicyRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // opening a URL in a Browser in Android:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.sidekickgame.com/privacy_policy.html"));
                            startActivity(browserIntent);
                }
            });

            return rootView;


        } catch (Exception ex) {
                    ACRA.getErrorReporter().handleSilentException(ex);
                    return null;
        }

    }

    public void backToGroups(){

        EventBus.getDefault().post(new NavigationEvent(R.id.nav_action_groups));
    }

}
