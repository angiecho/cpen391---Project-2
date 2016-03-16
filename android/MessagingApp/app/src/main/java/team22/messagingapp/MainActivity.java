package team22.messagingapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.database.sqlite.*;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase messages;
    private BluetoothAdapter BA;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messages = openOrCreateDatabase("Messages", Context.MODE_PRIVATE, null);
        messages.execSQL("CREATE TABLE IF NOT EXISTS messages(sender INTEGER, recipient INTEGER, message_text VARCHAR, message_date DATETIME, PRIMARY KEY(sender, recipient, message_date));");
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        /*
        //int sender_id = 0;  //Hardcoded for now, make a get function...

        //get recipient id
        int recipient_id = 1; //Hardcoded for now, make a get function...

        String selectQuery = "SELECT message_text, message_date FROM messages WHERE (recipient = '" + recipient_id + "' OR sender = '" + recipient_id + "') ORDER BY message_date desc LIMIT 10";

        Cursor c =  messages.rawQuery(selectQuery, new String[] {});

        if (c.moveToFirst()){
            String[] pastMessages = new String[c.getCount()];
            int x = 0;
            while(c.moveToNext()){
                pastMessages[x] = c.getString(c.getColumnIndex("message_text"));
                x++;
            }
        }

        c.close(); */

        //Code for Bluetooth... Bluetooth won't work on emulator so it's commented out for now.
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null){
            finish();
        }
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }

    }
    public void sendMessage(View view) {
        // Do something in response to button
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = null;
        if (editText != null) {
            message = editText.getText().toString();
            editText.setText("");
        }

        if (message != null && !message.trim().isEmpty()){
            //get sender id
            int sender_id = 0;  //Hardcoded for now, make a get function...

            //get recipient id
            int recipient_id = 1; //Hardcoded for now, make a get function...

           // messages.execSQL("INSERT INTO messages VALUES ('" + sender_id +"', '" + recipient_id + "', '" + message + "', datetime());");
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.message_holder);
            TextView textView = new TextView(this);
            textView.setText(message);
            textView.setGravity(Gravity.RIGHT);
            if (linearLayout != null) {
                linearLayout.addView(textView);
            }
            final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
            if (scrollView != null) {
                scrollView.post(new Runnable() {

                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
            //Add Bluetooth here
            //Will need to append some sort of header for the DE2 to parse here
            //As well as (eventually) encrypt the message
        }





        //intent.putExtra(EXTRA_MESSAGE, message);
        //startActivity(intent);

    }

    public void receiveMessage(View view){
        String message = getMessage();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.message_holder);
        TextView textView = new TextView(this);
        textView.setText(message);
        if (linearLayout != null) {
            linearLayout.addView(textView);
        }

        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        if (scrollView != null) {
            scrollView.post(new Runnable() {

                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

    }

    public String getMessage(){
        //This will (eventually) poll the bluetooth to get the message. For now, I'm getting
        //a constant String
        return "Hi!";
    }
}
