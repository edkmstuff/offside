package com.offsidegame.offside.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.offsidegame.offside.R;
import com.offsidegame.offside.activities.ChatActivity;
import com.offsidegame.offside.events.ActiveGameEvent;
import com.offsidegame.offside.events.AvailableGamesEvent;
import com.offsidegame.offside.events.AvailableLanguagesEvent;
import com.offsidegame.offside.events.ChatMessageEvent;
import com.offsidegame.offside.events.ConnectionEvent;
import com.offsidegame.offside.events.IsAnswerAcceptedEvent;
import com.offsidegame.offside.events.JoinGameEvent;
import com.offsidegame.offside.events.ChatEvent;
import com.offsidegame.offside.events.PositionEvent;
import com.offsidegame.offside.events.PrivateGameGeneratedEvent;
import com.offsidegame.offside.events.QuestionsEvent;
import com.offsidegame.offside.events.SignalRServiceBoundEvent;
import com.offsidegame.offside.models.AvailableGame;
import com.offsidegame.offside.models.Chat;
import com.offsidegame.offside.models.ChatMessage;
import com.offsidegame.offside.models.GameInfo;
import com.offsidegame.offside.models.OffsideApplication;
import com.offsidegame.offside.models.Player;
import com.offsidegame.offside.models.Position;
import com.offsidegame.offside.models.Question;
import com.offsidegame.offside.models.Scoreboard;
import com.offsidegame.offside.events.ScoreboardEvent;
import com.offsidegame.offside.models.User;

import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.Locale;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;


/**
 * Created by KFIR on 11/15/2016.
 */

public class SignalRService extends Service {
    public HubConnection hubConnection;
    private HubProxy hub;
    //private Handler handler; // to display Toast message
    private final IBinder binder = new LocalBinder(); // Binder given to clients
    private Date startReconnecting = null;

    //public final String ip = new String("192.168.1.140:8080");
    public final String ip = new String("10.0.0.17:8080");
    //public final String ip = new String("offside.somee.com");
    //public final String ip = new String("offside.azurewebsites.net");


    public Boolean stoppedIntentionally = false;
    private int mId = -1;


    //<editor-fold desc="constructors">
    public SignalRService() {
    }

    //</editor-fold>

    //<editor-fold desc="startup">
    @Override
    public void onCreate() {
        super.onCreate();
        //handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        startSignalR(false);
        return result;
    }

    @Override
    public void onDestroy() {
        if (OffsideApplication.signalRService != null)
            return;
        hubConnection.stop();
        super.onDestroy();

    }

    private void deleteSignalRServiceReferenceFromApplication() {
        if (OffsideApplication.signalRService != null && OffsideApplication.signalRService.hubConnection != null) {
            OffsideApplication.signalRService.hubConnection.stop();
            OffsideApplication.signalRService.stoppedIntentionally = true;

        }

        OffsideApplication.signalRService = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        if (OffsideApplication.signalRService != null) {
            deleteSignalRServiceReferenceFromApplication();
        }
        startSignalR(false);
        return binder;
    }

    private void startSignalR(Boolean notifyWhenConnected) {
        try {
            Platform.loadPlatformComponent(new AndroidPlatformComponent());
            final String serverUrl = "http://" + ip;
            hubConnection = new HubConnection(serverUrl);
            final String SERVER_HUB = "OffsideHub";
            hub = hubConnection.createHubProxy(SERVER_HUB);
            ClientTransport clientTransport = new ServerSentEventsTransport(hubConnection.getLogger());

            hubConnection.stateChanged(new StateChangedCallback() {

                @Override
                public void stateChanged(ConnectionState oldState, ConnectionState newState) {
                    try {
                        if (newState == ConnectionState.Disconnected && !stoppedIntentionally) {
                            ACRA.getErrorReporter().handleSilentException(new Throwable("SignalR disconnected"));

                            EventBus.getDefault().post(new ConnectionEvent(false, "disconnected"));
                            //try reconnection for 10 min
                            if (startReconnecting == null) {
                                startReconnecting = new Date();
                            } else
                                return; // we are already trying to reconnect
                            Date now = new Date();
                            while (startReconnecting != null && now.getTime() - startReconnecting.getTime() < 10 * 60 * 1000
                                    && hubConnection.getState() == ConnectionState.Disconnected) {

                                startSignalR(true);  //sending true to identify a reconnection event
                                now = new Date();
                                Thread.sleep(5000);

                            }
                            if (hubConnection.getState() == ConnectionState.Connected) {
                                startReconnecting = null;
                                //just for putting message that we are connected
                                EventBus.getDefault().post(new ConnectionEvent(true, "connected"));
                            }
                        }
                    } catch (Exception ex) {
                        ACRA.getErrorReporter().handleSilentException(ex);
                        EventBus.getDefault().post(new ConnectionEvent(false, ex.getMessage()));
                    }

                }
            });


            SignalRFuture<Void> signalRFuture = hubConnection.start(clientTransport);

            signalRFuture.get();
//            try {
//                signalRFuture.get();
//            } catch (InterruptedException | ExecutionException e) {
//                Log.e("SimpleSignalR", e.getMessage());
//                //return;
//            }

            if (notifyWhenConnected && hubConnection.getState() == ConnectionState.Connected) {
                EventBus.getDefault().post(new SignalRServiceBoundEvent(null));
                startReconnecting = null;
            }

            if (hubConnection.getState() == ConnectionState.Connected) {
                subscribeToServer();
                EventBus.getDefault().post(new ConnectionEvent(true, "connected"));
            }
        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);
        }
    }

    //</editor-fold>

    //<editor-fold desc="subscribeToServer">
    public void subscribeToServer() {

        hub.on("AddChatMessage", new SubscriptionHandler1<ChatMessage>() {
            @Override
            public void run(ChatMessage chatMessage) {

//                if (chatMessage.getMessageType().equals(OffsideApplication.getMessageTypeAskedQuestion()))
//                    notifyOnNewQuestion();
//                if (chatMessage.getMessageType().equals(OffsideApplication.getMessageTypeClosedQuestion()))

                fireNotification(chatMessage.getMessageType(), chatMessage.getMessageText());


                EventBus.getDefault().post(new ChatMessageEvent(chatMessage));
            }


        }, ChatMessage.class);

        hub.on("UpdatePosition", new SubscriptionHandler1<Position>() {
            @Override
            public void run(Position position) {
                EventBus.getDefault().post(new PositionEvent(position));
            }
        }, Position.class);

        hub.on("UpdatePlayerData", new SubscriptionHandler1<Player>() {
            @Override
            public void run(Player player) {
                EventBus.getDefault().post(player);
            }
        }, Player.class);

        hub.on("UpdateScoreboard", new SubscriptionHandler1<Scoreboard>() {
            @Override
            public void run(Scoreboard scoreboard) {
                EventBus.getDefault().post(new ScoreboardEvent(scoreboard));
            }
        }, Scoreboard.class);



        /*
         public void getScoreboard(String gameId) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;
        hub.invoke(Scoreboard.class, "GetScoreboard", gameId).done(new Action<Scoreboard>() {

            @Override
            public void run(Scoreboard scoreboard) throws Exception {
                EventBus.getDefault().post(new ScoreboardEvent(scoreboard));
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
    }
         */
    }

    private void fireNotification(String messageType, String message) {

        boolean isAskedQuestion = messageType.equals(OffsideApplication.getMessageTypeAskedQuestion());
        boolean isCloseQuestion = messageType.equals(OffsideApplication.getMessageTypeClosedQuestion());

        if (isAskedQuestion || isCloseQuestion) {
            MediaPlayer player;

            int soundResource = R.raw.human_whisle;
            if (isCloseQuestion) {
                final Gson gson = new GsonBuilder().create();
                Question question = gson.fromJson(message, Question.class);
                if (OffsideApplication.playerAnswers.containsKey(question.getId()) && OffsideApplication.playerAnswers.get(question.getId()).getAnswerId().equals(question.getCorrectAnswerId())) {
                    soundResource = ((int) (Math.random() * 100)) % 2 == 0 ? R.raw.bravo : R.raw.hooray;
                } else {
                    soundResource = R.raw.aww;
                }
            }

            player = MediaPlayer.create(getApplicationContext(), soundResource);
            player.start();

            if(!OffsideApplication.isChatActivityVisible()){

                int titleResource = R.string.lbl_new_question_is_waiting_for_you;
                int textResource = R.string.lbl_click_to_answer;
                if (isCloseQuestion) {
                    titleResource = R.string.lbl_we_have_an_answer;
                    textResource = R.string.lbl_click_to_view;
                }


                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_offside_logo)
                        .setContentTitle(getString(titleResource))
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentText(getString(textResource))
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

// Creates an explicit intent for an Activity in your app
                Intent chatIntent = new Intent(this, ChatActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(ChatActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(chatIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                mNotificationManager.notify(mId, mBuilder.build());

            }


        }


    }

//    private void notifyOnNewQuestion() {
//
//        MediaPlayer player;
//        player = MediaPlayer.create(getApplicationContext(), R.raw.human_whisle);
//        player.start();
//
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.mipmap.ic_offside_logo)
//                .setContentTitle(getString(R.string.lbl_new_question_is_waiting_for_you))
//                .setDefaults(NotificationCompat.DEFAULT_ALL)
//                .setContentText(getString(R.string.lbl_click_to_answer))
//                .setPriority(NotificationCompat.PRIORITY_HIGH);
//
//// Creates an explicit intent for an Activity in your app
//        Intent chatIntent = new Intent(this, ChatActivity.class);
//
//// The stack builder object will contain an artificial back stack for the
//// started Activity.
//// This ensures that navigating backward from the Activity leads out of
//// your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//// Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(ChatActivity.class);
//// Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(chatIntent);
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setContentIntent(resultPendingIntent);
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//// mId allows you to update the notification later on.
//        mNotificationManager.notify(mId, mBuilder.build());
//
//    }

    //</editor-fold>

    //<editor-fold desc="methods for client activities">


    public void joinGame(String gameCode, String playerId, String playerDisplayName, String playerProfilePictureUrl, boolean isPrivateGameCreator, String androidDeviceId) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;

        hub.invoke(GameInfo.class, "JoinGame", gameCode, playerId, playerDisplayName, playerProfilePictureUrl, isPrivateGameCreator, androidDeviceId).done(new Action<GameInfo>() {

            @Override
            public void run(GameInfo gameInfo) throws Exception {
                EventBus.getDefault().post(new JoinGameEvent(gameInfo));
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
    }

    public void quitGame(String gameId, String playerId, String androidDeviceId) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;

        hub.invoke(Boolean.class, "QuitGame", gameId, playerId, androidDeviceId).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
    }

    public void isGameActive(String gameId, String gameCode) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;
        hub.invoke(Boolean.class, "IsGameActive", gameId, gameCode).done(new Action<Boolean>() {

            @Override
            public void run(Boolean isGameActive) throws Exception {
                EventBus.getDefault().post(new ActiveGameEvent(isGameActive));
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
    }


    public void getAvailableLanguages() {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;
        hub.invoke(String[].class, "GetAvailableLanguages").done(new Action<String[]>() {

            @Override
            public void run(String[] availableLanguages) throws Exception {
                EventBus.getDefault().post(new AvailableLanguagesEvent(availableLanguages));
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
    }
    public void getAvailableGames() {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;
        hub.invoke(AvailableGame[].class, "GetAvailableGames").done(new Action<AvailableGame[]>() {

            @Override
            public void run(AvailableGame[] availableGames) throws Exception {
                EventBus.getDefault().post(new AvailableGamesEvent(availableGames));
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
    }

    public void generatePrivateGame(String gameId, String groupName, String playerId, String selectedLanguage) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;

        //String languageLocale = Locale.getDefault().getDisplayLanguage();

        hub.invoke(String.class, "GeneratePrivateGame", gameId, groupName, playerId, selectedLanguage).done(new Action<String>() {

            @Override
            public void run(String privateGameCode) throws Exception {
                EventBus.getDefault().post(new PrivateGameGeneratedEvent(privateGameCode));
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
    }


    public void postAnswer(final String gameId, final String playerId, final String questionId, final String answerId, final boolean isSkipped, final int betSize) {

        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;
        hub.invoke(Boolean.class, "PostAnswer", gameId, playerId, questionId, answerId, isSkipped, betSize).done(new Action<Boolean>() {
            @Override
            public void run(Boolean isAnswerAccepted) throws Exception {
                EventBus.getDefault().post(new IsAnswerAcceptedEvent(isAnswerAccepted, gameId, playerId, questionId, answerId, isSkipped, betSize));
            }

        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });

    }

    public void getScoreboard(String gameId) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;
        hub.invoke(Scoreboard.class, "GetScoreboard", gameId).done(new Action<Scoreboard>() {

            @Override
            public void run(Scoreboard scoreboard) throws Exception {
                EventBus.getDefault().post(new ScoreboardEvent(scoreboard));
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
    }

    public void getChatMessages(String gameId, String gameCode, String playerId, String androidDeviceId) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;
        hub.invoke(Chat.class, "GetChatMessages", gameId, gameCode, playerId, androidDeviceId).done(new Action<Chat>() {

            @Override
            public void run(Chat chat) throws Exception {
                EventBus.getDefault().post(new ChatEvent(chat));
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
    }

    public void sendChatMessage(String gameId, String gameCode, String message, String playerId) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;
        hub.invoke(ChatMessage.class, "SendChatMessage", gameId, gameCode, message, playerId).done(new Action<ChatMessage>() {
            @Override
            public void run(ChatMessage chatMessage) throws Exception {
                //          EventBus.getDefault().post(new ChatMessageEvent(chatMessage));
            }

        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });

    }

    public void getQuestions(String gameId) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return;
        hub.invoke(Question[].class, "GetQuestions", gameId).done(new Action<Question[]>() {

            @Override
            public void run(Question[] questions) throws Exception {
                EventBus.getDefault().post(new QuestionsEvent(questions));
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
    }

    public boolean saveLoggedInUser(User user) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return false;

        hub.invoke(Boolean.class, "SaveLoggedInUser", user.getId(), user.getName(), user.getEmail()).done(new Action<Boolean>() {
            @Override
            public void run(Boolean isUserSaved) throws Exception {
                //EventBus.getDefault().post(new IsAnswerAcceptedEvent(isUserSaved));
            }

        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });

        return true;
    }

//    public boolean setOffsideCoins(String gameId, String playerId, int offsideCoins, boolean isDueToRewardVideo) {
//        if (!(hubConnection.getState() == ConnectionState.Connected))
//            return false;
//
//        hub.invoke(Integer.class, "SetOffsideCoins", gameId, playerId, offsideCoins, isDueToRewardVideo).done(new Action<Integer>() {
//            @Override
//            public void run(Integer newOffsideCoinsValue) throws Exception {
//                EventBus.getDefault().post(newOffsideCoinsValue);
//            }
//
//        }).onError(new ErrorCallback() {
//            @Override
//            public void onError(Throwable error) {
//                ACRA.getErrorReporter().handleSilentException(error);
//            }
//        });
//
//        return true;
//    }

    public boolean setPowerItems(String gameId, String playerId, int powerItems, boolean isDueToRewardVideo) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return false;

        hub.invoke(Integer.class, "SetPowerItems", gameId, playerId, powerItems, isDueToRewardVideo).done(new Action<Integer>() {
            @Override
            public void run(Integer newPowerItemsValue) throws Exception {
                EventBus.getDefault().post(newPowerItemsValue);
            }

        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });

        return true;
    }

    public boolean saveImageInDatabase(String playerId, String imageString) {
        if (!(hubConnection.getState() == ConnectionState.Connected))
            return false;

        hub.invoke(Boolean.class, "SaveImageInDatabase", playerId, imageString).done(new Action<Boolean>() {
            @Override
            public void run(Boolean isImageSaved) throws Exception {
                //EventBus.getDefault().post(new IsAnswerAcceptedEvent(isUserSaved));
            }

        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                ACRA.getErrorReporter().handleSilentException(error);
            }
        });
        return true;

    }

    //</editor-fold>

    //<editor-fold desc="support classes">

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SignalRService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return SignalRService.this;
        }
    }

    //</editor-fold>


}

