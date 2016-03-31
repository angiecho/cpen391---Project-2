package team22.messagingapp;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
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
import java.lang.Long;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase messages;
    private BluetoothAdapter BA;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BluetoothSocket socket;

    private String key;
    private String iv;

    private static final String SENDER = "sender";
    private static final String RECIPIENT = "recipient";
    private static final String MESSAGE_TEXT = "message_text";
    private static final String MESSAGE_DATE = "message_date";
    private static final String DATABASE_NAME = "messages";
    private static final String FORMAT = "US-ASCII";
    private static final Long BLACK = 0xffffffff;
    private static final Long BLUE = 0xff000000;
    private static final Integer WIDTH = 300;
    private static final Integer TEXT_SIZE = 50;

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    public class Message {
        public String text;
        public boolean sent;
        public Date date;

        public Message(String text, boolean sent, String date){
            System.out.println("Creating with " + text);
            this.text = text;
            this.sent = sent;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                this.date = dateFormat.parse(date);
            }catch(ParseException e){
                e.printStackTrace();
            }
        }
    }

    // TODO: Store user's in database and retrieve user ID upon login
    // Returns the android user's ID.
    public int getCurrentSender(){
        Bundle contactBundle = getIntent().getExtras();
        String string = contactBundle.getString("senderName");
        if (string.toLowerCase().contentEquals("caleb")){
            return 1;
        }
        else if (string.toLowerCase().contentEquals("charles")){
            return 2;
        }
        else if (string.toLowerCase().contentEquals("cho")){
            return 3;
        }
        return 1;
    }

    // TODO: Change according to above TODO
    // Returns the name mapped by the android user ID
    public String getSenderName(int id){
        if (id == 1){
            return "Caleb";
        }
        else if (id == 2){
            return "Charles";
        }
        else if (id == 3){
            return "Cho";
        }
        return "???";
    }

    // TODO Change according to above TODO
    // Returns the message receiver's ID.
    public int getCurrentReceiver(){
        Bundle contactBundle = getIntent().getExtras();
        String string = contactBundle.getString("receiver");
        if (string.toLowerCase().contentEquals("caleb")){
            return 1;
        }
        else if (string.toLowerCase().contentEquals("charles")){
            return 2;
        }
        else if (string.toLowerCase().contentEquals("cho")){
            return 3;
        }
        return 1;
    }

    public void showNotification(String message, String author){
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager.getStreamVolume(AudioManager.STREAM_RING) > 0) {
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                ringtone.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //otherwise
        else {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
            vibrator.vibrate(100);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon3)
                        .setContentTitle(author)
                        .setContentText(message);

        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    public void scrollDown(){
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

    // Worker thread conts listens for bluetooth data.
    public void listenMessages(){
        final Handler handler = new Handler();
        final byte delimiter = 0; //This is the ASCII code for a \0
        final byte keyDelimiter = 2;
        final byte ivDelimiter = 3;

        if (workerThread == null) {
            workerThread = new Thread(new Runnable() {
                public void run() {
                    readBufferPosition = 0;
                    readBuffer = new byte[4096]; //4096 bytes SHOULD be enough....
                    stopWorker = false;
                    System.out.println("ayyyy"); //this stupid line is just for me to know if it's successfully connected - i'll get rid of it later

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int bytesAvailable = inputStream.available();
                            if (bytesAvailable > 1) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte bite = packetBytes[i];
                                    System.out.println(bite);
                                    if (bite == delimiter && readBufferPosition > 1) {
                                        byte[] encodedBytes = new byte[readBufferPosition - 1];
                                        System.arraycopy(readBuffer, 1, encodedBytes, 0, encodedBytes.length);

                                        int chunkNumber = (int)Math.ceil(encodedBytes.length/16);
                                        byte[][] byteChunks = new byte[chunkNumber][16];
                                        int start = 0;
                                        for(int j = 0; j < chunkNumber; j++) {
                                            if(start + 16 > encodedBytes.length) {
                                                System.arraycopy(encodedBytes, start, byteChunks[j], 0, encodedBytes.length - start);
                                            } else {
                                                System.arraycopy(encodedBytes, start, byteChunks[j], 0, 16);
                                            }
                                            start += 16;
                                        }
                                        String[] decodedByteChunks = new String[chunkNumber];
                                        for (int j = 0; j < byteChunks.length; j++){
                                            try {
                                                decodedByteChunks[j] = AESEncryption.decrypt(byteChunks[j], key, iv);
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                        final String data = decodedByteChunks.toString();
                                        System.out.println(data);
                                        System.out.println("S/R is " + readBuffer[0]);

                                        final int receiver_id = 0b00001111 & readBuffer[0];
                                        final int sender_id = 0b00001111 & (readBuffer[0] >>> 4);

                                        System.out.println("Receiver ID is: " + receiver_id);
                                        System.out.println("Sender ID is: " + sender_id);

                                        final Date date = insertMessageToDatabase(sender_id, receiver_id, data);

                                        readBufferPosition = 0;
                                        handler.post(new Runnable() {
                                            public void run() {
                                                //insert check if it's the same user we're getting stuff from

                                                if (sender_id == getCurrentReceiver()) {
                                                    insertReceivedMessageToView(data, false, date);
                                                } else {
                                                    //check if volume

                                                    String author = getSenderName(sender_id);
                                                    showNotification(data, author);
                                                }
                                                scrollDown();
                                            }
                                        });

                                    } else if(bite == keyDelimiter && readBufferPosition > 1) {
                                        readBuffer[readBufferPosition++] = bite;
                                        byte[] encodedBytes = new byte[readBufferPosition - 1];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        key = new String(encodedBytes, FORMAT);
                                        System.out.println("Key is : " + key);
                                        readBufferPosition = 0;

                                    }else if(bite == ivDelimiter && readBufferPosition > 1) {
                                        readBuffer[readBufferPosition++] = bite;
                                        byte[] encodedBytes = new byte[readBufferPosition - 1];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        iv  = new String(encodedBytes, FORMAT);
                                        System.out.println("IV is: " + iv);
                                        readBufferPosition = 0;
                                    } else {
                                        readBuffer[readBufferPosition++] = bite;
                                    }

                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            stopWorker = true;
                            break;
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            stopWorker = true;
                            break;
                        }
                    }
                }
            });
            workerThread.start();
        }
        else {
            stopWorker = false;
        }
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
                            final View view2 = findViewById(x);
                            if (view2 != null) {
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        view.scrollTo(0, view2.getTop());
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        workerThread.interrupt();
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
        chatName.setTextColor(BLUE);
        chatName.setTextSize(TEXT_SIZE);
        chatName.setText(chatWith);
        chatName.setGravity(Gravity.CENTER_HORIZONTAL);
        parentLinearLayout.addView(chatName);

        Log.v("Chat With:", chatWith);

        messages = openOrCreateDatabase("Messages", Context.MODE_PRIVATE, null);
        //messages.execSQL("DROP TABLE messages;"); //Drop table is here in case I want to clear the database
        messages.execSQL("CREATE TABLE IF NOT EXISTS messages(id INTEGER PRIMARY KEY AUTOINCREMENT, sender INTEGER, recipient INTEGER, message_text VARCHAR, message_date DATETIME);");
        socket = ((MessagingApplication) getApplication()).getSocket();

        if (!socket.isConnected()){
            try {
                socket.connect();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        else {
            outputStream = ((MessagingApplication) getApplication()).getOutputStream();
            inputStream = ((MessagingApplication) getApplication()).getInputStream();
        }
    }

    public void loadHistory(){
        //get id of contact accessed
        System.out.println("Attempting to load message history...");
        int recipient_id = getCurrentReceiver();
        int sender_id = getCurrentSender();

        String columns[] = {MESSAGE_TEXT, MESSAGE_DATE, SENDER, RECIPIENT, "id"};
        String args[] = {String.valueOf(recipient_id), String.valueOf(recipient_id), String.valueOf(sender_id), String.valueOf(sender_id)};

        String selectionQuery = "(recipient =? OR sender =?) AND (recipient =? OR sender =?)";

        //Limit of 15 is here because I don't want to load all the messages in the database
        //since that is potentially... Slow.
        Cursor cursor = messages.query(DATABASE_NAME, columns, selectionQuery, args, null, null, "id desc", "15");
        if (cursor.moveToFirst()) {
            ArrayList<Message> pastMessages = new ArrayList<>();
            do {
                try {
                    int r_id = cursor.getInt(cursor.getColumnIndexOrThrow(SENDER));
                    pastMessages.add(new Message(cursor.getString(cursor.getColumnIndex(MESSAGE_TEXT)), r_id == recipient_id, cursor.getString(cursor.getColumnIndex(MESSAGE_DATE))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            while (cursor.moveToNext());

            for (int j = pastMessages.size() - 1; j >= 0; j--) {
                if (pastMessages.get(j).sent) {
                    insertReceivedMessageToView(pastMessages.get(j).text, false, pastMessages.get(j).date);
                } else {
                    insertSentMessageToView(pastMessages.get(j).text, false, pastMessages.get(j).date);
                }
            }
        }
        cursor.close();
        listenMessages();
    }

    public int loadMoreMessages(){
        int recipient_id = getCurrentReceiver();
        int sender_id = getCurrentSender();

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.message_holder);
        int messagesShown = 0;
        View view = null;
        if (linearLayout != null) {
            messagesShown = linearLayout.getChildCount();
            view = linearLayout.getChildAt(0);
        }

        int messagesToShowCount = messagesShown/2 + 15;
        String columns[] = {MESSAGE_TEXT, MESSAGE_DATE, SENDER, RECIPIENT, "id"};
        String args[] = {String.valueOf(recipient_id), String.valueOf(recipient_id), String.valueOf(sender_id), String.valueOf(sender_id)};

        String selectionQuery = "(recipient =? OR sender =?) AND (recipient =? OR sender =?)";

        //Show 15 more!
        Cursor cursor = messages.query(DATABASE_NAME, columns, selectionQuery, args, null, null, "id desc", String.valueOf(messagesToShowCount));
        if (cursor.moveToFirst()) {
            ArrayList<Message> pastMessages = new ArrayList<>();
            do {
                try {
                    int r_id = cursor.getInt(cursor.getColumnIndexOrThrow(SENDER));
                    pastMessages.add(new Message(cursor.getString(cursor.getColumnIndex(MESSAGE_TEXT)), r_id == recipient_id, cursor.getString(cursor.getColumnIndex(MESSAGE_DATE))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            while (cursor.moveToNext());
            int difference = cursor.getCount() - messagesShown/2;

            for (int j = pastMessages.size() - difference; j <= pastMessages.size()-1; j++) {
                if (pastMessages.get(j).sent) {
                   insertReceivedMessageToView(pastMessages.get(j).text, true, pastMessages.get(j).date);
                } else {
                    insertSentMessageToView(pastMessages.get(j).text, true, pastMessages.get(j).date);
                }
            }
        }
        if (cursor.getCount() != messagesShown) {
            cursor.close();
            return view.getId();
        }
        cursor.close();
        return 0;
    }

    // Sends an encrypted message and header to the bluetooth
    public void sendMessage(View view) {
        // Do something in response to button
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = null;
        if (editText != null) {
            message = editText.getText().toString();
            editText.setText("");
        }

        if (message != null && !message.trim().isEmpty()){
            int sender_id = getCurrentSender();  //Hardcoded for now, make a get function...
            int recipient_id = getCurrentReceiver(); //Hardcoded for now, make a get function...
            int messageHeader = 16*sender_id + recipient_id; //16* = bit shift left 4

            Date d = insertMessageToDatabase(sender_id, recipient_id, message);
            insertSentMessageToView(message, false, d);
            scrollDown();

            if (outputStream != null) {
                sendMessageBluetooth(message, messageHeader);
            }
        }
    }

    // Helper fcn for sending message bytes to bluetooth.
    private void sendMessageBluetooth(String message, int messageHeader){
        System.out.println("Attempting to send message!");
        try {
            outputStream.write(5); //This is a way to say "Give me key/iv!"
            while (key == null && iv == null);
            outputStream.write(messageHeader);
            try {
                ArrayList<String> stringChunks = new ArrayList<>();
                for (int start = 0; start < message.length(); start += 16) {
                    stringChunks.add(message.substring(start, Math.min(message.length(), start + 16)));
                }
                for (int start = 0; start < stringChunks.size(); start++){
                    String paddedMessage = String.format("%1$16s", stringChunks.get(start));
                    byte [] cipher = AESEncryption.encrypt(paddedMessage, key, iv);
                    outputStream.write(cipher);
                }
                outputStream.write(0);
                key = null;
                iv = null;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // Display received message, store it in the DB, and get a notification.
    public void receiveMessage(View view){
        String message = getMessage();
        if (message != null) {
            insertMessageToDatabase(getCurrentReceiver(), getCurrentSender(), message);
            insertReceivedMessageToView(message, false, new Date());
            String author = getSenderName(getCurrentReceiver());
            showNotification(message, author);
        }
    }

    // Store message and sender/receiver ID's, and returns the current date to the DB.
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

    // Returns the given date as a string.
    public String getStringDate(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        return dateFormat.format(date);
    }

    // Display sent message and returns the id of the text view.
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
        dateView.setMaxWidth(WIDTH);

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

    // Display received message and returns the id of the text view.
    public int insertReceivedMessageToView(String message, boolean top, Date date){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.message_holder);
        TextView textView = new TextView(getApplicationContext());
        textView.setId(View.generateViewId());
        textView.setText(message);
        textView.setTextColor(BLUE);
        textView.setMaxWidth(WIDTH);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setBackgroundResource(R.drawable.bubble_grey);

        TextView dateView = new TextView(getApplicationContext());
        dateView.setId(View.generateViewId());
        dateView.setText(getStringDate(date));
        dateView.setMaxWidth(WIDTH);

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

    // Returns the send text view with black text
    public TextView getSendMessageTextView(){
        TextView textView = new TextView(this);
        textView.setTextColor(BLACK);
        textView.setBackgroundResource(R.drawable.bubble_blue);
        textView.setMaxWidth(WIDTH);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.LEFT);
        return textView;
    }

    // TODO: Want to get encrypted message and header from bluetooth in input stream and decrypt it.
    // This is a testing function! I use it to return a randomly generated String
    public String getMessage(){
        return getRandomString();
    }

    // Testing function to generate random string of length 20
    public String getRandomString(){
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder stringbuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            char character = chars[random.nextInt(chars.length)];
            stringbuilder.append(character);
        }
        return stringbuilder.toString();
    }


}