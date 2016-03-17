package team22.messagingapp;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.database.sqlite.*;

import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase messages;
    private BluetoothAdapter BA;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BluetoothSocket socket;

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

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
        final byte delimiter = 0; //This is the ASCII code for a newline character


        workerThread = new Thread(new Runnable() {
            public void run() {
                readBufferPosition = 0;
                readBuffer = new byte[1024];
                stopWorker = false;
                System.out.println("ayyyy");

                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = inputStream.available();
                        if(bytesAvailable > 0) {
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
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {

                                            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.message_holder);
                                            TextView textView = new TextView(getApplicationContext());
                                            textView.setText(data);
                                            textView.setTextColor(0xff000000);
                                            textView.setMaxWidth(300);
                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                                            textView.setLayoutParams(params);
                                            //textView.setBackgroundColor(0xffcccccc);
                                            textView.setBackgroundResource(R.drawable.bubble_grey);

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
                                    });
                                }else {
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
    protected void onStart(){
        super.onStart();
        try{
            chooseBluetooth();

        }catch(IOException e){
            e.printStackTrace();
        }


    }

    @Override
    protected void onStop(){
        super.onStart();
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
        messages = openOrCreateDatabase("Messages", Context.MODE_PRIVATE, null);
        messages.execSQL("CREATE TABLE IF NOT EXISTS messages(sender INTEGER, recipient INTEGER, message_text VARCHAR, message_date DATETIME, PRIMARY KEY(sender, recipient, message_date));");



        //Code for Bluetooth... Bluetooth won't work on emulator, so comment it out if on emu
        try {
            initBluetooth();
        }catch (IOException e){
            e.printStackTrace();
        }

        //listenMessages();

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
            LinearLayout parentLinearLayout = (LinearLayout) findViewById(R.id.message_holder);
            TextView textView = new TextView(this);
            textView.setTextColor(0xffffffff);
            //textView.setBackgroundColor(0xff32cd32);
            textView.setBackgroundResource(R.drawable.bubble_blue);
            textView.setText(message);
            textView.setMaxWidth(300);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(params);
            textView.setGravity(Gravity.RIGHT);

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setGravity(Gravity.RIGHT);
            linearLayout.addView(textView);
            if (parentLinearLayout != null) {
                parentLinearLayout.addView(linearLayout);
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

            if (outputStream != null) {
                System.out.println("Attempting to send message!");
                try {

                    int messageLength = message.length();
                    int messagePosition = 0;
                    System.out.println(messageLength);

                    while(messageLength > 255){
                        //sender = 0000 receiver = 0000
                        outputStream.write(1); // Sender/Receiver HARDCODED
                        outputStream.write(0);
                        String s = message.substring(messagePosition, messagePosition+255);
                        messagePosition += 255;
                        messageLength -= 255;
                        outputStream.write(s.getBytes("US-ASCII"));
                    }
                    outputStream.write(1);  // Sender/Receiver HARDCODED
                    outputStream.write(messageLength);
                    String s = message.substring(messagePosition, messagePosition+messageLength);
                    outputStream.write(s.getBytes("US-ASCII"));
                    System.out.println("Sent out " + s);
                    outputStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        //intent.putExtra(EXTRA_MESSAGE, message);
        //startActivity(intent);

    }

    public void receiveMessage(View view){
        String message = getMessage();
        if (message != null) {
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

    }

    public String getMessage(){
        //This will (eventually) poll the bluetooth to get the message. For now, I'm getting
        //a constant String

//        String s = "send me a message";
//        outputStream.write(s.getBytes("US-ASCII"));
//
//        inputStream.read();
        return "okay";

    }
}

//SQL Code... Not sure where to put it, so leaving it as a comment down here for now.
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