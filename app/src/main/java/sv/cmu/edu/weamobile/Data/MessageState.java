package sv.cmu.edu.weamobile.data;

import com.google.gson.Gson;

import java.util.Date;

import sv.cmu.edu.weamobile.utility.WEAUtil;

/**
 * Created by sumeet on 11/7/14.
 */
public class MessageState {

    private int id;
    //these properties are set by phone
    private boolean isAlreadyShown = false;
    private boolean isFeedbackGiven = false;
    private long timeWhenShownToUserInEpoch;
    private long timeWhenFeedbackGivenInEpoch;
    private GeoLocation locationWhenShown;
    private boolean isAlertToBeShown = false;
    private boolean isInPolygon= false;
    private boolean isLocationHistoryInPolygon = false;
    private boolean isFutureLocationInPolygon = false;
    private boolean isNotGeoTargeted = false;
    private Status status = null;

    public String getScheduledFor() {
        return scheduledFor;
    }

    private String scheduledFor;

    public MessageState(int id, String scheduledForTime){
        this.id = id;
        this.scheduledFor = scheduledForTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getScheduledEpochInSeconds(){
        long epoch = 0;
        Date date = WEAUtil.getTimeStringFromJsonTime(scheduledFor, "UTC");
        if(date != null){
            epoch = date.getTime();
        }
        return epoch/1000;
    }

    public boolean isFutureLocationInPolygon() {
        return isFutureLocationInPolygon;
    }

    public void setFutureLocationInPolygon(boolean isFutureLocationInPolygon) {
        this.isFutureLocationInPolygon = isFutureLocationInPolygon;
    }

    public boolean isLocationHistoryInPolygon() {
        return isLocationHistoryInPolygon;
    }

    public void setLocationHistoryInPolygon(boolean isLocationHistoryInPolygon) {
        this.isLocationHistoryInPolygon = isLocationHistoryInPolygon;
    }

    public boolean isInPolygon() {
        return isInPolygon;
    }

    public void setInPolygon(boolean isInPolygon) {
        this.isInPolygon = isInPolygon;
    }

    public boolean isNotGeoTargeted() {
        return isNotGeoTargeted;
    }

    public void setNotGeoTargeted(boolean isGeoTargeted) {
        this.isNotGeoTargeted = isGeoTargeted;
    }

    public enum Status {
        received, discarded, shown, clicked
    }

    public long getTimeWhenFeedbackGivenInEpoch() {
        return timeWhenFeedbackGivenInEpoch;
    }

    public void setTimeWhenFeedbackGivenInEpoch(long timeWhenFeedbackGivenInEpoch) {
        this.timeWhenFeedbackGivenInEpoch = timeWhenFeedbackGivenInEpoch;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public boolean isAlreadyShown() {
        return isAlreadyShown;
    }

    public void setAlreadyShown(boolean isAlreadyShown) {
        this.isAlreadyShown = isAlreadyShown;
    }

    public long getTimeWhenShownToUserInEpoch() {
        return timeWhenShownToUserInEpoch;
    }

    public void setTimeWhenShownToUserInEpoch(long timeWhenShownToUserInEpoch) {
        this.timeWhenShownToUserInEpoch = timeWhenShownToUserInEpoch;
    }

    public GeoLocation getLocationWhenShown() {
        GeoLocation location = null;
        if(locationWhenShown!= null && locationWhenShown.getLat()!= null && locationWhenShown.getLng() != null){
            location = locationWhenShown;
        }
        return location;
    }

    public void setLocationWhenShown(GeoLocation locationWhenShown) {
        this.locationWhenShown = locationWhenShown;
    }

    public String getJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static MessageState fromJson(String s) {
        return new Gson().fromJson(s, MessageState.class);
    }

    public boolean isFeedbackGiven() {
        return isFeedbackGiven;
    }

    public void setFeedbackGiven(boolean isFeedbackGiven) {
        this.isFeedbackGiven = isFeedbackGiven;
    }

    public boolean isToBeShown() {
        return isAlertToBeShown;
    }

    public void setIsToBeShown(boolean isInPolygon) {
        this.isAlertToBeShown = isInPolygon;
    }

    //Unique identifier for databse primary key
    public String getUniqueId()
    {
        return Integer.toString(id)+Long.toString(getScheduledEpochInSeconds());
    }
}
