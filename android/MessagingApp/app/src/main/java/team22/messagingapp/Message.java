package team22.messagingapp;

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
