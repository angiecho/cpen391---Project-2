package team22.messagingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.R.id;
import android.content.Intent;


public class Contacts extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
    }

    public void openChat(View view){
        String chatWith = view.getTag().toString();
        Intent chatWindow = new Intent (this, MainActivity.class);
        chatWindow.putExtra("receiver", chatWith.toString());
        startActivity(chatWindow);
    }

}
