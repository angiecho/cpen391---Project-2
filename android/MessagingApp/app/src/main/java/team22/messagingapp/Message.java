package team22.messagingapp;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {

    public String text;
    public boolean sent;
    public Date date;
    public int id;

    public Message(String text, boolean sent, String date, int id){
        this.text = text;
        this.sent = sent;
        this.id = id;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            this.date = dateFormat.parse(date);
        }catch(ParseException e){
            e.printStackTrace();
        }
    }



}