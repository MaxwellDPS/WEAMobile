package sv.cmu.edu.weamobile.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import sv.cmu.edu.weamobile.data.Configuration;
import sv.cmu.edu.weamobile.data.Message;
import sv.cmu.edu.weamobile.data.MessageState;
import sv.cmu.edu.weamobile.data.UserActivity;
import sv.cmu.edu.weamobile.utility.AlertHelper;
import sv.cmu.edu.weamobile.utility.Constants;
import sv.cmu.edu.weamobile.utility.Logger;
import sv.cmu.edu.weamobile.utility.WEAHttpClient;
import sv.cmu.edu.weamobile.utility.WEASharedPreferences;
import sv.cmu.edu.weamobile.utility.WEAUtil;
import sv.cmu.edu.weamobile.utility.db.MessageDataSource;
import sv.cmu.edu.weamobile.utility.db.MessageStateDataSource;

public class WEABackgroundService extends Service {
    public static final String FETCH_CONFIGURATION = "sv.cmu.edu.weamobile.service.action.FETCH_CONFIGURATION";
    public static final String SHOW_ALERT = "sv.cmu.edu.weamobile.service.action.SHOW_ALERT";

    private final IBinder mBinder = new LocalBinder();
    private BroadcastReceiver newConfigurationHandler;
    private NewActivityReceiver activityBroadcastReceiver;
    private UserActivity lastActivity;
    private Handler handler;


    public class LocalBinder extends Binder {
        WEABackgroundService getService() {
            return WEABackgroundService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        handler = new Handler();
        Log.d("WEA", "WEABackgroundService started at " + WEAUtil.getTimeStringFromEpoch(System.currentTimeMillis() / 1000) );
        Log.d("WEA", "Service onStart called with "+ intent);
        if(intent == null){
            Log.d("WEA", "Intent was null so setting it to FETCH_CONFIGURATION");
            intent = new Intent(getApplicationContext(), WEABackgroundService.class);
            intent.setAction(WEABackgroundService.FETCH_CONFIGURATION);
        }

        registerNewConfigurationReceiver();
        registerNewActivityReceiver();

        onHandleIntent(intent);


        return Service.START_NOT_STICKY;
    }

    private void registerNewConfigurationReceiver() {
        if(newConfigurationHandler == null){
            newConfigurationHandler= new NewConfigurationReceiver(handler);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(newConfigurationHandler,
                    new IntentFilter("new-config-event"));

        }
    }

    private void registerNewActivityReceiver() {
        if (activityBroadcastReceiver == null){
            activityBroadcastReceiver = new NewActivityReceiver(handler);

            Logger.log("WEA", "New configuration receiver created in main activity");
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.NEW_ACTIVITY");
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            getApplicationContext().registerReceiver(activityBroadcastReceiver, filter);
        }
    }

    protected void onHandleIntent(Intent intent) {
        Log.d("WEA", "called with "+ intent.getAction());
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_CONFIGURATION.equals(action)) {
                fetchConfiguration();
            }else if(SHOW_ALERT.equals(action)){
                int alertId= intent.getIntExtra(("alertId"),-1);
                showAlertAfterCheckingOtherConditions(alertId);
            }
        }
        AlarmBroadcastReceiver.completeWakefulIntent(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.log("WEABackgroundService onDestroy called");

        if(activityBroadcastReceiver!= null){
            getApplication().unregisterReceiver(activityBroadcastReceiver);
            activityBroadcastReceiver = null;
        }

        if(newConfigurationHandler != null){
            getApplication().unregisterReceiver(newConfigurationHandler);
            newConfigurationHandler = null;
        }
    }

    private void showAlertAfterCheckingOtherConditions(int alertId) {

        //Get info on UserActivity

//        WEAUtil.getUserActivityInfo(getApplicationContext());

        // Get info on location history

        // Get info on motion i.e. speed and direction

        //Combine the above three info to find if alert is to be shown or not

        AlertHelper.showAlertIfInTargetOrIsNotGeotargeted(getApplicationContext(), alertId);
    }

    private void fetchConfiguration() {
        Log.d("WEA", "Got request to fetch new configuration");
        //read configuration and setup up new alarm
        //if problem in getting/receiving configuration, set default alarm

        if(WEAUtil.checkIfPhoneIsRegisteredIfNotRegister(getApplicationContext())){

            //don't let all phones to ask at the same moment
            handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        WEAHttpClient.fetchAlerts(getApplicationContext());
                                    }

              }, WEAUtil.randInt(5, 1000));


            // Get info on motion i.e. speed and direction
            //Get info on UserActivity
            WEAUtil.getUserActivityInfo(getApplicationContext());
            //wait some time to get user activity, which is added to to the heartbeat
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    WEAUtil.sendHeartBeat(getApplicationContext(),
                            lastActivity);
                }
            }, WEAUtil.randInt(1000, 2000));
        }


    }

    private void addOrUpdatedMessageStatesToDatabase(Configuration configuration) {
        if(configuration!= null){
            List<Message> messages = configuration.getMessages();
            if(messages.size() >0){
                for(Message message: messages) {
                    //Only create messagestate for future/current alerts, no use of old alerts
                    if(message.isActive() || message.isOfFuture()){
//                        WEASharedPreferences.addAlertStateToPreferences(getApplicationContext(), message);

                        MessageState state = new MessageState(message.getId(), message.getScheduledFor());
                        MessageStateDataSource messageStateDataSource = new MessageStateDataSource(this);
                        messageStateDataSource.insertDataIfNotPresent(state);

                    }
                }
            }
        }
    }

    /*
        This function adds alerts to the database and if they already exist, updates them.
        Author: Harsh Alkutkar, Feb 25, 2015
     */
    private void addOrUpdatedMessagesToDatabase(Configuration configuration) {

        MessageDataSource messageDataSource = new MessageDataSource(this);
        messageDataSource.insertDataItemsIfNotPresent(configuration.getMessages());
    }




    private void setupAlarmToShowAlertAtRightTime(Configuration configuration){

        //two things to be done, shown now or schedule for later if in half an hour
        List<Message> alerts = getAlertRelevantBetweenNowAndNextScheduledCheck(configuration);
        for(Message alert : alerts){
            long currentTime = System.currentTimeMillis()/1000;
            String message = "Alert expected after: "+ (alert.getScheduledEpochInSeconds() - currentTime) + " secs";
            Logger.log(message);
            WEAUtil.showMessageIfInDebugMode(getApplicationContext(), message);
            WEAAlarmManager.setupAlarmForAlertAtScheduledTime(getApplicationContext(), alert.getId(), alert.getScheduleEpochInMillis());

            sendAlertScheduledInfoToServer(alert);
        }
    }

    private void sendAlertScheduledInfoToServer(Message alert) {
        try{
            MessageState messageState = AlertHelper.getAlertState(getApplicationContext(), alert);
            messageState.setState(MessageState.State.scheduled);

            AlertHelper.updateMessageState(messageState, getApplicationContext());

            WEAHttpClient.sendAlertState(getApplicationContext(),
                    messageState.getJson(),
                    String.valueOf(messageState.getId()));

        }
        catch (Exception ex){
            Logger.log(ex.getMessage());
        }
    }

    private List<Message> getAlertRelevantBetweenNowAndNextScheduledCheck(Configuration config){
        List<Message> relevantAlerts= new ArrayList<Message>();

        if(config != null){
            List<Message> alerts = config.getMessages();
            if(alerts.size() >0){
                for(Message alert: alerts){
                    try{
                        if(alert.isInRangeToSchedule()){
                            relevantAlerts.add(alert);
                        }
                    }catch(Exception ex){
                        Logger.log(ex.getMessage());
                    }
                }
            }
        }
        return relevantAlerts;
    }

    private class NewConfigurationReceiver extends BroadcastReceiver {
        private final Handler handler;

        public NewConfigurationReceiver(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onReceive(final Context context, Intent intent) {

            Logger.log("NewConfigurationReceiver");
            String json = intent.getStringExtra("message");
            WEANewConfigurationIntent newConfigurationIntent;

            if(json.isEmpty()){
                Logger.log("Received empty json");
                json = WEASharedPreferences.readApplicationConfiguration(context);
                newConfigurationIntent = new WEANewConfigurationIntent("", json, true);

            }else{
                WEASharedPreferences.saveApplicationConfiguration(context, json);
                newConfigurationIntent = new WEANewConfigurationIntent("Received new configuration. ", json, false);

            }

            Configuration configuration = Configuration.fromJson(json);

            //ToDo: for debugging
//            Date date = new Date();
//            date.setTime(System.currentTimeMillis() + 10000);
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//            String time = dateFormat.format(date);
//            configuration.getMessages().get(0).setScheduledFor(time);
//
//            date.setTime(System.currentTimeMillis() + 500000);
//            String timeTo = dateFormat.format(date);
//            configuration.getMessages().get(0).setEndingTime(timeTo);


            addOrUpdatedMessagesToDatabase(configuration);

            addOrUpdatedMessageStatesToDatabase(configuration); //Save the individual alerts

            setupAlarmToShowAlertAtRightTime(configuration);

            //update if new alerts
            Logger.log("Broadcast intent: About to broadcast new configuration");
            getApplicationContext().sendBroadcast(newConfigurationIntent);
        }

    }

    private class NewActivityReceiver extends BroadcastReceiver{

        private final Handler handler;

        public NewActivityReceiver(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        lastActivity = (UserActivity) intent.getSerializableExtra(Constants.ACTIVITY);
                        Logger.log("BackgroundService received new activity notification " + intent.getStringExtra(Constants.ACTIVITY_TYPE));
                    }
                });
            }
        }
    };
}
