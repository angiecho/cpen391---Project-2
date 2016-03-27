package team22.messagingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.R.id;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;


public class Contacts extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        checkUser();

    }

    public void checkUser(){
        Bundle loginBundle = getIntent().getExtras();
        int user = loginBundle.getInt("resUser");
        View userButton = findViewById(user);
        ViewGroup parent = (ViewGroup) userButton.getParent();
        parent.removeView(userButton);
    }

    public void openChat(View view){
        String chatWith = view.getTag().toString();
        Intent chatWindow = new Intent (this, MainActivity.class);
        chatWindow.putExtra("receiver", chatWith);
        chatWindow.putExtra("senderName", getIntent().getExtras().getString("username"));
        startActivity(chatWindow);
    }

}
