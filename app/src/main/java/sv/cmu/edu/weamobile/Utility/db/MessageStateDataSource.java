package sv.cmu.edu.weamobile.utility.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import sv.cmu.edu.weamobile.data.GeoLocation;
import sv.cmu.edu.weamobile.data.MessageState;
import sv.cmu.edu.weamobile.utility.Logger;

/**
 * Created by sumeet on 4/6/15.
 */
public class MessageStateDataSource extends WEADataSource<MessageState> {
//    private boolean isAlreadyShown = false;
//    private boolean isFeedbackGiven = false;
//    private long timeWhenShownToUserInEpoch;
//    private long timeWhenFeedbackGivenInEpoch;
//    private String latWhenShown;
//    private String lngWhenShown;
//    private float accuracyWhenShown = 0.00f;
//    private boolean isInPolygonOrAlertNotGeoTargeted = false;

    public static final String MESSAGE_STATE_TABLE = "MessageState";

    public static final String COLUMN_ROW_ID = "_id";
    public static final String COLUMN_SCHEDULED_FOR = "scheduledFor";
    public static final String COLUMN_MESSAGE_ID = "messageId";
    public static final String COLUMN_IS_ALREADY_SHOWN = "isAlreadyShown";
    public static final String COLUMN_IS_FEEDBACK_GIVEN = "isFeedbackGiven";
    public static final String COLUMN_TIME_WHEN_SHOWN_TO_USER_IN_EPOCH = "timeWhenShownToUserInEpoch";
    public static final String COLUMN_TIME_WHEN_FEEDBACK_GIVEN_IN_EPOCH = "timeWhenFeedbackGivenInEpoch";
    public static final String COLUMN_LAT_WHEN_SHOWN = "latWhenShown";
    public static final String COLUMN_LNG_WHEN_SHOWN = "lngWhenShown";
    public static final String COLUMN_ACCURACY_WHEN_SHOWN = "accuracyWhenShown";
    public static final String COLUMN_IS_IN_POLYGON_OR_MESSAGE_NOT_GEO_TARGETED = "isInPolygonOrAlertNotGeoTargeted";

    public final static String CREATE_MESSAGE_TABLE_SQL =    "create table " + MESSAGE_STATE_TABLE + " ( " +
            COLUMN_ROW_ID + " integer primary key autoincrement , " +
            COLUMN_MESSAGE_ID + " integer ," +
            COLUMN_SCHEDULED_FOR + " text ," +
            COLUMN_IS_ALREADY_SHOWN + " integer ," +
            COLUMN_IS_FEEDBACK_GIVEN + " integer ," +
            COLUMN_TIME_WHEN_SHOWN_TO_USER_IN_EPOCH + " text ," +
            COLUMN_TIME_WHEN_FEEDBACK_GIVEN_IN_EPOCH + " text ," +
            COLUMN_LAT_WHEN_SHOWN + " text ," +
            COLUMN_LNG_WHEN_SHOWN + " text ," +
            COLUMN_ACCURACY_WHEN_SHOWN + " text ," +
            COLUMN_IS_IN_POLYGON_OR_MESSAGE_NOT_GEO_TARGETED + " integer ," +
            "Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP "+
            " ) ";

    public MessageStateDataSource(Context context) {
        super(context);
    }

    public MessageStateDataSource(Context context, WEASQLiteHelper helper) {
        super(context, helper);
    }

    @Override
    public void insertData(MessageState messageState) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(COLUMN_MESSAGE_ID, messageState.getId());
        insertValues.put(COLUMN_SCHEDULED_FOR, messageState.getScheduledFor());
        insertValues.put(COLUMN_IS_ALREADY_SHOWN, messageState.isAlreadyShown());
        insertValues.put(COLUMN_IS_FEEDBACK_GIVEN, messageState.isFeedbackGiven());
        insertValues.put(COLUMN_TIME_WHEN_SHOWN_TO_USER_IN_EPOCH, messageState.getTimeWhenShownToUserInEpoch());
        insertValues.put(COLUMN_TIME_WHEN_FEEDBACK_GIVEN_IN_EPOCH, messageState.getTimeWhenFeedbackGivenInEpoch());

        if(messageState.getLocationWhenShown()!= null){
            insertValues.put(COLUMN_LAT_WHEN_SHOWN, messageState.getLocationWhenShown().getLatitude());
            insertValues.put(COLUMN_LNG_WHEN_SHOWN, messageState.getLocationWhenShown().getLongitude());
            insertValues.put(COLUMN_ACCURACY_WHEN_SHOWN, messageState.getLocationWhenShown().getAccuracy());
        }

        insertValues.put(COLUMN_IS_IN_POLYGON_OR_MESSAGE_NOT_GEO_TARGETED, messageState.isInPolygonOrAlertNotGeoTargeted());
        Long rows = insert(insertValues);

        Logger.log("Inserted Alert State " + (messageState.getUniqueId()) );
        Logger.log("No fo rows updated " + rows);
    }

    @Override
    public void insertDataIfNotPresent(MessageState data) {
        boolean found = false;
        List<MessageState> messageStates = getAllData();
        for(MessageState state : messageStates){
            if(state.getUniqueId().compareTo(data.getUniqueId()) == 0){
                found = true;
                break;
            }
        }

        if(!found){
            insertData(data);
        }
    }

    @Override
    public void insertDataItemsIfNotPresent(List<MessageState> data) {

    }

    @Override
    public MessageState getData(int id) {
        MessageState messageState = null;
        try{
            open();
            Cursor cursor = database.rawQuery("select * from "+ MESSAGE_STATE_TABLE +" where " + COLUMN_MESSAGE_ID + "=" +id, null);
            if (cursor .moveToFirst()) {

                while (cursor.isAfterLast() == false) {
                    int messageId = Integer.valueOf(cursor.getString(1));
                    String scheduledFor = cursor.getString(2);

                    MessageState state = new MessageState(messageId, scheduledFor);
                    state.setAlreadyShown(cursor.getInt(3)>0?true:false);
                    state.setFeedbackGiven(cursor.getInt(4) > 0 ? true : false);
                    state.setTimeWhenFeedbackGivenInEpoch(Long.parseLong(cursor.getString(5)));
                    state.setTimeWhenFeedbackGivenInEpoch(Long.parseLong(cursor.getString(6)));

                    state.setLocationWhenShown(new GeoLocation(cursor.getString(7), cursor.getString(7),cursor.getFloat(9) ));

                    state.setInPolygonOrAlertNotGeoTargeted(cursor.getInt(10)>0?true:false);

                    messageState = state;
                    cursor.moveToNext();
                }
            }
        }catch (Exception ex){
            Logger.log(ex.getLocalizedMessage());
        }finally {
            close();
        }

        Logger.log("Retrieved message sates with id " + messageState.getUniqueId());
        return  messageState;
    }

    @Override
    public void updateData(MessageState data) {
        try{
           open();
           database.rawQuery("delete from "+ MESSAGE_STATE_TABLE +" where " + COLUMN_MESSAGE_ID + "=" + data.getId(), null);
           insertDataIfNotPresent(data);

        }catch (Exception ex){
            Logger.log(ex.getLocalizedMessage());
        }finally {
            close();
        }

        Logger.log("Updated message sates with id " + data.getUniqueId());
    }

    @Override
    public void deleteData(MessageState data) {

    }

    @Override
    public List<MessageState> getAllData() {
        List<MessageState> messageStates = new ArrayList<MessageState>();
        try{
            open();
            Cursor cursor = database.rawQuery("select * from "+ MESSAGE_STATE_TABLE, null);
            if (cursor .moveToFirst()) {
                while (cursor.isAfterLast() == false) {
                    int id = Integer.valueOf(cursor.getString(1));
                    String scheduledFor = cursor.getString(2);

                    MessageState state = new MessageState(id, scheduledFor);
                    state.setAlreadyShown(cursor.getInt(3)>0?true:false);
                    state.setFeedbackGiven(cursor.getInt(4) > 0 ? true : false);
                    state.setTimeWhenFeedbackGivenInEpoch(Long.parseLong(cursor.getString(5)));
                    state.setTimeWhenFeedbackGivenInEpoch(Long.parseLong(cursor.getString(6)));

                    state.setLocationWhenShown(new GeoLocation(cursor.getString(7), cursor.getString(7),cursor.getFloat(9) ));

                    state.setInPolygonOrAlertNotGeoTargeted(cursor.getInt(10)>0?true:false);

                    messageStates.add(state);
                    cursor.moveToNext();
                }
            }
        }catch (Exception ex){
            Logger.log(ex.getLocalizedMessage());
        }finally {
            close();
        }

        Logger.log("No of message sates in database " + messageStates.size());
        return  messageStates;
    }

    @Override
    public String getTableName() {
        return MESSAGE_STATE_TABLE;
    }
}