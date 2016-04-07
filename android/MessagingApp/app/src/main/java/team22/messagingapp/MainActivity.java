package team22.messagingapp;

import android.app.NotificationManager;
import android.bluetooth.BluetoothSocket;
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
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.database.sqlite.*;
import android.util.Log;

import java.io.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BluetoothSocket socket;

    private static volatile String keyRequested;
    private static volatile String ivRequested;
    private static final int KEY_IV_SIZE = 16;

    public static final String SENDER = "sender";
    private static final String RECIPIENT = "recipient";
    private static final String MESSAGE_TEXT = "message_text";
    private static final String MESSAGE_DATE = "message_date";
    private static final String MESSAGES_TABLE = "messages";
    private static final String FORMAT = "US-ASCII";
    private static final int BLACK = 0xffffffff;
    private static final int BLUE = 0xff000000;
    private static final int WIDTH = 300;
    private static final int ENQ = 5;

    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;
    private Handler handler;
    volatile boolean waitForAck;
    private int id_index = 0;
    private ArrayList<Integer> ids;


    private View.OnFocusChangeListener focuser = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                ((LinearLayout) findViewById(R.id.search_holder)).setVisibility(View.GONE);
                ((EditText)v).setText("");
            }
        }
    };

    private MessageScrollView.onTopReachedListener onTopReachedListener = new MessageScrollView.onTopReachedListener() {
        @Override
        public void onTopReached() {
            int x = loadMessages();
            if (x != 0) {
                final View view2 = findViewById(x);
                if (view2 != null) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            MessageScrollView view = (MessageScrollView) findViewById(R.id.scrollView);
                            int top = getRelativeTop(view2);
                            view.scrollTo(0, top);
                            //This needs to be done **after** because
                            //Otherwise, it doesn't know where the new top is
                            //because of the change in height
                        }
                    });
                }
            }
        }
    };

    private View.OnClickListener previous = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            doPrevious();
        }
    };

    private View.OnClickListener next = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            doNext();
        }
    };

    private View.OnClickListener searcher = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            LinearLayout search =  (LinearLayout) findViewById(R.id.search_holder);
            if (search.getVisibility() == View.GONE){
                search.setVisibility(View.VISIBLE);
                ((EditText)findViewById(R.id.search_message)).requestFocus();
            }
            else {
                search.setVisibility(View.GONE);
            }
        }
    };

    private TextWatcher watcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {}
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() >= 3){
                String parameter = s.toString();
                ArrayList<Message> messages = getAllMessages();
                ids = findFuzzyMatches(messages, parameter, 0.7);
                findMatchMessages(ids);
            }
            else {
                findViewById(R.id.previous_button).setVisibility(View.GONE);
                findViewById(R.id.next_button).setVisibility(View.GONE);
                findViewById(R.id.results).setVisibility(View.GONE);
            }
        }
    };

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
        else if (string.toLowerCase().contentEquals("charles")) {
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
        final byte ackDelimiter = 6;
        if (bite == delimiter) {
            handleEndOfMessage();
        } else if (bite == keyDelimiter) {
            getKey();

        } else if (bite == ivDelimiter) {
            getIV();
        } else if (bite == ackDelimiter) {
            readBufferPosition = 0;
            waitForAck = false;


//        if (checkDelimiter(bite,delimiter)) {
//            handleEndOfMessage();
//        } else if(checkDelimiter(bite,keyDelimiter)) {
//            getKey();
//        } else if(checkDelimiter(bite,ivDelimiter)) {
//            getIV();
//        } else if(checkDelimiter(bite,ackDelimiter)) {
//            readBufferPosition = 0;
//            waitForAck = false;
        } else {
            readBuffer[readBufferPosition++] = bite;
        }

    }

    private boolean checkDelimiter(byte bite, byte delimiter) {
        if (bite != delimiter) {
            return false;
        }

        if (readBufferPosition < 2) {
            return false;
        }

        return (readBuffer[readBufferPosition-1] == delimiter
                && readBuffer[readBufferPosition-2] == delimiter);
    }


    private void handleEndOfMessage() throws  UnsupportedEncodingException{
        // need key, iv, header(1 byte), and message(1 >= bytes)
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
            s = s.replace("~", "");
            builder.append(s);
        }
        final String data = builder.toString().trim();

        final int receiver_id = 0xf & sender_receiver;
        final int sender_id = sender_receiver >>> 4;

        final Message m = Database.insertMessageToDatabase(sender_id, receiver_id, false, data, database);

        readBufferPosition = 0;
        handler.post(new Runnable() {
            public void run() {
                //insert check if it's the same user we're getting stuff from

                if (sender_id == getCurrentReceiver()) {
                    insertReceivedMessageToView(m, false);
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

    private String byteArrToString(byte[] bytes, int bytesLength) throws UnsupportedEncodingException{
        byte[] encodedBytes = new byte[bytesLength];
        System.arraycopy(bytes, 0, encodedBytes, 0, encodedBytes.length);
        return new String(encodedBytes, FORMAT);
    }
    @Override
    protected void onStart() {
        super.onStart();
        loadMessages();
        final MessageScrollView view = (MessageScrollView) findViewById(R.id.scrollView);
        if (view != null) {
            view.setOnTopReachedListener(onTopReachedListener);
        }
        listenMessages();
        scrollDown();
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

        EditText search = (EditText) findViewById(R.id.search_message);
        ((Button) findViewById(R.id.previous_button)).setOnClickListener(previous);
        ((Button) findViewById(R.id.next_button)).setOnClickListener(next);
        ((TextView)findViewById(R.id.start_search)).setOnClickListener(searcher);
        search.addTextChangedListener(watcher);
        search.setOnFocusChangeListener(focuser);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ");
        builder.setSpan(new ImageSpan(getApplicationContext(), R.drawable.search_white),
                builder.length() - 1, builder.length(), 0);


        TextView chatName = (TextView) findViewById(R.id.chat_name);
        TextView searchButton = (TextView) findViewById(R.id.start_search);
        chatName.setText(chatWith);
        searchButton.setText(builder);


        Log.v("Chat With:", chatWith);

        database = openOrCreateDatabase("Messages", Context.MODE_PRIVATE, null);
        //database.execSQL("DROP TABLE messages;"); //Drop table is here in case I want to clear the database
        database.execSQL("CREATE TABLE IF NOT EXISTS messages(id INTEGER PRIMARY KEY AUTOINCREMENT, sender INTEGER, recipient INTEGER, message_text VARCHAR, message_date DATETIME);");
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

    private ArrayList<Integer> findFuzzyMatches(ArrayList<Message> messages,
                                                String parameter, double fuzzyness){
        ArrayList<Integer> matchingIds = new ArrayList<Integer>();

        for (Message message : messages) {
            int dist = getLevenshteinDistance(message.text, parameter);
            int length = Math.max(message.text.length(), parameter.length());
            double score = 1.0 - (double)dist / length;

            if (score > fuzzyness) {
                matchingIds.add(message.id);
            }
        }

        return matchingIds;
    }

    private int getLevenshteinDistance(String lhs, String rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    private ArrayList<Message> getAllMessages(){
        int receiver_id = getCurrentReceiver();
        int sender_id = getCurrentSender();
        String columns[] = {MESSAGE_TEXT, MESSAGE_DATE, SENDER, RECIPIENT, "id"};
        ArrayList<Message> messagesAL = new ArrayList<>();
        String selectionQuery = "(recipient =? OR sender =?) AND (recipient =? OR sender =?)";
        String args[] = {String.valueOf(receiver_id), String.valueOf(receiver_id), String.valueOf(sender_id), String.valueOf(sender_id)};;

        Cursor cursor = database.query(MESSAGES_TABLE, columns, selectionQuery, args, null, null, "id desc", null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    String text = cursor.getString(cursor.getColumnIndex(MESSAGE_TEXT));
                    boolean sent = cursor.getInt(cursor.getColumnIndex(SENDER)) == sender_id;
                    String date = cursor.getString(cursor.getColumnIndex(MESSAGE_DATE));
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    Message message = new Message(text, sent, date, id);
                    messagesAL.add(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return messagesAL;
    }

    public void doPrevious(){
        if (id_index <= 0){
            id_index = 0;
        } else{
            id_index--;
            findViewById(R.id.next_button).setClickable(true);
            findViewById(R.id.previous_button).setClickable(true);
            if (id_index == 0){
                findViewById(R.id.previous_button).setClickable(false);
            }
        }
        matchMessages();
    }

    public void doNext(){
        if (id_index > ids.size() - 1){
            id_index = ids.size() - 1;
        } else{
            id_index++;
            findViewById(R.id.next_button).setClickable(true);
            findViewById(R.id.previous_button).setClickable(true);

            if (id_index == ids.size() - 1){
                findViewById(R.id.next_button).setClickable(false);
            }
        }
        matchMessages();
    }

    private void matchMessages(){
        TextView results = (TextView) findViewById(R.id.results);
        String resultText = String.valueOf((id_index+1))  + "/" + String.valueOf(ids.size());
        results.setText(resultText);
        MessageScrollView view = (MessageScrollView) findViewById(R.id.scrollView);
        TextView message = (TextView) view.findViewWithTag((ids.get(id_index)));
        while (message == null){
            loadMessages();
            message = (TextView) view.findViewWithTag((ids.get(id_index)));
        }
        if (message != null) {
            int top = getRelativeTop(message);
            view.scrollTo(0, top);
        }
    }

    private void findMatchMessages(ArrayList<Integer> ids){
        findViewById(R.id.previous_button).setVisibility(View.GONE);
        findViewById(R.id.next_button).setVisibility(View.GONE);
        findViewById(R.id.results).setVisibility(View.GONE);
        if (ids.size() > 1){
            findViewById(R.id.previous_button).setVisibility(View.VISIBLE);
            findViewById(R.id.next_button).setVisibility(View.VISIBLE);
        }
        if (ids.size() > 0){
            TextView resultsView = (TextView) findViewById(R.id.results);
            resultsView.setVisibility(View.VISIBLE);
            findViewById(R.id.next_button).setClickable(true);
            findViewById(R.id.previous_button).setClickable(false);
            id_index = 0;
            matchMessages();
        }
    }

    public int loadMessages(){
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

        //Show 15 more than before!
        Cursor cursor = database.query(MESSAGES_TABLE, columns, selectionQuery, args, null, null, "id desc", String.valueOf(messagesToShowCount));
        if (cursor.moveToFirst()) {
            ArrayList<Message> pastMessages = new ArrayList<>();
            do {
                try {
                    int r_id = cursor.getInt(cursor.getColumnIndexOrThrow(SENDER));
                    pastMessages.add(new Message(cursor.getString(cursor.getColumnIndex(MESSAGE_TEXT)), r_id == recipient_id, cursor.getString(cursor.getColumnIndex(MESSAGE_DATE)), cursor.getInt(cursor.getColumnIndex("id"))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            while (cursor.moveToNext());
            int difference = cursor.getCount() - messagesShown/2;

            for (int j = pastMessages.size() - difference; j <= pastMessages.size()-1; j++) {
                if (pastMessages.get(j).sent) {
                   insertReceivedMessageToView(pastMessages.get(j), true);
                } else {
                    insertSentMessageToView(pastMessages.get(j), true);
                }
            }
        }
        if (cursor.getCount() != messagesShown && view != null) {
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

            Message m1 = Database.insertMessageToDatabase(sender_id, recipient_id, true, message, database);
            insertSentMessageToView(m1, false);
            scrollDown();

            if (outputStream != null) {
                sendMessageBluetooth(message, messageHeader);
            }
        }
    }

    // Helper fcn for sending message bytes to bluetooth.
    private void sendMessageBluetooth(String message, int messageHeader){
        try {
            outputData((byte)ENQ); //request key, iv from DE2

            System.out.println("Waiting for TS and GPS");
            while (keyRequested == null || ivRequested == null);
            System.out.println("Key requested: " + keyRequested);
            System.out.println("IV requested: " + ivRequested);

            outputData((byte)messageHeader);

            ArrayList<String> stringChunks = new ArrayList<>();
            for (int start = 0; start < message.length(); start += 16) {
                stringChunks.add(message.substring(start, Math.min(message.length(), start + 16)));
            }
            outputData((byte)stringChunks.size());

            System.out.println(stringChunks.size() + " chunks to send.");

            for (int start = 0; start < stringChunks.size(); start++){
                String paddedMessage = ("~~~~~~~~~~~~~~~~" + stringChunks.get(start)).substring(stringChunks.get(start).length());
                //String paddedMessage = String.format("%1$16s", stringChunks.get(start));
                byte[] cipher = AESEncryption.encrypt(paddedMessage, keyRequested, ivRequested);
                AESEncryption.print_cipher(cipher, cipher.length);
                for (byte bite : cipher) {
                    outputData(bite);
                }

            }

            outputStream.flush();
            keyRequested = null;
            ivRequested = null;

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void outputData(byte data) throws IOException{
        outputStream.write(data);
        waitForAck = true;
        while(waitForAck);
        System.out.print("Ack");
    }

    // Display received message, store it in the DB, and get a notification.
    public void receiveMessage(View view){

        String message_text = getMessage();
        if (message_text != null) {
            boolean sent = getCurrentSender() == getCurrentReceiver();
            Message message = Database.insertMessageToDatabase(getCurrentReceiver(), getCurrentSender(), sent, message_text, database);
            insertReceivedMessageToView(message, false);
            String author = getSenderName(getCurrentReceiver());
            showNotification(message.text, author);
        }
    }

    // Display sent message and returns the id of the text view.
    public int insertSentMessageToView(Message message, boolean top){
        LinearLayout parentLinearLayout = (LinearLayout) findViewById(R.id.message_holder);
        TextView textView = getSendMessageTextView();
        textView.setText(message.text);
        textView.setTag(message.id);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setGravity(Gravity.RIGHT);
        linearLayout.addView(textView);
        linearLayout.setId(View.generateViewId());

        TextView dateView = new TextView(getApplicationContext());
        dateView.setId(View.generateViewId());
        dateView.setText(message.date.toString());
        dateView.setMaxWidth(WIDTH);

        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setGravity(Gravity.RIGHT);
        linearLayout2.addView(dateView);
        linearLayout2.setId(View.generateViewId());

        if (parentLinearLayout != null) {
            if (top) {
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
    public int insertReceivedMessageToView(Message message, boolean top){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.message_holder);
        TextView textView = new TextView(getApplicationContext());
        textView.setId(View.generateViewId());
        textView.setTag(message.id);
        textView.setText(message.text);
        textView.setTextColor(BLUE);
        textView.setMaxWidth(WIDTH);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setBackgroundResource(R.drawable.bubble_grey);

        TextView dateView = new TextView(getApplicationContext());
        dateView.setId(View.generateViewId());
        dateView.setText(message.date.toString());
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

    private int getRelativeTop(View myView) {
        if (myView.getParent() == findViewById(R.id.scrollView))
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }
}