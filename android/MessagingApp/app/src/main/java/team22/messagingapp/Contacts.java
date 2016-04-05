package team22.messagingapp;

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
    private OutputStream outputStream;
    private static final Integer EOT = 4;
    private String User;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        removeUser();
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

    public void logout(View view) {
        Integer ID = Login.getUserID(User);
        try {
            for (int i = 0; i < 3; i++) {
                outputStream.write(EOT);
            }
            outputStream.write(ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent loginScreen = new Intent(this, Login.class);
        startActivity(loginScreen);
    }


    private void connect(){
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
        }
    }
}
