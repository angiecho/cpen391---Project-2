package team22.messagingapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Database {
    private static final String SENDER = "sender";
    private static final String RECIPIENT = "recipient";
    private static final String MESSAGE_TEXT = "message_text";
    private static final String MESSAGE_DATE = "message_date";
    private static final String MESSAGE_TABLE = "messages";

    public static final String KEY_ID = "_id";
    public static final String KEY_USERNAME= "username";
    public static final String KEY_PASSWORD = "password";
    private static final String USER_TABLE = "users";

    public static Message insertMessageToDatabase(int sender_id, int recipient_id, boolean sent, String message, SQLiteDatabase db) {

        ContentValues values = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        values.put(SENDER, sender_id);
        values.put(RECIPIENT, recipient_id);
        values.put(MESSAGE_TEXT, message);
        values.put(MESSAGE_DATE, dateFormat.format(date));
        int id = -1;
        try {
            id = (int) db.insertOrThrow(MESSAGE_TABLE, null, values);
        }catch(Exception e){
            e.printStackTrace();
        }

        if (id > -1){
            System.out.println("Inserted message to database!");
        }
        else {
            System.out.println("Message did not get inserted to the database...");
        }
        return new Message(message, sent, dateFormat.format(date), id);
    }

    public static long AddUser(String username, String password, Integer ID, SQLiteDatabase db) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_USERNAME, username);
        initialValues.put(KEY_PASSWORD, password);
        initialValues.put(KEY_ID, ID);
        return db.insert(USER_TABLE, null, initialValues);
    }

    public static Integer getUserID(String user, SQLiteDatabase db){
        String columns[] = {KEY_USERNAME, KEY_ID};
        String args[] = {user};
        Integer ID = -1;
        Cursor mCursor = db.query(USER_TABLE, columns, "username=?", args, null, null, null, String.valueOf(3));
        if (mCursor.moveToFirst()) {
            ID = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_ID));
        }
        mCursor.close();
        return ID;
    }

    public static boolean checkLogin(String username, String password, SQLiteDatabase db) throws SQLException {
        String columns[] = {KEY_USERNAME, KEY_PASSWORD};
        String args[] = {username, password};
        Cursor mCursor = db.query(USER_TABLE, columns, "username=? AND password=?", args, null, null, null, String.valueOf(3));
        if (mCursor != null) {
            if(mCursor.getCount() > 0) {
                mCursor.close();
                return true;
            }
            mCursor.close();
        }
        return false;
    }


}