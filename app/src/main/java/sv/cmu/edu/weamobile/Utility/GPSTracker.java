package sv.cmu.edu.weamobile.utility;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import sv.cmu.edu.weamobile.data.GeoLocation;
import sv.cmu.edu.weamobile.data.Message;

/**
 * Created by sumeet on 9/24/14.
 */
public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    // Flag for GPS status
    boolean isGPSEnabled = false;

    // Flag for network status
    boolean isNetworkEnabled = false;

    // Flag for GPS status
    boolean canGetLocation = false;

    Location location; // Location

    Message message;

    int countOfUpdates =0;
    int noOfTimesToCheck = 5;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 2; // 10 sec

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;

        locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);

        // Getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // No network provider is enabled
            Logger.log("GPS or NETWORK provider location manager is not available");
        } else {
            this.canGetLocation = true;
        }

//        ReceiveBothNetworkAndGPSLocationUpdates();
    }

    private Location ReceiveBothNetworkAndGPSLocationUpdates() {
        Logger.log("Register to get both network and GPS location.");
        try {
            if (isNetworkEnabled) {
                location = getNetworkLocationUpdates();
            }
            // If GPS enabled, get latitude/longitude using GPS Services
            if (isGPSEnabled) {
                location = getGPSLocationUpdates();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.log(e.getMessage());
        }

        return location;
    }

    private Location getGPSLocationUpdates() {
        Location loc = null;
        try{
            if(isGPSEnabled)
            {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Logger.log("Asking for GPS location");
                if (locationManager != null) {
                    loc = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc != null) {
                        location = loc;
                    }
                }
            }
        }catch(Exception ex){
            Logger.log(ex.getMessage());
        }
        return loc;
    }

    private Location getNetworkLocationUpdates() {
        Location loc = null;
        if(isNetworkEnabled){
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            Logger.log("Asking for network location");
            if (locationManager != null) {
                loc = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (loc != null) {
                    location = loc;
                }
            }
        }
        return loc;
    }


    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app.
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to check GPS/Wi-Fi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    public GeoLocation getNetworkGeoLocation(){
        location = getNetworkLocationUpdates();
        if(location == null){
            location = getGPSLocationUpdates();
        }

        GeoLocation loc = new GeoLocation(
                Double.toString(location.getLatitude()),
                Double.toString(location.getLongitude()),
                location.getAccuracy());
        stopUsingGPS();
        return loc;
    }

    public Location getNetworkLocation(){
        location = getNetworkLocationUpdates();
        if(location == null){
            location = getGPSLocationUpdates();
        }

        stopUsingGPS();
        return location;
    }

    private GeoLocation getGPSGeoLocation(){
        location = getGPSLocationUpdates();
        if(location == null){
            location = getNetworkLocationUpdates();
        }

        GeoLocation loc = new GeoLocation(
                Double.toString(location.getLatitude()),
                Double.toString(location.getLongitude()),
                location.getAccuracy());
        stopUsingGPS();
        return loc;
    }

    @Override
    public void onLocationChanged(Location location) {
        Logger.log(location.toString());
        this.countOfUpdates  = this.countOfUpdates +1;
        Logger.log("Received another GPS location update");

        if(isBetterLocation(location, this.location)){
            this.location = location;
            GeoLocation geoLocation = new GeoLocation(Double.toString(location.getLatitude()),Double.toString(location.getLongitude()), location.getAccuracy());
            if(message != null){
                String message= "Got GPS update";
                if(geoLocation == null || WEALocationHelper.isInPolygon(geoLocation, this.message.getPolygon())){
                    message = "Present in polygon or location not known, will show message";
                    AlertHelper.showAlert(mContext, this.message, geoLocation, message, true, false, false,false);
                    stopUsingGPS();
                }else if(WEALocationHelper.getDistance(this.message.getPolygon(), geoLocation) < 0.1){
                    noOfTimesToCheck = 60;
                    message ="Alert Time!! You are close, but not inside the polygon, remaining times to check "
                            + (noOfTimesToCheck -countOfUpdates + 1);

                }else if(WEALocationHelper.getDistance(this.message.getPolygon(), geoLocation) < 0.2){
                    noOfTimesToCheck = 20;
                    message ="Alert Time!! You are close, but not inside the polygon, remaining times to check "
                            + (noOfTimesToCheck -countOfUpdates +1);
                }else{
                    message ="Alert Time!! But you are not inside the polygon, remaining times to check "
                                                     + (noOfTimesToCheck - countOfUpdates);
                }

                Logger.log(message);
                WEAUtil.showMessageIfInDebugMode(mContext, message);
            }
        }

        if(countOfUpdates > noOfTimesToCheck){
            stopUsingGPS();
            Logger.log("Stopping GPS usage");
        }
    }


    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onProviderEnabled(String provider) {
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Logger.log("Provider : " + provider + " Status: " + status);
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > MIN_TIME_BW_UPDATES;
        boolean isSignificantlyOlder = timeDelta < -MIN_TIME_BW_UPDATES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void keepLookingForPresenceInPolygonAndShowAlertIfNecessary(Context context,
                                                                       Message alert) {
        this.message = alert;
        countOfUpdates = 0;

        ReceiveBothNetworkAndGPSLocationUpdates();

    }
}
