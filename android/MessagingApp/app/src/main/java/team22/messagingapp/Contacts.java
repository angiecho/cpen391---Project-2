package team22.messagingapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.R.id;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class Contacts extends AppCompatActivity {
    private static OutputStream outputStream;
    private static InputStream inputStream;
    private static final int EOT = 4;
    private static String User;
    private SQLiteDatabase db;
    volatile boolean waitForAck;
    private byte[] readBuffer;
    private int readBufferPosition;


    private View.OnClickListener logouter = new View.OnClickListener(){
        @Override
        public void onClick(View v){
           logout();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        db = openOrCreateDatabase("Messages", Context.MODE_PRIVATE, null);
        removeUser();   // Remove current user from contact list
        findViewById(R.id.logout).setOnClickListener(logouter);

    }

    private void removeUser() {
        Bundle loginBundle = getIntent().getExtras();
        User = loginBundle.getString("username");
        int resID = getResources().getIdentifier(User, "id", getPackageName());
        View userButton = findViewById(resID);
        ViewGroup parent = (ViewGroup) userButton.getParent();
        parent.removeView(userButton);
    }

    public void openChat(View view) {
        String chatWith = view.getTag().toString();
        Intent chatWindow = new Intent(this, MainActivity.class);
        Bundle loginBundle = getIntent().getExtras();
        String senderName = loginBundle.getString("username");
        chatWindow.putExtra("receiver", chatWith);
        chatWindow.putExtra("senderName", senderName);
        startActivity(chatWindow);
    }

    @Override
    public void onBackPressed() {
        logout();
        super.onBackPressed();
    }

    public void logout() {
        int ID = Database.getUserID(User, db);
        System.out.println("Logging out:" + ID + "\n");
        connectBT();
        SystemClock.sleep(250);
        try {
            for (int i = 0; i < 3; i++) {
                SystemClock.sleep(250);
                System.out.println("This is the " + i + "th iteration.");
                outputData((byte) EOT);
            }
            outputData((byte)ID);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent loginScreen = new Intent(this, Login.class);
        startActivity(loginScreen);
    }

    /*  Code duplicate. Login.java and Contacts.java
        Function cannot be shared due to Activity classes.
     */
   private void connectBT(){
        BluetoothSocket socket = ((MessagingApplication) getApplication()).getSocket();
        if (!socket.isConnected()){
            try {
                socket.connect();
                outputStream = socket.getOutputStream();

            } catch(IOException e){
                e.printStackTrace();
            }
        }
        else {
            outputStream = ((MessagingApplication) getApplication()).getOutputStream();
            inputStream = ((MessagingApplication)getApplication()).getInputStream();
            System.out.println("Yay streams!");
        }
    }

    private void outputData(byte data) throws IOException{
        outputStream.write(data);
        SystemClock.sleep(100);
        waitForAck = true;
        if(waitForAck){
            int bytesAvailable = inputStream.available();
            System.out.println("Bytes available: " + bytesAvailable);
            while (bytesAvailable > 0) {
                readBufferPosition = 0;
                readBuffer = new byte[4096];
                byte[] packetBytes = new byte[bytesAvailable];
                inputStream.read(packetBytes);
                for (byte bite : packetBytes) {
                    handleBite(bite);
                }
                bytesAvailable = inputStream.available();
            }
        }
        System.out.print("Ack");
    }

    private void handleBite(byte bite) throws UnsupportedEncodingException {

        final byte ackDelimiter = 6;

        System.out.println(bite);

        if(checkDelimiter(bite,ackDelimiter)) {
            System.out.println("I got noticed!");
            readBufferPosition = 0;
            waitForAck = false;
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
}
