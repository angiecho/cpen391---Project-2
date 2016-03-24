package team22.messagingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.R.id;
import android.content.Intent;
import android.widget.Button;


public class Contacts extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        Bundle loginBundle = getIntent().getExtras();
        String user = loginBundle.getString("username");
        int resID = getResources().getIdentifier(user, "id", getPackageName());
        Log.v("user is:", user);
        View userButton = findViewById(resID);
        ViewGroup parent = (ViewGroup) userButton.getParent();
        parent.removeView(userButton);

    }

    public void openChat(View view){
        String chatWith = view.getTag().toString();
        Intent chatWindow = new Intent (this, MainActivity.class);
        chatWindow.putExtra("receiver", chatWith);
        startActivity(chatWindow);
    }

}
