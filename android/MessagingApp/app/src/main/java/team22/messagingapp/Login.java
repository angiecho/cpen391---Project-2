package team22.messagingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class Login extends AppCompatActivity {
    EditText mUser;
    EditText mPin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view){
        mUser = (EditText)findViewById(R.id.username);
        mPin = (EditText)findViewById(R.id.pin);
        String userID = mUser.getText().toString();
        String userPIN = mPin.getText().toString();
        mUser.setText("");
        mPin.setText("");

        Intent chatWindow = new Intent (this, Contacts.class);
        chatWindow.putExtra("username", userID);
        startActivity(chatWindow);
    }
}
