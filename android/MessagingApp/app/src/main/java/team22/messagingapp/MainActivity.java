package team22.messagingapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.database.sqlite.*;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase messages;
    private BluetoothAdapter BA;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BluetoothSocket socket;

    private static final String SENDER = "sender";
    private static final String RECIPIENT = "recipient";
    private static final String MESSAGE_TEXT = "message_text";
    private static final String MESSAGE_DATE = "message_date";
    private static final String DATABASE_NAME = "messages";

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    public class Message {
        public String text;
        public boolean sent;
        public Date date;

        public Message(String t, boolean s, String d){
            System.out.println("Creating with " + t);
            text = t;
            sent = s;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = dateFormat.parse(d);
            }catch(ParseException e){
                e.printStackTrace();
            }

        }
    }

    private void initBluetooth() throws IOException {
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null){
            finish();
        }
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }

    }

    private void chooseBluetooth() throws IOException{
        //In the future, we **REALLY** want to set it up
        // so you can choose which BT to be connected to
        // Currently, we say connect to the first one listed on paired devices...
        // Which is (quite obviously) quite bad.
        Set<BluetoothDevice> pairedDevices;
        pairedDevices = BA.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();

        for(BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName());
        }
        if (pairedDevices.size() > 0) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.message_holder);
            for (int x = 0; x < list.size(); x++) {
                TextView textView = new TextView(this);
                textView.setText(list.get(x));
                textView.setGravity(Gravity.CENTER);
                if (linearLayout != null) {
                    linearLayout.addView(textView);
                }
            }
            BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[pairedDevices.size()]);
            BluetoothDevice device = devices[0];
            System.out.println(device.getName());
            ParcelUuid[] uuids = device.getUuids();
            System.out.println(uuids[0]);
            try{
                socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                try {
                    socket.connect();
                }catch (IOException e){
                    e.printStackTrace();
                    try{
                        socket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                        socket.connect();
                    }catch(Exception e2){
                        e2.printStackTrace();
                    }
                }
                if (socket.isConnected()) {
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                    listenMessages();
                }
                else {
                    System.out.println("Could not connect to socket!");
                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void listenMessages(){
        final Handler handler = new Handler();
        final byte delimiter = 0; //This is the ASCII code for a \0

        workerThread = new Thread(new Runnable() {
            public void run() {
                readBufferPosition = 0;
                readBuffer = new byte[1024];
                stopWorker = false;
                System.out.println("ayyyy"); //this stupid line is just for me to know if it's successfully connected - i'll get rid of it later

                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = inputStream.available();
                        if(bytesAvailable > 2) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];

                                System.out.println(b);
                                if(b == delimiter) {
                                       byte[] encodedBytes = new byte[readBufferPosition - 2];
                                        System.arraycopy(readBuffer, 2, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        System.out.println(data);

                                        //We're... Going to need a system to store the multi messages into one.
                                        int receiver_id = 0x00001111 & readBuffer[0];
                                        int sender_id = (0x11110000 & readBuffer[0]) >>> 4;
                                        final Date d = insertMessageToDatabase(sender_id, receiver_id, data);
                                    //Check receiver here

                                        readBufferPosition = 0;

                                        handler.post(new Runnable() {
                                            public void run() {
                                                //insert check if it's the same user we're getting stuff from
                                                insertReceivedMessageToView(data, false, d);

                                                //otherwise do this
                                                //check if volume
//                                                try {
//                                                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//                                                    r.play();
//                                                } catch (Exception e) {
//                                                    e.printStackTrace();
//                                                }
                                                //otherwise

                                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                v.vibrate(400);

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
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }

                            }
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                        stopWorker = true;
                        break;
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        stopWorker = true;
                        break;
                    }
                }
            }
        });
        workerThread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadHistory();
        final MessageScrollView view = (MessageScrollView) findViewById(R.id.scrollView);
        if (view != null) {
            view.setOnTopReachedListener(
                new MessageScrollView.onTopReachedListener() {
                    @Override
                    public void onTopReached() {
                        int x = loadMoreMessages();

                        if (x != 0){
                            final View v = findViewById(x);
                            if (v != null) {
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        view.scrollTo(0, v.getTop());
                                        //This needs to be done **after** because
                                        //Otherwise, it doesn't know where the new top is
                                        //because of the change in height
                                    }
                                });
                            }

                        }

                    }
                }
            );
        }

        try{
            chooseBluetooth();

        }catch(IOException e){
            e.printStackTrace();
        }


    }

    @Override
    protected void onStop() {

        super.onStop();
        try {
            stopWorker = true;
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the name of the chat. ie. talking to "Caleb"
        Bundle chatBundle = getIntent().getExtras();
        String chatWith = chatBundle.getString("receiver");

        LinearLayout parentLinearLayout = (LinearLayout) findViewById(R.id.chat_name);
        TextView chatName = new TextView(this);
        chatName.setTextColor(0xff000000);
        chatName.setTextSize(50);
        chatName.setText(chatWith);
        chatName.setGravity(Gravity.CENTER_HORIZONTAL);
        parentLinearLayout.addView(chatName);

        Log.v("Chat With:", chatWith);

        messages = openOrCreateDatabase("Messages", Context.MODE_PRIVATE, null);
        //messages.execSQL("DROP TABLE messages;"); //Drop table is here in case I want to clear the database
        messages.execSQL("CREATE TABLE IF NOT EXISTS messages(id INTEGER PRIMARY KEY AUTOINCREMENT, sender INTEGER, recipient INTEGER, message_text VARCHAR, message_date DATETIME);");

        //Code for Bluetooth... Bluetooth won't work on emulator, so comment it out if on emu
        try {
            initBluetooth();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void loadHistory(){
        //get id of contact accessed
        System.out.println("Attempting to load message history...");
        int recipient_id = 1; //Hardcoded for now, make a get function...

        String columns[] = {MESSAGE_TEXT, MESSAGE_DATE, SENDER, RECIPIENT, "id"};
        String args[] = {String.valueOf(recipient_id), String.valueOf(recipient_id)};

        String selectionQuery = "recipient =? OR sender =?";

        //Limit of 15 is here because I don't want to load all the messages in the database
        //since that is potentially... Slow.
        Cursor c = messages.query(DATABASE_NAME, columns, selectionQuery, args, null, null, "id desc", "15");
        if (c.moveToFirst()) {
            ArrayList<Message> pastMessages = new ArrayList<>();
            do {
                try {
                    int r_id = c.getInt(c.getColumnIndexOrThrow(SENDER));
                    pastMessages.add(new Message(c.getString(c.getColumnIndex(MESSAGE_TEXT)), r_id == recipient_id, c.getString(c.getColumnIndex(MESSAGE_DATE))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            while (c.moveToNext());

            for (int y = pastMessages.size() - 1; y >= 0; y--) {
                if (pastMessages.get(y).sent) {
                    insertReceivedMessageToView(pastMessages.get(y).text, false, pastMessages.get(y).date);
                } else {
                    insertSentMessageToView(pastMessages.get(y).text, false, pastMessages.get(y).date);
                }
            }
        }
        c.close();

        listenMessages();
    }

    public int loadMoreMessages(){
        int recipient_id = 1; //make a getter function
        LinearLayout ll = (LinearLayout) findViewById(R.id.message_holder);
        int messagesShown = 0;
        View v = null;
        if (ll != null) {
            messagesShown = ll.getChildCount();
            v = ll.getChildAt(0);
        }

        int messagesToShowCount = messagesShown/2 + 15;
        String columns[] = {MESSAGE_TEXT, MESSAGE_DATE, SENDER, RECIPIENT, "id"};
        String args[] = {String.valueOf(recipient_id), String.valueOf(recipient_id)};

        String selectionQuery = "recipient =? OR sender =?";

        //Show 15 more!
        Cursor c = messages.query(DATABASE_NAME, columns, selectionQuery, args, null, null, "id desc", String.valueOf(messagesToShowCount));
        if (c.moveToFirst()) {
            ArrayList<Message> pastMessages = new ArrayList<>();
            do {
                try {
                    int r_id = c.getInt(c.getColumnIndexOrThrow(SENDER));
                    pastMessages.add(new Message(c.getString(c.getColumnIndex(MESSAGE_TEXT)), r_id == recipient_id, c.getString(c.getColumnIndex(MESSAGE_DATE))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            while (c.moveToNext());
            int difference = c.getCount() - messagesShown/2;
            for (int y = pastMessages.size()  - difference; y <= pastMessages.size() -1; y++) {

                if (pastMessages.get(y).sent) {
                   insertReceivedMessageToView(pastMessages.get(y).text, true, pastMessages.get(y).date);
                } else {
                    insertSentMessageToView(pastMessages.get(y).text, true, pastMessages.get(y).date);
                }

            }
        }
        if (c.getCount() != messagesShown) {
            c.close();
            return v.getId();
        }
        c.close();
        return 0;
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

            int messageHeader = 16 * sender_id + recipient_id; //16* = bit shift left 4
            Date d = insertMessageToDatabase(sender_id, recipient_id, message);

            insertSentMessageToView(message, false, d);
            final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
            if (scrollView != null) {
                scrollView.post(new Runnable() {

                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }

            if (outputStream != null) {
                sendMessageBluetooth(message, messageHeader);
            }
        }

    }

    private void sendMessageBluetooth(String message, int messageHeader){
        System.out.println("Attempting to send message!");
        try {
            int messageLength = message.length();
            int messagePosition = 0;
            System.out.println(messageLength);

            while(messageLength > 255){
                //sender = 0000 receiver = 0000
                outputStream.write(messageHeader);
                outputStream.write(0);
                String s = message.substring(messagePosition, messagePosition+255);
                messagePosition += 255;
                messageLength -= 255;
                outputStream.write(s.getBytes("US-ASCII"));
            }
            outputStream.write(messageHeader);
            outputStream.write(messageLength);
            String s = message.substring(messagePosition, messagePosition+messageLength);
            outputStream.write(s.getBytes("US-ASCII"));
            System.out.println("Sent out " + s);
            outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage(View view){
        String message = getMessage();
        if (message != null) {
            insertMessageToDatabase(1, 0, message);
            insertReceivedMessageToView(message, false, new Date());
        }

    }

    public Date insertMessageToDatabase(int sender_id, int recipient_id, String message) {
        ContentValues values = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        values.put(SENDER, sender_id);
        values.put(RECIPIENT, recipient_id);
        values.put(MESSAGE_TEXT, message);
        values.put(MESSAGE_DATE, dateFormat.format(date));

        if (messages.insert(DATABASE_NAME, null, values) > -1){
            System.out.println("Inserted message to database!");
        }
        else {
            System.out.println("Message did not get inserted to the database...");
        }
        return date;
    }

    public String getStringDate(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        return dateFormat.format(date);
    }

    public int insertSentMessageToView(String message, boolean top, Date date){
        LinearLayout parentLinearLayout = (LinearLayout) findViewById(R.id.message_holder);
        TextView textView = getSendMessageTextView();
        textView.setText(message);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setGravity(Gravity.RIGHT);
        linearLayout.addView(textView);
        linearLayout.setId(View.generateViewId());


        TextView dateView = new TextView(getApplicationContext());
        dateView.setId(View.generateViewId());
        dateView.setText(getStringDate(date));
        dateView.setMaxWidth(300);

        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setGravity(Gravity.RIGHT);
        linearLayout2.addView(dateView);
        linearLayout2.setId(View.generateViewId());

        if (parentLinearLayout != null) {
            if (top){
                parentLinearLayout.addView(linearLayout, 0);
                parentLinearLayout.addView(linearLayout2, 1);
            }
            else {
                parentLinearLayout.addView(linearLayout);
                parentLinearLayout.addView(linearLayout2);
            }
        }
        return linearLayout.getId();
    }
    
    public int insertReceivedMessageToView(String message, boolean top, Date date){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.message_holder);
        TextView textView = new TextView(getApplicationContext());
        textView.setId(View.generateViewId());
        textView.setText(message);
        textView.setTextColor(0xff000000);
        textView.setMaxWidth(300);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setBackgroundResource(R.drawable.bubble_grey);

        TextView dateView = new TextView(getApplicationContext());
        dateView.setId(View.generateViewId());
        dateView.setText(getStringDate(date));
        dateView.setMaxWidth(300);

        if (linearLayout != null) {
            if (top){
                linearLayout.addView(textView, 0);
                linearLayout.addView(dateView, 1);
            }
            else {
                linearLayout.addView(textView);
                linearLayout.addView(dateView);

            }

        }



        return textView.getId();
    }

    public TextView getSendMessageTextView(){
        TextView textView = new TextView(this);
        textView.setTextColor(0xffffffff);
        textView.setBackgroundResource(R.drawable.bubble_blue);
        textView.setMaxWidth(300);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.LEFT);
        return textView;
    }
    //This is a testing function! I use it to return
    //a randomly generated String
    public String getMessage(){
        return getRandomString();
    }

    //More testing functions!
    public String getRandomString(){
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }


}