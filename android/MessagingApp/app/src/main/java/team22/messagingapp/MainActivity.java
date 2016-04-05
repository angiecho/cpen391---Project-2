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

import java.io.*;
import java.text.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase messages;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BluetoothSocket socket;

    private static volatile String keyRequested;
    private static volatile String ivRequested;
    private static final int KEY_IV_SIZE = 16;

    private static final String SENDER = "sender";
    private static final String RECIPIENT = "recipient";
    private static final String MESSAGE_TEXT = "message_text";
    private static final String MESSAGE_DATE = "message_date";
    private static final String DATABASE_NAME = "messages";
    private static final String FORMAT = "US-ASCII";
    private static final int BLACK = 0xffffffff;
    private static final int BLUE = 0xff000000;
    private static final Integer WIDTH = 300;
    private static final Integer TEXT_SIZE = 50;
    private static final Integer ENQ = 5;

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    private Handler handler;

    public class Message {
        public String text;
        public boolean sent;
        public Date date;

        public Message(String text, boolean sent, String date){
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
        this.handler = new Handler();
        if (workerThread == null) {
            workerThread = new Thread(new Runnable() {
                public void run() {
                    readBufferPosition = 0;
                    readBuffer = new byte[4096]; //4096 bytes SHOULD be enough....
                    stopWorker = false;
                    System.out.println("listenMessages started");

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int bytesAvailable = inputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);
                                for (byte bite : packetBytes) {
                                    handleBite(bite);
                                }
                            }
                        } catch (Exception e) {
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

    private void handleBite(byte bite) throws UnsupportedEncodingException{
        final byte delimiter = 0; //This is the ASCII code for a \0
        final byte keyDelimiter = 2;
        final byte ivDelimiter = 3;

        if (bite == delimiter) {
            handleEndOfMessage();
        } else if(bite == keyDelimiter) {
            getKey();
        } else if(bite == ivDelimiter) {
            getIV();
        } else {
            readBuffer[readBufferPosition++] = bite;
        }

    }

    private void handleEndOfMessage() throws  UnsupportedEncodingException{
        // need key, iv, header(1 byte), and message(1 >= bytes)
        assert (readBufferPosition > (KEY_IV_SIZE*2)+2 );
        System.out.println("Got a new message " + readBufferPosition);
        byte[] keyBytes = new byte[KEY_IV_SIZE];
        byte[] ivBytes = new byte[KEY_IV_SIZE];
        byte sender_receiver;
        byte[] encodedBytes = new byte[readBufferPosition-(KEY_IV_SIZE*2+1)];

        System.arraycopy(readBuffer, 0, keyBytes, 0, KEY_IV_SIZE);
        System.arraycopy(readBuffer, KEY_IV_SIZE, ivBytes, 0, KEY_IV_SIZE);
        sender_receiver = readBuffer[KEY_IV_SIZE*2];
        System.arraycopy(readBuffer, KEY_IV_SIZE*2+1, encodedBytes, 0, encodedBytes.length);

        String keyReceived = byteArrToString(keyBytes, KEY_IV_SIZE);
        String ivReceived = byteArrToString(ivBytes, KEY_IV_SIZE);
        System.out.println("Key received: " + keyReceived);
        System.out.println("IV received: " + ivReceived);

        AESEncryption.print_cipher(encodedBytes, encodedBytes.length);

        final int chunkSize = 16;

        // we expect this to be padded
        assert (encodedBytes.length % chunkSize == 0);
        int numChunks = encodedBytes.length/chunkSize;
        System.out.println("Received " + encodedBytes.length + " bytes");

        byte[][] byteChunks = new byte[numChunks][16];
        int curr = 0;

        for(int currChunk = 0; currChunk < numChunks; currChunk++) {
            int length = Math.min(chunkSize, encodedBytes.length - curr);
            System.arraycopy(encodedBytes, curr, byteChunks[currChunk], 0, length);
            curr += chunkSize;
        }

        String[] decodedByteChunks = new String[numChunks];

        for (int j = 0; j < byteChunks.length; j++){
            try {
                decodedByteChunks[j] = AESEncryption.decrypt(byteChunks[j], keyReceived, ivReceived);
                System.out.print("Decoded byte: " + decodedByteChunks[j]);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        StringBuilder builder = new StringBuilder();
        for(String s : decodedByteChunks) {
            builder.append(s);
        }
        final String data = builder.toString().trim();

        final int receiver_id = 0xf & sender_receiver;
        final int sender_id = sender_receiver >>> 4;

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
    }

    private void getIV() throws UnsupportedEncodingException{
        assert (readBufferPosition > 0);
        ivRequested = byteArrToString(readBuffer, readBufferPosition);
        readBufferPosition = 0;
    }

    private void getKey() throws UnsupportedEncodingException{
        assert (readBufferPosition > 0);
        keyRequested = byteArrToString(readBuffer, readBufferPosition);
        readBufferPosition = 0;
    }

    private String byteArrToString(byte[] bytes, int bytesLength)
                throws UnsupportedEncodingException{
        byte[] encodedBytes = new byte[bytesLength];
        System.arraycopy(bytes, 0, encodedBytes, 0, encodedBytes.length);
        return new String(encodedBytes, FORMAT);
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

        TextView chatName = (TextView) findViewById(R.id.chat_name);
        chatName.setText(chatWith);

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
            int sender_id = getCurrentSender();
            int recipient_id = getCurrentReceiver();
            int messageHeader = (sender_id<<4) + recipient_id;

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
        try {
            outputStream.write(ENQ); //request key, iv from DE2

            System.out.println("Waiting for TS and GPS");
            while (keyRequested == null || ivRequested == null);
            System.out.println("Key requested: " + keyRequested);
            System.out.println("IV requested: " + ivRequested);

            outputStream.write(messageHeader);

            ArrayList<String> stringChunks = new ArrayList<>();
            for (int start = 0; start < message.length(); start += 16) {
                stringChunks.add(message.substring(start, Math.min(message.length(), start + 16)));
            }

            for (int start = 0; start < stringChunks.size(); start++){
                String paddedMessage = String.format("%1$16s", stringChunks.get(start));
                byte[] cipher = AESEncryption.encrypt(paddedMessage, keyRequested, ivRequested);
                AESEncryption.print_cipher(cipher, cipher.length);
                outputStream.write(cipher);
            }

            keyRequested = null;
            ivRequested = null;

        } catch (Exception e){
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