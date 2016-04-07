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
import java.io.OutputStream;

public class Contacts extends AppCompatActivity {
    private static OutputStream outputStream;
    private static final Integer EOT = 4;
    private static String User;
    private SQLiteDatabase db;

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
        chatWindow.putExtra("receiver", chatWith);
        chatWindow.putExtra("senderName", getIntent().getExtras().getString("username"));
        startActivity(chatWindow);
    }

    @Override
    public void onBackPressed() {
        logout();
        super.onBackPressed();
    }

    public void logout() {
        Integer ID = Database.getUserID(User, db);
        System.out.println("Logging out:" + ID + "\n");
        connectBT();
        SystemClock.sleep(250);
        try {
            for (int i = 0; i < 3; i++) {
                SystemClock.sleep(250);
                outputStream.write(EOT);
            }
            outputStream.write(ID);
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
            System.out.println("Yay output stream!");
        }
    }
}
