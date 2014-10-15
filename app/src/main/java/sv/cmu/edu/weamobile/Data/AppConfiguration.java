package sv.cmu.edu.weamobile.Data;

import com.google.gson.Gson;

import sv.cmu.edu.weamobile.Utility.Logger;

/**
 * Created by sumeet on 10/7/14.
 */
public class AppConfiguration {
//    {
//              "id": 1,
//            "channel": "http",
//            "feebackURL": "http://maxwell.sv.cmu.edu/wea/api/feedback/890141",
//            "alerts": [
//        {
//            "id": 1,
//                "capFilePath": null,
//                "channel": "CM",
//                "format": "short",
//                "protocol": "simple",
//                "scheduledFor": "2015-01-01T05:19:11.000Z",
//                "endingAt": "2013-01-01T07:15:00.000Z",
//                "text": "Flash Flood Warning issued January 1 at 5:18AM CST expiring January 1 at 7:15AM CST by NWS Houston/Galveston TX",
//                "alertType": "Extreme",
//                "alertType2": "TBD",
//                "polygonType": "Polygon",
//                "fdbURL": "NWS-120723-220266-154673",
//                "createdAt": null,
//                "updatedAt": null,
//                "polygon": [
//            {
//                "lng": "-94.71",
//                    "lat": "29.34",
//                    "id": 1
//            },
//            {
//                "lng": "-95.11",
//                    "lat": "29.09",
//                    "id": 2
//            },
//            {
//                "lng": "-94.98",
//                    "lat": "29.2",
//                    "id": 3
//            },
//            {
//                "lng": "-94.97",
//                    "lat": "29.19",
//                    "id": 4
//            },
//            {
//                "lng": "-94.91",
//                    "lat": "29.27",
//                    "id": 5
//            },
//            {
//                "lng": "-94.81",
//                    "lat": "29.31",
//                    "id": 6
//            },
//            {
//                "lng": "-94.83",
//                    "lat": "29.35",
//                    "id": 7
//            },
//            {
//                "lng": "-94.78",
//                    "lat": "29.33",
//                    "id": 8
//            },
//            {
//                "lng": "-94.8",
//                    "lat": "29.31",
//                    "id": 9
//            },
//            {
//                "lng": "-94.74",
//                    "lat": "29.33",
//                    "id": 10
//            },
//            {
//                "lng": "-94.74",
//                    "lat": "29.37",
//                    "id": 11
//            },
//            {
//                "lng": "-94.77",
//                    "lat": "29.36",
//                    "id": 12
//            },
//            {
//                "lng": "-94.78",
//                    "lat": "29.38",
//                    "id": 13
//            },
//            {
//                "lng": "-94.68",
//                    "lat": "29.48",
//                    "id": 14
//            },
//            {
//                "lng": "-94.61",
//                    "lat": "29.48",
//                    "id": 15
//            },
//            {
//                "lng": "-94.58",
//                    "lat": "29.53",
//                    "id": 16
//            },
//            {
//                "lng": "-94.5",
//                    "lat": "29.5",
//                    "id": 17
//            },
//            {
//                "lng": "-94.49",
//                    "lat": "29.54",
//                    "id": 18
//            },
//            {
//                "lng": "-94.44",
//                    "lat": "29.54",
//                    "id": 19
//            },
//            {
//                "lng": "-94.71",
//                    "lat": "29.34",
//                    "id": 20
//            }
//            ]
//        }
//        ]
//    }


    private int id;
    private String channel;
    private String protocol;
    private long scheduledFor;
    private long endingAt;
    private String text;
    private String alertType;
    private String alertType2;
    private String polygonType;
    private String feebackURL;
    private Alert [] alerts;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public long getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(long scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public long getEndingAt() {
        return endingAt;
    }

    public void setEndingAt(long endingAt) {
        this.endingAt = endingAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertType2() {
        return alertType2;
    }

    public void setAlertType2(String alertType2) {
        this.alertType2 = alertType2;
    }

    public String getPolygonType() {
        return polygonType;
    }

    public void setPolygonType(String polygonType) {
        this.polygonType = polygonType;
    }

    public String getFeebackURL() {
        return feebackURL;
    }

    public void setFeebackURL(String feebackURL) {
        this.feebackURL = feebackURL;
    }

    public static AppConfiguration fromJson(String s) {
        AppConfiguration configuration= null;
        if(isValidJson(s)){
            try {
                configuration= new Gson().fromJson(s, AppConfiguration.class);
            }catch (Exception ex){
                Logger.log(ex.getMessage());
            }
        }
        return configuration;
    }

    private static boolean isValidJson(String jsonString){
        return true;
    }

    private long getNextAlertTime(){
        return System.currentTimeMillis();
    }

    public Alert[] getAlerts() {
        return alerts;
    }

    public void setAlerts(Alert[] alerts) {
        this.alerts = alerts;
    }
}
