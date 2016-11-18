package com.offsidegame.offside;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.offsidegame.offside.helpers.SignalRService;
import com.offsidegame.offside.models.PlayerScore;
import com.offsidegame.offside.models.PlayerScoreEvent;
import com.offsidegame.offside.models.SignalRServiceBoundEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerScoreActivity extends AppCompatActivity {


    private final Context context = this;
    private SignalRService signalRService;
    private boolean boundToSignalRService = false;

    private final ServiceConnection signalRServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to SignalRService, cast the IBinder and get SignalRService instance
            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
            signalRService = binder.getService();
            boundToSignalRService = true;
            EventBus.getDefault().post(new SignalRServiceBoundEvent(context));
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            boundToSignalRService = false;
        }
    };


    TextView score;
    TextView position;
    TextView leaderScore;
    TextView totalOpenQuestions;


    /**
     * Defines callbacks for service binding, passed to bindService()
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_score);
        score = (TextView) findViewById(R.id.score);
        position = (TextView) findViewById(R.id.position);
        leaderScore = (TextView) findViewById(R.id.leader_score);
        totalOpenQuestions = (TextView) findViewById(R.id.total_active_questions);

//

    }

    @Override
    public void onResume() {
        super.onResume();
//        setContentView(R.layout.activity_player_score);
//        score = (TextView) findViewById(R.id.score);
//        position = (TextView) findViewById(R.id.position);
//        leaderScore = (TextView) findViewById(R.id.leader_score);
//        totalOpenQuestions = (TextView) findViewById(R.id.total_active_questions);
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(getString(R.string.preference_name), 0);
        boolean isLoggedIn = settings.getBoolean(getString(R.string.is_logged_in_key), false);

        String loginExpirationTimeAsString = (String) settings.getString(getString(R.string.login_expiration_time), "");
        SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.date_format));

        Date loginExpirationTime;
        try {
            loginExpirationTime = formatter.parse(loginExpirationTimeAsString);
        }catch (ParseException pe){
            Log.e(getString(R.string.log_tag), pe.getMessage());
            loginExpirationTime = new Date();
        }

        Date current = new Date();
        if (!isLoggedIn || current.after(loginExpirationTime) ) {
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            return;
        }

        Intent intent = new Intent();
        intent.setClass(context, SignalRService.class);
        bindService(intent, signalRServiceConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(context);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(context);
        // Unbind from the service
        if (boundToSignalRService) {
            unbindService(signalRServiceConnection);
            boundToSignalRService = false;
        }

        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSignalRServiceBinding(SignalRServiceBoundEvent signalRServiceBoundEvent) {
        Context eventContext = signalRServiceBoundEvent.getContext();
        if (eventContext == context)
            signalRService.getPlayerScore();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceivePlayerScore(PlayerScoreEvent playerScoreEvent) {
        PlayerScore playerScore = playerScoreEvent.getPlayerScore();
        updatePlayerScoreInUi(playerScore);
        Toast.makeText(context, getString(R.string.data_updated), Toast.LENGTH_SHORT).show();
    }

    void updatePlayerScoreInUi(PlayerScore playerScore) {
//            boolean isOnMainThread = Looper.myLooper() == Looper.getMainLooper();
//        if (!isOnMainThread)
//            return;
        score.setText(playerScore.getScore().toString());
//        score.invalidate();
        //player position
        position.setText(playerScore.getPosition().toString() + " " + getString(R.string.out_of) + " " + playerScore.getTotalPlayers().toString());
        //leader score
        leaderScore.setText(playerScore.getLeaderScore().toString());
        //open questions
        totalOpenQuestions.setText(playerScore.getTotalOpenQuestions().toString());





    }


}
