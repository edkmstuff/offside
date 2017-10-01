package com.offsidegame.offside.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.offsidegame.offside.R;
import com.offsidegame.offside.events.ConnectionEvent;
import com.offsidegame.offside.events.PrivateGroupCreatedEvent;
import com.offsidegame.offside.events.SignalRServiceBoundEvent;
import com.offsidegame.offside.models.OffsideApplication;

import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class CreatePrivateGroupActivity extends AppCompatActivity {


    //activity
    private final String activityName = "LobbyActivity";
    private final Context context = this;
    private FrameLayout loadingRoot;

    //create private group form

    private LinearLayout createPrivateGroupRoot;
    private EditText privateGroupNameEditText;
    private TextView savePrivateGroupButtonTextView;


    private String playerDisplayName;
    private String playerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_private_group);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        playerId = firebaseUser.getUid();

        if(OffsideApplication.getUserProfileInfo() == null)

            playerDisplayName = firebaseUser.getDisplayName();

        else
            playerDisplayName = OffsideApplication.getUserProfileInfo().getPlayerName();


        getIds();
        setEvents();

        privateGroupNameEditText.setText(playerDisplayName.split(" ")[0] + "'s" + " friends");


    }

    private void getIds(){
        savePrivateGroupButtonTextView =  findViewById(R.id.cpg_save_private_group_button_text_view);
        loadingRoot =  findViewById(R.id.shared_loading_root);
        createPrivateGroupRoot =  findViewById(R.id.cpg_create_private_group_root);


        privateGroupNameEditText =  findViewById(R.id.cpg_private_group_name_edit_text);


    }

    private void setEvents(){

        savePrivateGroupButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get language
                String selectedLanguage = "Hebrew";

                String groupName = privateGroupNameEditText.getText().toString();
                groupName = groupName.length() > 20 ? groupName.substring(0, 20) : groupName;

                String groupType= getResources().getString(R.string.key_private_group_name);

                if (OffsideApplication.isBoundToSignalRService)
                    OffsideApplication.signalRService.requestCreatePrivateGroup(groupName, groupType, playerId, selectedLanguage);
                else
                    throw new RuntimeException(activityName + " - generatePrivateGameCodeButtonTextView - onClick - Error: SignalRIsNotBound");

                createPrivateGroupRoot.setVisibility(View.GONE);
                createPrivateGroupRoot.setVisibility(View.VISIBLE);
            }
        });

//        Field1.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count) {
//                if(s.length() != 0)
//                    Field2.setText("");
//            }
//        });

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
        EventBus.getDefault().unregister(context);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPrivateGroupCreated(PrivateGroupCreatedEvent privateGroupCreatedEvent) {
        try {

            //PrivateGroup privateGroup = privateGroupCreatedEvent.getPrivateGroup();
            //EventBus.getDefault().post(new NavigationEvent(R.id.nav_action_groups));
            Intent intent = new Intent(context,LobbyActivity.class);
            intent.putExtra("showGroups",true);
            startActivity(intent);

        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSignalRServiceBinding(SignalRServiceBoundEvent signalRServiceBoundEvent) {
        try {
            if (OffsideApplication.signalRService == null)
                return;

            Context eventContext = signalRServiceBoundEvent.getContext();
            if (eventContext == context || eventContext == getApplicationContext()) {

                if (OffsideApplication.isPlayerQuitGame()) {
                    loadingRoot.setVisibility(View.GONE);

                    return;
                }


                if (OffsideApplication.isBoundToSignalRService) {
                    loadingRoot.setVisibility(View.GONE);

                } else
                    throw new RuntimeException(activityName + " - onSignalRServiceBinding - Error: SignalRIsNotBound");


            }

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
}
